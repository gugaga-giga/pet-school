package com.petschool.ai.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petschool.ai.config.AiConfig;
import com.petschool.ai.context.CourseContext;
import com.petschool.ai.context.PetContext;
import com.petschool.ai.context.UserContext;
import com.petschool.ai.entity.ChatMessage;
import com.petschool.ai.entity.ChatSession;
import com.petschool.ai.entity.MemoryProfile;
import com.petschool.ai.memory.ContextCompressor;
import com.petschool.ai.memory.MemoryExtractor;
import com.petschool.ai.service.*;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * AI聊天核心服务实现
 * 完整流程：获取会话→构建上下文→意图识别→智能路由→RAG检索→构建Prompt→调用LLM→保存消息→提取记忆
 */
@Service
public class AiChatServiceImpl implements AiChatService {

    private static final Logger log = LoggerFactory.getLogger(AiChatServiceImpl.class);

    private final AiConfig aiConfig;
    private final MemoryService memoryService;
    private final ContextService contextService;
    private final PromptService promptService;
    private final IntentClassifier intentClassifier;
    private final RouterService routerService;
    private final RAGService ragService;
    private final MemoryExtractor memoryExtractor;
    private final ContextCompressor contextCompressor;
    private final ObjectMapper objectMapper;

    public AiChatServiceImpl(AiConfig aiConfig,
                             MemoryService memoryService,
                             ContextService contextService,
                             PromptService promptService,
                             IntentClassifier intentClassifier,
                             RouterService routerService,
                             RAGService ragService,
                             MemoryExtractor memoryExtractor,
                             ContextCompressor contextCompressor) {
        this.aiConfig = aiConfig;
        this.memoryService = memoryService;
        this.contextService = contextService;
        this.promptService = promptService;
        this.intentClassifier = intentClassifier;
        this.routerService = routerService;
        this.ragService = ragService;
        this.memoryExtractor = memoryExtractor;
        this.contextCompressor = contextCompressor;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public Flux<String> chatStream(HttpServletRequest request, String message, Long sessionId) {
        return Flux.create(sink -> {
            try {
                // 1. 获取/创建会话
                ChatSession session;
                if (sessionId != null) {
                    session = memoryService.getSession(sessionId);
                    if (session == null) {
                        sink.next(formatSSE("error", "会话不存在"));
                        sink.complete();
                        return;
                    }
                } else {
                    // 用消息前20字作为标题
                    String title = message.length() > 20 ? message.substring(0, 20) + "..." : message;
                    session = memoryService.createSession(getUserId(request), title);
                }

                // 发送会话ID
                sink.next(formatSSE("session", Map.of("session_id", session.getId())));

                // 2. 构建用户上下文
                UserContext userContext = contextService.buildUserContext(request);

                // 3. 构建宠物上下文
                List<PetContext> petContexts = contextService.buildPetContext(userContext.getUserId());

                // 4. 构建课程上下文
                List<CourseContext> courseContexts = contextService.buildCourseContext(userContext.getUserId());

                // 5. 加载历史记忆
                List<MemoryProfile> memories = memoryService.getMemoryProfiles(userContext.getUserId());

                // 6. 保存用户消息
                memoryService.saveMessage(session.getId(), "user", message, null, null, estimateTokens(message));

                // 7. 意图识别
                String intent = intentClassifier.classify(message);
                log.info("意图识别: query={}, intent={}", message, intent);

                // 8. 智能路由
                RouterService.RouteResult routeResult = routerService.route(intent, message);

                // 9. RAG检索（如果需要）
                String ragContext = "";
                final List<Map<String, Object>> ragResults;
                if (routeResult.needRAG() && routeResult.kbIds() != null && !routeResult.kbIds().isEmpty()) {
                    ragResults = ragService.retrieve(message, routeResult.kbIds(), 5);
                    if (!ragResults.isEmpty()) {
                        ragContext = ragResults.stream()
                                .map(r -> String.valueOf(r.getOrDefault("content", "")))
                                .collect(Collectors.joining("\n\n"));
                    }
                } else {
                    ragResults = Collections.emptyList();
                }

                // 10. 构建Prompt
                String systemPrompt = promptService.buildSystemPrompt(userContext, petContexts, courseContexts, memories);

                // 构建消息列表
                List<ChatMessage> historyMessages = memoryService.getMessages(session.getId());
                List<Map<String, String>> messages = new ArrayList<>();
                messages.add(Map.of("role", "system", "content", systemPrompt));

                // 上下文压缩：历史消息过长时保留最近N条
                List<ChatMessage> compressedHistory = contextCompressor.compress(historyMessages, aiConfig.getMaxContextLength());
                for (ChatMessage msg : compressedHistory) {
                    messages.add(Map.of("role", msg.getRole(), "content", msg.getContent()));
                }

                // 如果有RAG上下文，在最后一条用户消息前插入RAG信息
                if (!ragContext.isEmpty()) {
                    String ragPrompt = promptService.buildRAGPrompt(message, ragContext);
                    // 替换最后一条用户消息为RAG增强版本
                    if (!messages.isEmpty()) {
                        Map<String, String> lastMsg = messages.get(messages.size() - 1);
                        if ("user".equals(lastMsg.get("role"))) {
                            messages.set(messages.size() - 1, Map.of("role", "user", "content", ragPrompt));
                        }
                    }
                }

                // 11. 调用Qwen3生成答案（SSE流式）
                StringBuilder fullResponse = new StringBuilder();
                callLLMStream(messages, new LLMStreamCallback() {
                    @Override
                    public void onContent(String content) {
                        fullResponse.append(content);
                        sink.next(formatSSE("content", Map.of("content", content)));
                    }

                    @Override
                    public void onError(String error) {
                        sink.next(formatSSE("error", error));
                    }

                    @Override
                    public void onComplete() {
                        // 12. 保存助手消息
                        String sourcesJson = null;
                        if (!ragResults.isEmpty()) {
                            try {
                                sourcesJson = objectMapper.writeValueAsString(ragResults);
                            } catch (Exception e) {
                                log.warn("序列化RAG结果失败", e);
                            }
                        }
                        memoryService.saveMessage(session.getId(), "assistant", fullResponse.toString(),
                                sourcesJson, intent, estimateTokens(fullResponse.toString()));

                        // 13. 提取长期记忆（异步，不影响响应）
                        try {
                            List<ChatMessage> recentMessages = memoryService.getMessages(session.getId());
                            List<MemoryProfile> extractedMemories = memoryExtractor.extract(userContext.getUserId(), recentMessages);
                            for (MemoryProfile mp : extractedMemories) {
                                memoryService.saveMemoryProfile(userContext.getUserId(), mp.getKey(), mp.getValue(), mp.getSource());
                            }
                        } catch (Exception e) {
                            log.warn("提取长期记忆失败", e);
                        }

                        sink.next(formatSSE("done", Map.of("session_id", session.getId())));
                        sink.complete();
                    }
                });

            } catch (Exception e) {
                log.error("聊天处理异常", e);
                sink.next(formatSSE("error", "处理失败: " + e.getMessage()));
                sink.complete();
            }
        });
    }

    @Override
    public Map<String, Object> chatSync(HttpServletRequest request, String message, Long sessionId) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 1. 获取/创建会话
            ChatSession session;
            if (sessionId != null) {
                session = memoryService.getSession(sessionId);
                if (session == null) {
                    result.put("code", 404);
                    result.put("message", "会话不存在");
                    return result;
                }
            } else {
                String title = message.length() > 20 ? message.substring(0, 20) + "..." : message;
                session = memoryService.createSession(getUserId(request), title);
            }

            // 2-5. 构建上下文
            UserContext userContext = contextService.buildUserContext(request);
            List<PetContext> petContexts = contextService.buildPetContext(userContext.getUserId());
            List<CourseContext> courseContexts = contextService.buildCourseContext(userContext.getUserId());
            List<MemoryProfile> memories = memoryService.getMemoryProfiles(userContext.getUserId());

            // 6. 保存用户消息
            memoryService.saveMessage(session.getId(), "user", message, null, null, estimateTokens(message));

            // 7. 意图识别
            String intent = intentClassifier.classify(message);

            // 8. 智能路由
            RouterService.RouteResult routeResult = routerService.route(intent, message);

            // 9. RAG检索
            String ragContext = "";
            List<Map<String, Object>> ragResults = Collections.emptyList();
            if (routeResult.needRAG() && routeResult.kbIds() != null && !routeResult.kbIds().isEmpty()) {
                ragResults = ragService.retrieve(message, routeResult.kbIds(), 5);
                if (!ragResults.isEmpty()) {
                    ragContext = ragResults.stream()
                            .map(r -> String.valueOf(r.getOrDefault("content", "")))
                            .collect(Collectors.joining("\n\n"));
                }
            }

            // 10. 构建Prompt
            String systemPrompt = promptService.buildSystemPrompt(userContext, petContexts, courseContexts, memories);
            List<ChatMessage> historyMessages = memoryService.getMessages(session.getId());
            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of("role", "system", "content", systemPrompt));

