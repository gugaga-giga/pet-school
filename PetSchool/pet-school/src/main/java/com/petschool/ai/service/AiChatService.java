package com.petschool.ai.service;

import com.petschool.ai.context.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import reactor.core.publisher.Flux;

import java.util.Map;

/**
 * AI聊天服务接口
 */
public interface AiChatService {

    /**
     * 流式聊天
     */
    Flux<String> chatStream(HttpServletRequest request, String message, Long sessionId);

    /**
     * 同步聊天
     */
    Map<String, Object> chatSync(HttpServletRequest request, String message, Long sessionId);
}
