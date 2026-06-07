"""Prompt模板管理，定义售前客服场景下的各类提示词模板"""

from typing import Any, Dict


class PromptTemplates:
    """Prompt模板管理，集中管理所有提示词模板"""

    # ==================== 售前客服系统提示词 ====================
    SYSTEM_PROMPT = """你是一个专业的售前客服助手，名叫"小智"。你的职责是：
1. 热情友好地回答客户关于产品和服务的问题
2. 根据客户需求推荐合适的产品
3. 提供准确的产品规格、价格和功能信息
4. 对比不同产品的优缺点，帮助客户做出决策
5. 回答常见问题（FAQ）

回答要求：
- 回答要准确、专业、有礼貌
- 如果不确定信息，请诚实告知客户，不要编造
- 适当引导客户了解产品优势
- 使用简洁清晰的语言"""

    # ==================== RAG问答模板 ====================
    RAG_QA_TEMPLATE = """你是一个专业的售前客服助手，名叫"小智"。请基于以下参考资料回答用户问题。

要求：
1. 优先使用参考资料中的信息回答
2. 如果参考资料中没有相关信息，请根据你的知识回答，但要说明"以上信息仅供参考"
3. 不要编造不存在的产品信息或价格
4. 回答要准确、专业、有礼貌
5. 如果用户问题与参考资料完全无关，请礼貌引导用户回到产品咨询话题

参考资料：
{context}

请根据以上参考资料回答用户问题。"""

    # ==================== 意图识别模板 ====================
    INTENT_CLASSIFY_TEMPLATE = """请判断用户问题的意图类别。

可选类别：
1. product_inquiry - 产品咨询：询问产品功能、规格、参数等
2. price_inquiry - 价格咨询：询问产品价格、优惠、折扣等
3. comparison - 产品对比：对比不同产品的优缺点
4. recommendation - 产品推荐：根据需求推荐合适的产品
5. faq - 常见问题：售后、物流、退换货等常见问题
6. complaint - 投诉建议：客户不满或提出建议
7. greeting - 问候闲聊：打招呼、寒暄
8. other - 其他：不属于以上类别的问题

用户问题：{query}

请只输出意图类别代码（如 product_inquiry），不要输出其他内容。"""

    # ==================== 产品推荐模板 ====================
    PRODUCT_RECOMMEND_TEMPLATE = """基于用户需求和以下产品信息，为用户推荐最合适的产品。

用户需求：{query}

可选产品信息：
{product_info}

推荐要求：
1. 根据用户需求推荐1-3款最合适的产品
2. 说明推荐理由，突出产品优势
3. 对比推荐产品的差异，帮助用户选择
4. 如果没有完全匹配的产品，推荐最接近的替代方案
5. 给出明确的购买建议"""

    # ==================== FAQ匹配模板 ====================
    FAQ_MATCH_TEMPLATE = """判断以下问题是否为常见问题，如果是，请给出标准回答。

用户问题：{query}

常见问题列表：
{faq_list}

请按以下格式回答：
- 如果是常见问题：MATCHED|问题编号|标准回答
- 如果不是常见问题：NOT_MATCHED"""

    # ==================== 竞品对比模板 ====================
    COMPARE_TEMPLATE = """请对比以下产品，帮助用户做出选择。

用户需求：{query}

产品A：
{product_a}

产品B：
{product_b}

对比要求：
1. 从功能、性能、价格、适用场景等维度进行对比
2. 用表格形式展示对比结果
3. 给出各自的优缺点
4. 根据用户需求给出推荐建议"""

    # ==================== 模板注册表 ====================
    _TEMPLATES: Dict[str, str] = {}

    @classmethod
    def _ensure_registry(cls) -> None:
        """确保模板注册表已初始化"""
        if not cls._TEMPLATES:
            cls._TEMPLATES = {
                "SYSTEM_PROMPT": cls.SYSTEM_PROMPT,
                "RAG_QA_TEMPLATE": cls.RAG_QA_TEMPLATE,
                "INTENT_CLASSIFY_TEMPLATE": cls.INTENT_CLASSIFY_TEMPLATE,
                "PRODUCT_RECOMMEND_TEMPLATE": cls.PRODUCT_RECOMMEND_TEMPLATE,
                "FAQ_MATCH_TEMPLATE": cls.FAQ_MATCH_TEMPLATE,
                "COMPARE_TEMPLATE": cls.COMPARE_TEMPLATE,
            }

    @classmethod
    def get_template(cls, template_name: str) -> str:
        """获取模板原始内容

        Args:
            template_name: 模板名称，如"RAG_QA_TEMPLATE"

        Returns:
            模板内容字符串

        Raises:
            KeyError: 模板不存在
        """
        cls._ensure_registry()
        if template_name not in cls._TEMPLATES:
            raise KeyError(
                f"模板 '{template_name}' 不存在，可用模板: {list(cls._TEMPLATES.keys())}"
            )
        return cls._TEMPLATES[template_name]

    @classmethod
    def format_template(cls, template_name: str, **kwargs: Any) -> str:
        """格式化模板，将变量填充到模板中

        Args:
            template_name: 模板名称
            **kwargs: 模板变量键值对

        Returns:
            格式化后的模板内容

        Raises:
            KeyError: 模板不存在
            ValueError: 缺少必要的模板变量
        """
        template = cls.get_template(template_name)
        try:
            return template.format(**kwargs)
        except KeyError as e:
            raise ValueError(
                f"模板 '{template_name}' 缺少变量: {e}，提供的变量: {list(kwargs.keys())}"
            ) from e
