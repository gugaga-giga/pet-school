"""推荐策略配置 - 意图到策略的映射与权重定义"""

from typing import Dict


class RecommendStrategy:
    """推荐策略配置

    定义意图类型到推荐策略的映射关系，
    以及不同策略下的评分权重配置。
    """

    # 意图到策略的映射
    INTENT_STRATEGY_MAP: Dict[str, str] = {
        "product_inquiry": "feature_match",
        "price_inquiry": "price_range",
        "purchase_advice": "comprehensive",
        "comparison": "comparison",
        "feature_inquiry": "feature_match",
        "after_sales": "service_match",
        "complaint": "service_match",
        "general": "comprehensive",
    }

    # 推荐权重配置（默认权重）
    FEATURE_WEIGHT: float = 0.4
    PRICE_WEIGHT: float = 0.3
    RATING_WEIGHT: float = 0.2
    POPULARITY_WEIGHT: float = 0.1

    # 不同策略的权重配置
    STRATEGY_WEIGHTS: Dict[str, Dict[str, float]] = {
        # 特性匹配策略：侧重产品功能特性匹配
        "feature_match": {
            "feature": 0.5,
            "price": 0.2,
            "rating": 0.2,
            "popularity": 0.1,
        },
        # 价格区间策略：侧重价格匹配
        "price_range": {
            "feature": 0.2,
            "price": 0.5,
            "rating": 0.2,
            "popularity": 0.1,
        },
        # 综合推荐策略：均衡考量各维度
        "comprehensive": {
            "feature": 0.4,
            "price": 0.3,
            "rating": 0.2,
            "popularity": 0.1,
        },
        # 对比推荐策略：侧重特性差异
        "comparison": {
            "feature": 0.5,
            "price": 0.3,
            "rating": 0.1,
            "popularity": 0.1,
        },
        # 服务匹配策略：侧重售后和口碑
        "service_match": {
            "feature": 0.2,
            "price": 0.1,
            "rating": 0.4,
            "popularity": 0.3,
        },
    }

    @classmethod
    def get_strategy(cls, intent_type: str) -> str:
        """获取推荐策略

        根据意图类型获取对应的推荐策略名称。

        Args:
            intent_type: 意图类型

        Returns:
            推荐策略名称，未知意图类型返回"comprehensive"
        """
        return cls.INTENT_STRATEGY_MAP.get(intent_type, "comprehensive")

    @classmethod
    def get_weights(cls, strategy: str) -> Dict[str, float]:
        """获取策略权重

        根据推荐策略获取各维度的评分权重。

        Args:
            strategy: 推荐策略名称

        Returns:
            权重字典，包含 feature/price/rating/popularity 四个维度
        """
        return cls.STRATEGY_WEIGHTS.get(strategy, cls.STRATEGY_WEIGHTS["comprehensive"])
