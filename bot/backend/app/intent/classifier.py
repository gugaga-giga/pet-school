"""意图分类器 - 基于LLM的意图识别，支持规则降级"""

import json
from typing import Any, Dict, List, Optional

import jieba
import jieba.analyse
from loguru import logger

from app.llm.llm_service import LLMService
from app.llm.prompts.templates import PromptTemplates


class IntentClassifier:
    """意图分类器 - 基于LLM的意图识别

    优先使用LLM进行意图分类，当LLM不可用时降级为基于规则的关键词匹配。
    """

    # 意图类别定义
    INTENT_TYPES: Dict[str, str] = {
        "FAQ": "常见问题",
        "Course": "课程咨询",
        "PetKnowledge": "宠物知识",
        "Order": "订单查询",
        "PetProfile": "宠物档案",
        "UserInfo": "用户信息",
        "Greeting": "问候闲聊",
        "Other": "其他",
    }

    # 每个意图类型的关键词列表，用于规则匹配降级
    INTENT_KEYWORDS: Dict[str, List[str]] = {
        "FAQ": ["营业时间", "几点开门", "几点下班", "地址", "在哪里", "怎么去", "电话", "联系方式", "收费", "价格表", "服务内容", "预约"],
        "Course": ["课程", "训练", "培训", "班", "学习", "教学", "训犬", "服从训练", "行为纠正", "幼犬班", "进阶班", "敏捷训练"],
        "PetKnowledge": ["怎么养", "饲养", "喂什么", "吃什么", "疫苗", "驱虫", "绝育", "洗澡", "掉毛", "生病", "症状", "品种", "金毛", "拉布拉多", "柯基", "泰迪", "哈士奇", "猫", "狗", "宠物知识"],
        "Order": ["订单", "购买记录", "消费记录", "付款", "退款", "预约状态", "报名状态"],
        "PetProfile": ["我的宠物", "宠物档案", "疫苗记录", "体重", "健康", "旺财", "宠物信息"],
        "UserInfo": ["我的账户", "个人信息", "账户信息", "会员", "积分", "余额", "我的资料"],
        "Greeting": ["你好", "在吗", "嗨", "hi", "hello", "谢谢", "再见", "早上好", "下午好"],
        "Other": ["帮忙", "咨询"],
    }

    def __init__(self, llm_service: LLMService) -> None:
        """初始化意图分类器

        Args:
            llm_service: LLM服务实例
        """
        self.llm_service = llm_service

    async def classify(self, query: str) -> Dict[str, Any]:
        """分类用户意图

        优先使用LLM进行意图分类，失败时降级为规则匹配。

        Args:
            query: 用户查询文本

        Returns:
            分类结果字典，包含:
                - intent_type: 意图类型
                - confidence: 置信度 (0-1)
                - keywords: 提取的关键词列表
                - sub_intent: 子意图（如有）
        """
        if not query or not query.strip():
            return {
                "intent_type": "Other",
                "confidence": 0.0,
                "keywords": [],
                "sub_intent": None,
            }

        # 提取关键词（无论LLM是否成功都需要）
        keywords = self.extract_keywords(query)

        try:
            # 使用LLM进行意图分类
            result = await self._classify_by_llm(query, keywords)
            logger.info(
                f"LLM意图分类: query='{query[:30]}...', "
                f"intent={result['intent_type']}, "
                f"confidence={result['confidence']:.2f}"
            )
            return result
        except Exception as e:
            logger.warning(f"LLM意图分类失败，降级为规则匹配: {e}")
            # 降级为规则匹配
            result = self.classify_by_rules(query)
            result["keywords"] = keywords
            logger.info(
                f"规则意图分类: query='{query[:30]}...', "
                f"intent={result['intent_type']}, "
                f"confidence={result['confidence']:.2f}"
            )
            return result

    async def classify_batch(self, queries: List[str]) -> List[Dict[str, Any]]:
        """批量分类用户意图

        Args:
            queries: 用户查询文本列表

        Returns:
            分类结果列表，顺序与输入一致
        """
        results: List[Dict[str, Any]] = []
        for query in queries:
            result = await self.classify(query)
            results.append(result)
        return results

    def classify_by_rules(self, query: str) -> Dict[str, Any]:
        """基于规则的快速分类（关键词匹配）

        作为LLM分类的补充和降级方案，通过关键词匹配判断意图。

        Args:
            query: 用户查询文本

        Returns:
            分类结果字典
        """
        if not query or not query.strip():
            return {
                "intent_type": "Other",
                "confidence": 0.0,
                "keywords": [],
                "sub_intent": None,
            }

        query_lower = query.lower()
        best_intent = "Other"
        best_score = 0.0
        matched_keywords: List[str] = []

        for intent_type, keyword_list in self.INTENT_KEYWORDS.items():
            score = 0.0
            intent_matched: List[str] = []

            for keyword in keyword_list:
                if keyword.lower() in query_lower:
                    # 关键词越长，权重越高
                    weight = len(keyword) / 2.0
                    score += weight
                    intent_matched.append(keyword)

            if score > best_score:
                best_score = score
                best_intent = intent_type
                matched_keywords = intent_matched

        # 计算置信度：基于匹配关键词数量和权重
        # 最少匹配1个关键词，置信度范围0.3-0.8（规则匹配的置信度上限低于LLM）
        confidence = min(0.8, 0.3 + best_score * 0.1) if best_score > 0 else 0.2

        return {
            "intent_type": best_intent,
            "confidence": round(confidence, 2),
            "keywords": matched_keywords,
            "sub_intent": None,
        }

    def extract_keywords(self, query: str) -> List[str]:
        """提取关键词

        使用jieba提取关键词，结合TF-IDF算法。

        Args:
            query: 用户查询文本

        Returns:
            关键词列表
        """
        if not query or not query.strip():
            return []

        try:
            # 使用jieba的TF-IDF提取关键词
            keywords = jieba.analyse.extract_tags(query, topK=5, withWeight=False)
            return list(keywords)
        except Exception as e:
            logger.warning(f"jieba关键词提取失败: {e}")
            # 降级为简单分词
            words = jieba.lcut(query)
            # 过滤停用词和短词
            stopwords = {"的", "了", "吗", "呢", "啊", "是", "在", "有", "和", "与", "我", "你", "这", "那"}
            return [w for w in words if len(w) > 1 and w not in stopwords]

    async def _classify_by_llm(self, query: str, keywords: List[str]) -> Dict[str, Any]:
        """使用LLM进行意图分类

        Args:
            query: 用户查询文本
            keywords: 已提取的关键词

        Returns:
            分类结果字典

        Raises:
            Exception: LLM调用失败
        """
        # 构建意图分类提示词
        intent_list = "\n".join(
            [f"{k} - {v}" for k, v in self.INTENT_TYPES.items()]
        )

        prompt = f"""请判断用户问题的意图类别。

可选类别：
{intent_list}

用户问题：{query}
提取的关键词：{', '.join(keywords) if keywords else '无'}

请按以下JSON格式输出，不要输出其他内容：
{{"intent_type": "意图类别代码", "confidence": 0.95, "sub_intent": "子意图描述或null"}}"""

        messages = [
            {"role": "system", "content": "你是一个意图识别专家，请准确判断用户问题的意图类别。只输出JSON格式结果。"},
            {"role": "user", "content": prompt},
        ]

        response = await self.llm_service.chat_cached(
            messages, temperature=0.1, max_tokens=256
        )

        # 解析LLM返回的JSON
        result = self._parse_llm_response(response)

        # 验证意图类型是否合法
        if result["intent_type"] not in self.INTENT_TYPES:
            logger.warning(
                f"LLM返回了未知的意图类型: {result['intent_type']}，降级为Other"
            )
            result["intent_type"] = "Other"
            result["confidence"] = 0.3

        result["keywords"] = keywords
        return result

    def _parse_llm_response(self, response: str) -> Dict[str, Any]:
        """解析LLM返回的意图分类结果

        Args:
            response: LLM返回的文本

        Returns:
            解析后的分类结果字典
        """
        # 尝试提取JSON部分（LLM可能返回额外文本）
        response = response.strip()

        # 移除可能的markdown代码块标记
        if response.startswith("```"):
            lines = response.split("\n")
            # 去掉首尾的```行
            lines = [l for l in lines if not l.strip().startswith("```")]
            response = "\n".join(lines).strip()

        # 尝试找到JSON对象
        start_idx = response.find("{")
        end_idx = response.rfind("}")

        if start_idx != -1 and end_idx != -1 and end_idx > start_idx:
            json_str = response[start_idx:end_idx + 1]
            try:
                parsed = json.loads(json_str)
                return {
                    "intent_type": parsed.get("intent_type", "Other"),
                    "confidence": float(parsed.get("confidence", 0.5)),
                    "sub_intent": parsed.get("sub_intent"),
                }
            except (json.JSONDecodeError, ValueError) as e:
                logger.warning(f"LLM返回的JSON解析失败: {e}, 原始内容: {json_str}")

        # JSON解析失败，尝试从文本中提取意图类型
        for intent_type in self.INTENT_TYPES:
            if intent_type in response:
                return {
                    "intent_type": intent_type,
                    "confidence": 0.5,
                    "sub_intent": None,
                }

        # 完全无法解析
        logger.warning(f"无法解析LLM意图分类结果: {response}")
        return {
            "intent_type": "Other",
            "confidence": 0.3,
            "sub_intent": None,
        }
