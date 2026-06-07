package com.petschool.ai.memory;

import com.petschool.ai.entity.ChatMessage;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 上下文压缩器：当历史消息过长时，保留最近N条 + 摘要
 */
@Component
public class ContextCompressor {

    /** 保留的最近消息条数 */
    private static final int KEEP_RECENT_COUNT = 10;

    /** 单条消息最大字符数 */
    private static final int MAX_MESSAGE_LENGTH = 500;

    /**
     * 压缩历史消息，使其不超过最大上下文长度
     * @param messages 原始消息列表
     * @param maxContextLength 最大上下文长度（字符数）
     * @return 压缩后的消息列表
     */
    public List<ChatMessage> compress(List<ChatMessage> messages, int maxContextLength) {
        if (messages == null || messages.isEmpty()) {
            return messages;
        }

        // 计算总长度
        int totalLength = messages.stream()
                .mapToInt(m -> m.getContent() != null ? m.getContent().length() : 0)
                .sum();

        // 如果总长度未超限，直接返回
        if (totalLength <= maxContextLength) {
            return messages;
        }

        // 截取最近N条消息
        int start = Math.max(0, messages.size() - KEEP_RECENT_COUNT);
        List<ChatMessage> recentMessages = messages.subList(start, messages.size());

        // 对每条消息进行截断
        List<ChatMessage> compressed = new ArrayList<>();
        for (ChatMessage msg : recentMessages) {
            ChatMessage compressedMsg = new ChatMessage();
            compressedMsg.setId(msg.getId());
            compressedMsg.setSessionId(msg.getSessionId());
            compressedMsg.setRole(msg.getRole());
            compressedMsg.setIntentType(msg.getIntentType());
            compressedMsg.setSources(msg.getSources());
            compressedMsg.setTokenCount(msg.getTokenCount());
            compressedMsg.setCreatedAt(msg.getCreatedAt());

            // 截断过长的消息内容
            if (msg.getContent() != null && msg.getContent().length() > MAX_MESSAGE_LENGTH) {
                compressedMsg.setContent(msg.getContent().substring(0, MAX_MESSAGE_LENGTH) + "...[内容已截断]");
            } else {
                compressedMsg.setContent(msg.getContent());
            }
            compressed.add(compressedMsg);
        }

        return compressed;
    }
}
