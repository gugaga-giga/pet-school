"""产品推荐策略引擎 - 根据意图和策略生成产品推荐"""

import json
from typing import Any, Dict, List, Optional

from loguru import logger
from sqlalchemy.orm import Session

from app.llm.llm_service import LLMService
from app.llm.prompts.templates import PromptTemplates
from app.models.product import Product, RecommendRule
from app.recommend.strategy import RecommendStrategy


class RecommendEngine:
    """产品推荐策略引擎

    根据用户意图选择推荐策略，匹配产品并生成推荐理由。
    支持多种推荐策略：特性匹配、价格区间、综合推荐、对比推荐等。
    """

    def __init__(self, db_session: Session, llm_service: LLMService) -> None:
        """初始化推荐引擎

        Args:
            db_session: 数据库会话
            llm_service: LLM服务实例
        """
        self.db = db_session
        self.llm_service = llm_service

    async def recommend(
        self,
        query: str,
        intent: Dict[str, Any],
        top_k: int = 3,
    ) -> List[Dict[str, Any]]:
        """生成推荐

        根据意图类型选择推荐策略，匹配产品并生成推荐理由。

        Args:
            query: 用户查询文本
            intent: 意图识别结果，包含intent_type/keywords等
            top_k: 返回的推荐产品数量，默认3

        Returns:
            推荐结果列表，每项包含:
                - product_id: 产品ID
                - name: 产品名称
                - reason: 推荐理由
                - score: 相关性分数
        """
        intent_type = intent.get("intent_type", "general")
        keywords = intent.get("keywords", [])

        # 根据意图类型选择推荐策略
        strategy = RecommendStrategy.get_strategy(intent_type)
        logger.info(
            f"推荐引擎: intent={intent_type}, strategy={strategy}, "
            f"keywords={keywords}"
        )

        # 根据策略执行推荐
        strategy_method = {
            "feature_match": self._strategy_product_inquiry,
            "price_range": self._strategy_price_inquiry,
            "comprehensive": self._strategy_purchase_advice,
            "comparison": self._strategy_comparison,
            "service_match": self._strategy_product_inquiry,
        }.get(strategy, self._strategy_purchase_advice)

        try:
            products = await strategy_method(query, intent)
        except Exception as e:
            logger.error(f"推荐策略执行失败: {e}")
            products = []

        if not products:
            # 降级为规则匹配
            products = self._match_products_by_rules(intent_type)
            logger.info(f"降级为规则匹配，获取到 {len(products)} 个产品")

        # 计算相关性分数并排序
        scored_products: List[Dict[str, Any]] = []
        for product in products[:top_k * 3]:
            score = self._calculate_relevance_score(product, query, intent)
            product["score"] = score
            scored_products.append(product)

        # 按分数降序排序
        scored_products.sort(key=lambda x: x["score"], reverse=True)

        # 取top_k个产品，生成推荐理由
        results: List[Dict[str, Any]] = []
        for product in scored_products[:top_k]:
            reason = await self._generate_recommend_reason(product, query, intent)
            results.append({
                "product_id": product.get("id") or product.get("product_id"),
                "name": product.get("name", ""),
                "reason": reason,
                "score": round(product["score"], 4),
                "category": product.get("category"),
                "price": product.get("price"),
                "brand": product.get("brand"),
            })

        logger.info(f"推荐引擎返回 {len(results)} 个产品")
        return results

    async def _strategy_product_inquiry(
        self, query: str, intent: Dict[str, Any]
    ) -> List[Dict[str, Any]]:
        """产品咨询策略：匹配产品特性

        根据用户查询中的关键词匹配产品名称、描述、规格参数等。

        Args:
            query: 用户查询文本
            intent: 意图识别结果

        Returns:
            匹配的产品列表
        """
        keywords = intent.get("keywords", [])

        # 先尝试关键词匹配
        products = self._match_products_by_keywords(keywords)

        if not products:
            # 关键词无匹配，尝试从查询中提取更多关键词
            import jieba
            extra_keywords = list(jieba.cut(query))
            extra_keywords = [w for w in extra_keywords if len(w) > 1]
            products = self._match_products_by_keywords(extra_keywords)

        return products

    async def _strategy_price_inquiry(
        self, query: str, intent: Dict[str, Any]
    ) -> List[Dict[str, Any]]:
        """价格咨询策略：匹配价格区间

        尝试从查询中提取价格区间，匹配该区间内的产品。

        Args:
            query: 用户查询文本
            intent: 意图识别结果

        Returns:
            匹配的产品列表
        """
        import re

        # 尝试提取价格区间
        price_range = self._extract_price_range(query)

        # 先获取所有上架产品
        query_obj = self.db.query(Product).filter(Product.is_available == True)
        products = query_obj.all()

        product_list: List[Dict[str, Any]] = []
        for p in products:
            product_dict = self._product_to_dict(p)

            # 如果有价格区间，进行过滤
            if price_range:
                try:
                    price_val = float(p.price) if p.price else 0
                    min_price, max_price = price_range
                    if min_price is not None and price_val < min_price:
                        continue
                    if max_price is not None and price_val > max_price:
                        continue
                except (ValueError, TypeError):
                    pass

            product_list.append(product_dict)

        # 按价格排序（价格咨询场景下价格是核心维度）
        product_list.sort(key=lambda x: self._safe_price(x.get("price", "0")))

        return product_list

    async def _strategy_purchase_advice(
        self, query: str, intent: Dict[str, Any]
    ) -> List[Dict[str, Any]]:
        """购买建议策略：综合推荐

        综合考虑关键词匹配、推荐规则、销量和评分进行推荐。

        Args:
            query: 用户查询文本
            intent: 意图识别结果

        Returns:
            匹配的产品列表
        """
        keywords = intent.get("keywords", [])
        intent_type = intent.get("intent_type", "general")

        # 1. 关键词匹配
        keyword_products = self._match_products_by_keywords(keywords)

        # 2. 推荐规则匹配
        rule_products = self._match_products_by_rules(intent_type)

        # 合并去重
        seen_ids: set = set()
        all_products: List[Dict[str, Any]] = []

        for product in keyword_products + rule_products:
            pid = product.get("id") or product.get("product_id")
            if pid and pid not in seen_ids:
                seen_ids.add(pid)
                all_products.append(product)

        # 如果匹配结果不足，补充热门产品
        if len(all_products) < 3:
            hot_products = self._get_hot_products()
            for product in hot_products:
                pid = product.get("id") or product.get("product_id")
                if pid and pid not in seen_ids:
                    seen_ids.add(pid)
                    all_products.append(product)

        return all_products

    async def _strategy_comparison(
        self, query: str, intent: Dict[str, Any]
    ) -> List[Dict[str, Any]]:
        """竞品对比策略：对比推荐

        识别用户提到的产品或品类，返回同类产品用于对比。

        Args:
            query: 用户查询文本
            intent: 意图识别结果

        Returns:
            匹配的产品列表
        """
        keywords = intent.get("keywords", [])

        # 尝试识别用户提到的品类
        category = self._detect_category(query)

        if category:
            # 按品类查找产品
            products = (
                self.db.query(Product)
                .filter(Product.is_available == True, Product.category == category)
                .all()
            )
            product_list = [self._product_to_dict(p) for p in products]
        else:
            # 无明确品类，使用关键词匹配
            product_list = self._match_products_by_keywords(keywords)

        return product_list

    def _match_products_by_keywords(
        self,
        keywords: List[str],
        category: Optional[str] = None,
    ) -> List[Dict[str, Any]]:
        """根据关键词匹配产品

        在产品名称、描述、品牌、标签中搜索关键词。

        Args:
            keywords: 关键词列表
            category: 可选的分类过滤

        Returns:
            匹配的产品列表
        """
        if not keywords:
            return []

        query_obj = self.db.query(Product).filter(Product.is_available == True)

        if category:
            query_obj = query_obj.filter(Product.category == category)

        # 构建OR条件：任意关键词匹配名称、描述、品牌或标签
        from sqlalchemy import or_

        conditions: list = []
        for keyword in keywords:
            if len(keyword) < 2:
                continue
            conditions.append(Product.name.like(f"%{keyword}%"))
            conditions.append(Product.description.like(f"%{keyword}%"))
            conditions.append(Product.brand.like(f"%{keyword}%"))
            conditions.append(Product.tags.like(f"%{keyword}%"))

        if not conditions:
            return []

        query_obj = query_obj.filter(or_(*conditions))
        products = query_obj.limit(20).all()

        return [self._product_to_dict(p) for p in products]

    def _match_products_by_rules(self, intent_type: str) -> List[Dict[str, Any]]:
        """根据推荐规则匹配产品

        从数据库中读取激活的推荐规则，匹配对应意图类型的产品。

        Args:
            intent_type: 意图类型

        Returns:
            匹配的产品列表
        """
        try:
            # 查询激活的推荐规则，按优先级降序
            rules = (
                self.db.query(RecommendRule)
                .filter(RecommendRule.is_active == True)
                .order_by(RecommendRule.priority.desc())
                .all()
            )

            product_ids: List[int] = []
            for rule in rules:
                try:
                    conditions = json.loads(rule.conditions) if rule.conditions else {}
                    # 检查规则条件是否匹配当前意图
                    if self._rule_matches_intent(rule, intent_type, conditions):
                        rule_product_ids = json.loads(rule.product_ids) if rule.product_ids else []
                        product_ids.extend(rule_product_ids)
                except (json.JSONDecodeError, TypeError) as e:
                    logger.warning(f"推荐规则 {rule.id} 解析失败: {e}")
                    continue

            # 去重
            product_ids = list(dict.fromkeys(product_ids))

            if not product_ids:
                return []

            # 查询产品
            products = (
                self.db.query(Product)
                .filter(
                    Product.id.in_(product_ids),
                    Product.is_available == True,
                )
                .all()
            )

            return [self._product_to_dict(p) for p in products]

        except Exception as e:
            logger.error(f"推荐规则匹配失败: {e}")
            return []

    def _calculate_relevance_score(
        self,
        product: Dict[str, Any],
        query: str,
        intent: Dict[str, Any],
    ) -> float:
        """计算产品与查询的相关性分数

        根据推荐策略的权重配置，综合计算产品在特性、价格、评分、
        热度四个维度的加权分数。

        Args:
            product: 产品信息字典
            query: 用户查询文本
            intent: 意图识别结果

        Returns:
            相关性分数 (0-1)
        """
        intent_type = intent.get("intent_type", "general")
        strategy = RecommendStrategy.get_strategy(intent_type)
        weights = RecommendStrategy.get_weights(strategy)

        # 1. 特性匹配分数：关键词在产品信息中的命中情况
        feature_score = self._calc_feature_score(product, intent.get("keywords", []))

        # 2. 价格匹配分数：基于价格合理性
        price_score = self._calc_price_score(product, query)

        # 3. 评分分数：产品评分
        rating_score = self._calc_rating_score(product)

        # 4. 热度分数：销量
        popularity_score = self._calc_popularity_score(product)

        # 加权计算总分
        total_score = (
            weights["feature"] * feature_score
            + weights["price"] * price_score
            + weights["rating"] * rating_score
            + weights["popularity"] * popularity_score
        )

        return min(1.0, total_score)

    async def _generate_recommend_reason(
        self,
        product: Dict[str, Any],
        query: str,
        intent: Dict[str, Any],
    ) -> str:
        """使用LLM生成推荐理由

        根据产品信息和用户意图，调用LLM生成个性化的推荐理由。

        Args:
            product: 产品信息字典
            query: 用户查询文本
            intent: 意图识别结果

        Returns:
            推荐理由文本
        """
        product_info = (
            f"产品名称：{product.get('name', '未知')}\n"
            f"品牌：{product.get('brand', '未知')}\n"
            f"分类：{product.get('category', '未知')}\n"
            f"价格：{product.get('price', '未知')}\n"
            f"描述：{product.get('description', '无')}\n"
            f"规格参数：{product.get('specifications', '无')}\n"
            f"评分：{product.get('rating', '未知')}\n"
            f"销量：{product.get('sales_count', 0)}"
        )

        prompt = f"""基于用户需求和产品信息，简要说明推荐这款产品的理由（1-2句话）。

用户需求：{query}
意图类型：{intent.get('intent_type', 'general')}

产品信息：
{product_info}

请直接输出推荐理由，不要输出其他内容。"""

        try:
            messages = [
                {"role": "system", "content": "你是一个产品推荐专家，请简洁地说明推荐理由。"},
                {"role": "user", "content": prompt},
            ]
            reason = await self.llm_service.chat_cached(
                messages, temperature=0.3, max_tokens=128
            )
            return reason.strip()
        except Exception as e:
            logger.warning(f"LLM生成推荐理由失败: {e}")
            # 降级为模板理由
            name = product.get("name", "该产品")
            return f"{name}符合您的需求，具有良好的用户评价和性价比。"

    # ==================== 辅助方法 ====================

    @staticmethod
    def _product_to_dict(product: Product) -> Dict[str, Any]:
        """将Product ORM对象转换为字典

        Args:
            product: Product ORM对象

        Returns:
            产品信息字典
        """
        return {
            "id": product.id,
            "name": product.name,
            "category": product.category,
            "brand": product.brand,
            "description": product.description,
            "specifications": product.specifications,
            "price": product.price,
            "original_price": product.original_price,
            "image_url": product.image_url,
            "tags": product.tags,
            "sales_count": product.sales_count or 0,
            "rating": product.rating,
            "stock": product.stock,
        }

    @staticmethod
    def _extract_price_range(query: str) -> Optional[tuple]:
        """从查询中提取价格区间

        支持格式：100-200元、100到200、100~200、100元以上、200元以内等。

        Args:
            query: 用户查询文本

        Returns:
            (min_price, max_price) 元组，无法提取时返回None
        """
        import re

        # 匹配 "数字-数字" 或 "数字~数字" 或 "数字到数字"
        range_pattern = r"(\d+)\s*[-~到至]\s*(\d+)"
        match = re.search(range_pattern, query)
        if match:
            return (float(match.group(1)), float(match.group(2)))

        # 匹配 "数字元以上"
        above_pattern = r"(\d+)\s*元以上"
        match = re.search(above_pattern, query)
        if match:
            return (float(match.group(1)), None)

        # 匹配 "数字元以内" / "不超过数字元"
        below_pattern = r"(\d+)\s*元以内|不超过\s*(\d+)\s*元"
        match = re.search(below_pattern, query)
        if match:
            value = match.group(1) or match.group(2)
            return (None, float(value))

        return None

    @staticmethod
    def _detect_category(query: str) -> Optional[str]:
        """从查询中检测产品品类

        Args:
            query: 用户查询文本

        Returns:
            品类名称，未检测到返回None
        """
        # 常见品类关键词映射
        category_keywords: Dict[str, List[str]] = {
            "手机": ["手机", "智能手机", "电话"],
            "电脑": ["电脑", "笔记本", "台式机", "平板"],
            "耳机": ["耳机", "耳麦", "头戴式", "入耳式"],
            "电视": ["电视", "电视机", "显示器"],
            "相机": ["相机", "摄像机", "单反", "微单"],
            "家电": ["冰箱", "洗衣机", "空调", "微波炉", "烤箱"],
        }

        for category, keywords in category_keywords.items():
            for keyword in keywords:
                if keyword in query:
                    return category

        return None

    @staticmethod
    def _rule_matches_intent(
        rule: RecommendRule,
        intent_type: str,
        conditions: Dict[str, Any],
    ) -> bool:
        """判断推荐规则是否匹配当前意图

        Args:
            rule: 推荐规则对象
            intent_type: 意图类型
            conditions: 规则条件字典

        Returns:
            是否匹配
        """
        # 检查规则类型是否匹配意图
        rule_type = rule.rule_type
        if rule_type == "keyword":
            # 关键词规则：检查条件中的意图类型
            rule_intent = conditions.get("intent_type")
            if rule_intent and rule_intent != intent_type:
                return False
        elif rule_type == "category":
            # 品类规则：总是匹配，后续通过产品品类过滤
            pass
        elif rule_type == "tag":
            # 标签规则：总是匹配
            pass
        elif rule_type == "custom":
            # 自定义规则：检查意图匹配
            rule_intent = conditions.get("intent_type")
            if rule_intent and rule_intent != intent_type:
                return False

        return True

    @staticmethod
    def _calc_feature_score(product: Dict[str, Any], keywords: List[str]) -> float:
        """计算特性匹配分数

        Args:
            product: 产品信息字典
            keywords: 关键词列表

        Returns:
            特性匹配分数 (0-1)
        """
        if not keywords:
            return 0.3

        # 将产品信息合并为可搜索的文本
        searchable_text = " ".join([
            str(product.get("name", "")),
            str(product.get("description", "")),
            str(product.get("brand", "")),
            str(product.get("tags", "")),
            str(product.get("specifications", "")),
            str(product.get("category", "")),
        ]).lower()

        matched = sum(1 for kw in keywords if kw.lower() in searchable_text)
        return min(1.0, matched / max(len(keywords), 1))

    @staticmethod
    def _calc_price_score(product: Dict[str, Any], query: str) -> float:
        """计算价格匹配分数

        基于价格合理性和是否有价格相关查询。

        Args:
            product: 产品信息字典
            query: 用户查询文本

        Returns:
            价格匹配分数 (0-1)
        """
        try:
            price = float(product.get("price", 0)) if product.get("price") else 0
        except (ValueError, TypeError):
            return 0.3

        # 如果查询中有价格区间，检查产品是否在区间内
        price_range = RecommendEngine._extract_price_range(query)
        if price_range:
            min_price, max_price = price_range
            if min_price and price < min_price:
                return 0.1
            if max_price and price > max_price:
                return 0.1
            return 1.0

        # 无明确价格区间，根据价格合理性给分
        # 价格越低（在合理范围内）分数越高
        if price <= 0:
            return 0.3
        elif price < 500:
            return 0.8
        elif price < 2000:
            return 0.6
        elif price < 5000:
            return 0.5
        else:
            return 0.4

    @staticmethod
    def _calc_rating_score(product: Dict[str, Any]) -> float:
        """计算评分分数

        Args:
            product: 产品信息字典

        Returns:
            评分分数 (0-1)
        """
        try:
            rating = float(product.get("rating", 0)) if product.get("rating") else 0
        except (ValueError, TypeError):
            return 0.3

        # 假设评分为0-5分制
        if rating <= 0:
            return 0.3
        return min(1.0, rating / 5.0)

    @staticmethod
    def _calc_popularity_score(product: Dict[str, Any]) -> float:
        """计算热度分数

        基于销量计算热度分数。

        Args:
            product: 产品信息字典

        Returns:
            热度分数 (0-1)
        """
        sales = product.get("sales_count", 0) or 0
        if sales <= 0:
            return 0.2
        elif sales < 100:
            return 0.4
        elif sales < 500:
            return 0.6
        elif sales < 1000:
            return 0.8
        else:
            return 1.0

    def _get_hot_products(self, limit: int = 5) -> List[Dict[str, Any]]:
        """获取热门产品

        按销量降序获取热门产品。

        Args:
            limit: 返回数量

        Returns:
            热门产品列表
        """
        try:
            products = (
                self.db.query(Product)
                .filter(Product.is_available == True)
                .order_by(Product.sales_count.desc())
                .limit(limit)
                .all()
            )
            return [self._product_to_dict(p) for p in products]
        except Exception as e:
            logger.error(f"获取热门产品失败: {e}")
            return []

    @staticmethod
    def _safe_price(price_str: str) -> float:
        """安全地将价格字符串转为浮点数

        Args:
            price_str: 价格字符串

        Returns:
            价格浮点数
        """
        try:
            return float(price_str) if price_str else 0.0
        except (ValueError, TypeError):
            return 0.0