            List<ChatMessage> compressedHistory = contextCompressor.compress(historyMessages, aiConfig.getMaxContextLength());
            for (ChatMessage msg : compressedHistory) {
                messages.add(Map.of("role", msg.getRole(), "content", msg.getContent()));
            }

            if (!ragContext.isEmpty()) {
                String ragPrompt = promptService.buildRAGPrompt(message, ragContext);
                if (!messages.isEmpty()) {
                    Map<String, String> lastMsg = messages.get(messages.size() - 1);
                    if ("user".equals(lastMsg.get("role"))) {
                        messages.set(messages.size() - 1, Map.of("role", "user", "content", ragPrompt));
                    }
                }
            }

            // 11. 同步调用LLM
            String response = callLLMSync(messages);

            // 12. 保存助手消息
            String sourcesJson = null;
            if (!ragResults.isEmpty()) {
                try {
                    sourcesJson = objectMapper.writeValueAsString(ragResults);
                } catch (Exception e) {
                    log.warn("序列化RAG结果失败", e);
                }
            }
            memoryService.saveMessage(session.getId(), "assistant", response, sourcesJson, intent, estimateTokens(response));

            // 13. 提取长期记忆
            try {
                List<ChatMessage> recentMessages = memoryService.getMessages(session.getId());
                List<MemoryProfile> extractedMemories = memoryExtractor.extract(userContext.getUserId(), recentMessages);
                for (MemoryProfile mp : extractedMemories) {
                    memoryService.saveMemoryProfile(userContext.getUserId(), mp.getKey(), mp.getValue(), mp.getSource());
                }
            } catch (Exception e) {
                log.warn("提取长期记忆失败", e);
            }

