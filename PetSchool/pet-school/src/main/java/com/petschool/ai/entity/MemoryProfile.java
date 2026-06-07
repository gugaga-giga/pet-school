package com.petschool.ai.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户长期记忆档案实体
 */
public class MemoryProfile implements Serializable {

    private Long id;
    private Long userId;
    private String key; // 记忆键，如 pet_preference, common_question
    private String value; // 记忆值
    private String source; // 来源，如 chat/manual/system
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }
    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
