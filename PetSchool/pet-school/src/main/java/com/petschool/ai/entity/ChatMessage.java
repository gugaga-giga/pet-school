package com.petschool.ai.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 聊天消息实体
 */
public class ChatMessage implements Serializable {

    private Long id;
    private Long sessionId;
    private String role; // user/assistant/system
    private String content;
    private String sources; // JSON字符串，RAG检索来源
    private String intentType; // 意图类型
    private Integer tokenCount;
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getSources() { return sources; }
    public void setSources(String sources) { this.sources = sources; }
    public String getIntentType() { return intentType; }
    public void setIntentType(String intentType) { this.intentType = intentType; }
    public Integer getTokenCount() { return tokenCount; }
    public void setTokenCount(Integer tokenCount) { this.tokenCount = tokenCount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