            result.put("code", 200);
            result.put("message", "成功");
            result.put("data", Map.of(
                    "sessionId", session.getId(),
                    "content", response,
                    "intent", intent
            ));

        } catch (Exception e) {
            log.error("同步聊天处理异常", e);
            result.put("code", 500);
            result.put("message", "处理失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 从request获取userId
     */
    private Long getUserId(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return userId != null ? userId : 0L;
    }

    /**
     * 流式调用LLM（OpenAI兼容API）
     */
    private void callLLMStream(List<Map<String, String>> messages, LLMStreamCallback callback) {
        try {
            URL url = new URL(aiConfig.getBaseUrl() + "/chat/completions");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + aiConfig.getApiKey());
            conn.setDoOutput(true);
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(120000);

            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", aiConfig.getModelName());
            requestBody.put("messages", messages);
            requestBody.put("stream", true);

            String jsonBody = objectMapper.writeValueAsString(requestBody);
            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonBody.getBytes(StandardCharsets.UTF_8));
            }

            // 读取SSE流
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("data: ")) {
                        String data = line.substring(6).trim();
                        if ("[DONE]".equals(data)) {
                            callback.onComplete();
                            return;
                        }
                        try {
                            Map<String, Object> chunk = objectMapper.readValue(data, Map.class);
                            @SuppressWarnings("unchecked")
                            List<Map<String, Object>> choices = (List<Map<String, Object>>) chunk.get("choices");
                            if (choices != null && !choices.isEmpty()) {
                                Map<String, Object> delta = (Map<String, Object>) choices.get(0).get("delta");
                                if (delta != null && delta.containsKey("content")) {
                                    String content = (String) delta.get("content");
                                    if (content != null) {
                                        callback.onContent(content);
                                    }
                                }
                            }
                        } catch (Exception e) {
                            log.debug("解析SSE chunk失败: {}", data);
                        }
                    }
                }
            }
            callback.onComplete();
        } catch (Exception e) {
            log.error("LLM流式调用失败", e);
            callback.onError("AI服务调用失败: " + e.getMessage());
        }
    }

    /**
     * 同步调用LLM
     */
    private String callLLMSync(List<Map<String, String>> messages) {
        try {
            URL url = new URL(aiConfig.getBaseUrl() + "/chat/completions");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + aiConfig.getApiKey());
            conn.setDoOutput(true);
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(120000);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", aiConfig.getModelName());
            requestBody.put("messages", messages);
            requestBody.put("stream", false);

            String jsonBody = objectMapper.writeValueAsString(requestBody);
            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonBody.getBytes(StandardCharsets.UTF_8));
            }

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                Map<String, Object> result = objectMapper.readValue(response.toString(), Map.class);
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> choices = (List<Map<String, Object>>) result.get("choices");
                if (choices != null && !choices.isEmpty()) {
                    Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                    if (message != null) {
                        return (String) message.get("content");
                    }
                }
            }
            return "抱歉，AI服务暂时无法回复。";
        } catch (Exception e) {
            log.error("LLM同步调用失败", e);
            return "抱歉，AI服务暂时无法回复：" + e.getMessage();
        }
    }

    /**
     * 格式化SSE消息
     */
    private String formatSSE(String type, Object data) {
        try {
            Map<String, Object> msg = new HashMap<>();
            msg.put("type", type);
            if (data instanceof String) {
                msg.put("content", data);
            } else {
                msg.putAll((Map<String, ?>) data);
            }
            return "data: " + objectMapper.writeValueAsString(msg) + "\n\n";
        } catch (Exception e) {
            return "data: {\"type\":\"error\",\"content\":\"格式化消息失败\"}\n\n";
        }
    }

    /**
     * 估算token数量（中文约1.5字/token）
     */
    private int estimateTokens(String text) {
        if (text == null) return 0;
        return (int) (text.length() * 1.5);
    }

    /**
     * LLM流式回调接口
     */
    private interface LLMStreamCallback {
        void onContent(String content);
        void onError(String error);
        void onComplete();
    }
}
