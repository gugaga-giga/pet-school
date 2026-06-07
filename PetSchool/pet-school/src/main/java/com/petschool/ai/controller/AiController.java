package com.petschool.ai.controller;

import com.petschool.ai.entity.ChatMessage;
import com.petschool.ai.entity.ChatSession;
import com.petschool.ai.entity.MemoryProfile;
import com.petschool.ai.service.MemoryService;
import com.petschool.ai.service.AiChatService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI聊天控制器
 */
@RestController
@RequestMapping("/ai")
public class AiController {

    private final AiChatService aiChatService;
    private final MemoryService memoryService;

    public AiController(AiChatService aiChatService, MemoryService memoryService) {
        this.aiChatService = aiChatService;
        this.memoryService = memoryService;
    }

    private Map<String, Object> ok(Object data) {
        Map<String, Object> r = new HashMap<>();
        r.put("code", 200);
        r.put("data", data);
        return r;
    }

    private Map<String, Object> ok(String msg, Object data) {
        Map<String, Object> r = new HashMap<>();
        r.put("code", 200);
        r.put("message", msg);
        r.put("data", data);
        return r;
    }

    private Map<String, Object> fail(String msg) {
        Map<String, Object> r = new HashMap<>();
        r.put("code", 500);
        r.put("message", msg);
        return r;
    }

    /**
     * 主聊天接口（SSE流式）
     */
    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chat(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        String message = (String) body.get("message");
        Long sessionId = body.get("sessionId") != null ? Long.valueOf(body.get("sessionId").toString()) : null;
        return aiChatService.chatStream(request, message, sessionId);
    }

    /**
     * 同步聊天接口
     */
    @PostMapping("/chat/sync")
    public Map<String, Object> chatSync(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        String message = (String) body.get("message");
        Long sessionId = body.get("sessionId") != null ? Long.valueOf(body.get("sessionId").toString()) : null;
        return aiChatService.chatSync(request, message, sessionId);
    }

    /**
     * 获取会话列表
     */
    @GetMapping("/sessions")
    public Map<String, Object> sessions(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return fail("未登录");
        }
        List<ChatSession> sessions = memoryService.getSessions(userId);
        return ok(sessions);
    }

    /**
     * 获取会话消息
     */
    @GetMapping("/sessions/{id}/messages")
    public Map<String, Object> messages(@PathVariable Long id) {
        List<ChatMessage> messages = memoryService.getMessages(id);
        return ok(messages);
    }

    /**
     * 删除会话
     */
    @DeleteMapping("/sessions/{id}")
    public Map<String, Object> deleteSession(@PathVariable Long id) {
        boolean success = memoryService.deleteSession(id);
        if (success) {
            return ok("删除成功", null);
        }
        return fail("删除失败");
    }

    /**
     * 获取用户记忆
     */
    @GetMapping("/memory")
    public Map<String, Object> memory(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return fail("未登录");
        }
        List<MemoryProfile> memories = memoryService.getMemoryProfiles(userId);
        return ok(memories);
    }
}
