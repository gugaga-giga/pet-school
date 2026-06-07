"""Qwen3大语言模型服务封装，支持普通对话、流式输出、上下文注入"""

import hashlib
import json
from collections import OrderedDict
from typing import AsyncGenerator, Dict, List, Optional

from loguru import logger
from openai import AsyncOpenAI
from tenacity import retry, retry_if_exception_type, stop_after_attempt, wait_exponential

from app.llm.prompts.templates import PromptTemplates


class LRUCache:
    """简单的LRU缓存实现，支持异步场景"""

    def __init__(self, maxsize: int = 128) -> None:
        self.maxsize = maxsize
        self._cache: OrderedDict[str, str] = OrderedDict()

    def get(self, key: str) -> Optional[str]:
        if key in self._cache:
            self._cache.move_to_end(key)
            return self._cache[key]
        return None

    def put(self, key: str, value: str) -> None:
        if key in self._cache:
            self._cache.move_to_end(key)
        self._cache[key] = value
        if len(self._cache) > self.maxsize:
            self._cache.popitem(last=False)

    def clear(self) -> None:
        self._cache.clear()


class LLMService:
    """Qwen3大语言模型服务，封装OpenAI兼容接口调用"""

    def __init__(
        self,
        api_key: str,
        base_url: str,
        model_name: str = "qwen3",
    ) -> None:
        """初始化LLM服务

        Args:
            api_key: API密钥
            base_url: API基础URL（兼容OpenAI接口）
            model_name: 模型名称，默认qwen3
        """
        self.client = AsyncOpenAI(api_key=api_key, base_url=base_url)
        self.model_name = model_name
        # 简单LRU缓存，缓存最近128次调用的结果
        self._cache = LRUCache(maxsize=128)

    # ==================== 核心对话方法 ====================

    @retry(
        retry=retry_if_exception_type((ConnectionError, TimeoutError)),
        stop=stop_after_attempt(3),
        wait=wait_exponential(multiplier=1, min=2, max=10),
        before_sleep=lambda retry_state: logger.warning(
            f"LLM调用失败，第{retry_state.attempt_number}次重试，"
            f"等待{retry_state.next_action.sleep}秒..."
        ),
    )
    async def chat(
        self,
        messages: List[Dict],
        temperature: float = 0.7,
        max_tokens: int = 2048,
    ) -> str:
        """普通对话

        Args:
            messages: 消息列表，格式为[{"role": "user", "content": "..."}]
            temperature: 生成温度，越高越随机
            max_tokens: 最大生成Token数

        Returns:
            模型生成的文本内容

        Raises:
            ConnectionError: 连接失败
            TimeoutError: 请求超时
        """
        try:
            response = await self.client.chat.completions.create(
                model=self.model_name,
                messages=messages,
                temperature=temperature,
                max_tokens=max_tokens,
            )
            content = response.choices[0].message.content or ""
            logger.debug(f"LLM响应长度: {len(content)}字符")
            return content
        except Exception as e:
            logger.error(f"LLM调用失败: {e}")
            raise

    @retry(
        retry=retry_if_exception_type((ConnectionError, TimeoutError)),
        stop=stop_after_attempt(3),
        wait=wait_exponential(multiplier=1, min=2, max=10),
        before_sleep=lambda retry_state: logger.warning(
            f"LLM流式调用失败，第{retry_state.attempt_number}次重试..."
        ),
    )
    async def chat_stream(
        self,
        messages: List[Dict],
        temperature: float = 0.7,
        max_tokens: int = 2048,
    ) -> AsyncGenerator[str, None]:
        """流式对话，逐token输出

        Args:
            messages: 消息列表
            temperature: 生成温度
            max_tokens: 最大生成Token数

        Yields:
            逐token的文本片段
        """
        try:
            stream = await self.client.chat.completions.create(
                model=self.model_name,
                messages=messages,
                temperature=temperature,
                max_tokens=max_tokens,
                stream=True,
            )
            async for chunk in stream:
                if chunk.choices and chunk.choices[0].delta.content:
                    yield chunk.choices[0].delta.content
        except Exception as e:
            logger.error(f"LLM流式调用失败: {e}")
            raise

    # ==================== 带上下文的对话 ====================

    async def chat_with_context(
        self,
        query: str,
        context: str,
        history: Optional[List[Dict]] = None,
        system_prompt: Optional[str] = None,
        temperature: float = 0.7,
    ) -> str:
        """带知识库上下文的对话

        Args:
            query: 用户问题
            context: 检索到的知识库上下文
            history: 历史对话消息列表
            system_prompt: 自定义系统提示词
            temperature: 生成温度

        Returns:
            模型生成的回答
        """
        # 压缩过长的上下文
        compressed_context = self.compress_context(context)
        # 构建消息列表
        messages = self.build_messages(
            query=query,
            context=compressed_context,
            history=history,
            system_prompt=system_prompt,
        )
        return await self.chat(messages, temperature=temperature)

    async def chat_stream_with_context(
        self,
        query: str,
        context: str,
        history: Optional[List[Dict]] = None,
        system_prompt: Optional[str] = None,
        temperature: float = 0.7,
    ) -> AsyncGenerator[str, None]:
        """带知识库上下文的流式对话

        Args:
            query: 用户问题
            context: 检索到的知识库上下文
            history: 历史对话消息列表
            system_prompt: 自定义系统提示词
            temperature: 生成温度

        Yields:
            逐token的文本片段
        """
        # 压缩过长的上下文
        compressed_context = self.compress_context(context)
        # 构建消息列表
        messages = self.build_messages(
            query=query,
            context=compressed_context,
            history=history,
            system_prompt=system_prompt,
        )
        async for token in self.chat_stream(messages, temperature=temperature):
            yield token

    # ==================== 缓存对话（相同输入返回缓存结果） ====================

    async def chat_cached(
        self,
        messages: List[Dict],
        temperature: float = 0.7,
        max_tokens: int = 2048,
    ) -> str:
        """带缓存的普通对话，相同输入直接返回缓存结果

        适用于意图识别、FAQ匹配等确定性较高的场景，
        避免重复调用LLM造成浪费

        Args:
            messages: 消息列表
            temperature: 生成温度
            max_tokens: 最大生成Token数

        Returns:
            模型生成的文本内容
        """
        cache_key = self._make_cache_key(messages, temperature, max_tokens)

        # 检查缓存
        cached_result = self._cache.get(cache_key)
        if cached_result is not None:
            logger.debug("LLM缓存命中")
            return cached_result

        # 缓存未命中，调用LLM
        result = await self.chat(messages, temperature=temperature, max_tokens=max_tokens)

        # 写入缓存
        self._cache.put(cache_key, result)

        return result

    # ==================== 上下文处理 ====================

    def compress_context(self, context: str, max_tokens: int = 3000) -> str:
        """上下文压缩：当上下文过长时按token数截断

        使用简单的字符数估算（中文约1.5字符/token，英文约4字符/token），
        取保守估计约2字符/token进行截断

        Args:
            context: 原始上下文文本
            max_tokens: 最大允许的Token数

        Returns:
            压缩后的上下文文本
        """
        if not context:
            return ""

        # 保守估算：约2字符/token
        max_chars = max_tokens * 2

        if len(context) <= max_chars:
            return context

        # 截断并添加省略提示
        truncated = context[:max_chars]
        # 尝试在最后一个句号处截断，保持语义完整
        last_period = max(
            truncated.rfind("。"),
            truncated.rfind("！"),
            truncated.rfind("？"),
            truncated.rfind("."),
        )
        if last_period > max_chars * 0.8:
            truncated = truncated[: last_period + 1]

        truncated += "\n\n[注：参考资料过长，已截断部分内容]"
        logger.info(f"上下文压缩: {len(context)} -> {len(truncated)} 字符")
        return truncated

    # ==================== 消息构建 ====================

    def build_messages(
        self,
        query: str,
        context: Optional[str] = None,
        history: Optional[List[Dict]] = None,
        system_prompt: Optional[str] = None,
    ) -> List[Dict]:
        """构建消息列表

        按照OpenAI消息格式构建，顺序为：system -> history -> user

        Args:
            query: 用户问题
            context: 知识库上下文
            history: 历史对话消息
            system_prompt: 自定义系统提示词

        Returns:
            构建好的消息列表
        """
        messages: List[Dict] = []

        # 1. 系统提示词
        if system_prompt:
            sys_content = system_prompt
        elif context:
            # 有上下文时使用RAG模板
            sys_content = PromptTemplates.format_template(
                "RAG_QA_TEMPLATE", context=context
            )
        else:
            # 无上下文时使用默认售前客服提示词
            sys_content = PromptTemplates.SYSTEM_PROMPT

        messages.append({"role": "system", "content": sys_content})

        # 2. 历史消息（限制最近10轮，避免过长）
        if history:
            # 保留最近的10轮对话（20条消息）
            recent_history = history[-20:]
            messages.extend(recent_history)

        # 3. 当前用户问题
        messages.append({"role": "user", "content": query})

        return messages

    # ==================== 工具方法 ====================

    @staticmethod
    def _make_cache_key(
        messages: List[Dict],
        temperature: float,
        max_tokens: int,
    ) -> str:
        """生成缓存键

        Args:
            messages: 消息列表
            temperature: 生成温度
            max_tokens: 最大Token数

        Returns:
            缓存键字符串
        """
        key_data = json.dumps(
            {"messages": messages, "temperature": temperature, "max_tokens": max_tokens},
            ensure_ascii=False,
            sort_keys=True,
        )
        return hashlib.md5(key_data.encode("utf-8")).hexdigest()
