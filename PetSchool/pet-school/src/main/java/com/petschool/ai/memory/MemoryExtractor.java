package com.petschool.ai.memory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petschool.ai.config.AiConfig;
import com.petschool.ai.entity.ChatMessage;
import com.petschool.ai.entity.MemoryProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 长期记忆提取器：从对话中提取关键信息存入memory_profile
 */
@Component
public class MemoryExtractor {

    private static final Logger log = LoggerFactory.getLogger(MemoryExtractor.class);

    private final String apiKey;
    private final String baseUrl;
    private final String modelName;
    private final ObjectMapper objectMapper;

    public MemoryExtractor(AiConfig aiConfig) {
        this.apiKey = aiConfig.getApiKey();
        this.baseUrl = aiConfig.getBaseUrl();
        this.modelName = aiConfig.getModelName();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 从对话中提取长期记忆
     * @param userId 用户ID
     * @param messages 对话消息列表
     * @return 提取的记忆列表
     */
    public List<MemoryProfile> extract(Long userId, List<ChatMessage> messages) {
        if (messages == null || messages.size() < 2) {
            return Collections.emptyList();
        }

        try {
            // 取最近几轮对话
            int start = Math.max(0, messages.size() - 6);
            List<ChatMessage> recentMessages = messages.subList(start, messages.size());

            // 构建对话文本
            StringBuilder conversation = new StringBuilder();
            for (ChatMessage msg : recentMessages) {
                conversation.append(msg.getRole()).append(": ").append(msg.getContent()).append("\n");
            }

            // 调用LLM提取记忆
            String extractionPrompt = "请从以下对话中提取用户的长期偏好和关键信息。" +
                    "只提取明确表达的信息，不要推测。" +
                    "返回JSON数组格式，每项包含key和value字段。" +
                    "key可选值: pet_preference(宠物偏好), training_need(训练需求), health_concern(健康关注), " +
                    "schedule_preference(时间偏好), budget_range(预算范围), other(其他)。" +
                    "如果没有可提取的信息，返回空数组[]。\n\n" +
                    "对话内容：\n" + conversation;

            String llmResponse = callLLM(extractionPrompt);
            return parseMemoryResponse(userId, llmResponse);

        } catch (Exception e) {
            log.error("提取长期记忆失败: userId={}, error={}", userId, e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * 调用LLM提取记忆
     */
    private String callLLM(String prompt) {
        try {
            URL url = new URL(baseUrl + "/chat/completions");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + apiKey);
            conn.setDoOutput(true);
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(30000);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", modelName);
            requestBody.put("messages", List.of(
                    Map.of("role", "user", "content", prompt)
            ));
            requestBody.put("stream", false);
            requestBody.put("temperature", 0.1);

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
            return "[]";
        } catch (Exception e) {
            log.error("LLM调用失败: {}", e.getMessage());
            return "[]";
        }
    }

    /**
     * 解析LLM返回的记忆信息
     */
    private List<MemoryProfile> parseMemoryResponse(Long userId, String response) {
        List<MemoryProfile> memories = new ArrayList<>();
        try {
            // 尝试提取JSON数组
            String jsonPart = response.trim();
            int startIdx = jsonPart.indexOf('[');
            int endIdx = jsonPart.lastIndexOf(']');
            if (startIdx >= 0 && endIdx > startIdx) {
                jsonPart = jsonPart.substring(startIdx, endIdx + 1);
            }

            List<Map<String, String>> items = objectMapper.readValue(jsonPart,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Map.class));

            for (Map<String, String> item : items) {
                String key = item.get("key");
                String value = item.get("value");
                if (key != null && value != null && !value.isBlank()) {
                    MemoryProfile profile = new MemoryProfile();
                    profile.setUserId(userId);
                    profile.setKey(key);
                    profile.setValue(value);
                    profile.setSource("chat");
                    memories.add(profile);
                }
            }
        } catch (Exception e) {
            log.debug("解析记忆响应失败: {}", e.getMessage());
        }
        return memories;
    }
}
