package com.petschool.ai.service;

import com.petschool.ai.entity.ChatMessage;
import com.petschool.ai.entity.ChatSession;
import com.petschool.ai.entity.MemoryProfile;

import java.util.List;

/**
 * 会话记忆管理服务接口
 */
public interface MemoryService {

    /**
     * 创建会话
     */
    ChatSession createSession(Long userId, String title);

    /**
     * 获取用户会话列表
     */
    List<ChatSession> getSessions(Long userId);

    /**
     * 获取会话详情
     */
    ChatSession getSession(Long sessionId);

    /**
     * 保存消息
     */
    ChatMessage saveMessage(Long sessionId, String role, String content, String sources, String intentType, Integer tokenCount);

    /**
     * 获取会话消息列表
     */
    List<ChatMessage> getMessages(Long sessionId);

    /**
     * 删除会话及其消息
     */
    boolean deleteSession(Long sessionId);

    /**
     * 获取用户记忆档案
     */
    List<MemoryProfile> getMemoryProfiles(Long userId);

    /**
     * 保存用户记忆档案
     */
    boolean saveMemoryProfile(Long userId, String key, String value, String source);
}
