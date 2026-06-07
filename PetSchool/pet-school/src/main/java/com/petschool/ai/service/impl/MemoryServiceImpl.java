package com.petschool.ai.service.impl;

import com.petschool.ai.entity.ChatMessage;
import com.petschool.ai.entity.ChatSession;
import com.petschool.ai.entity.MemoryProfile;
import com.petschool.ai.mapper.ChatMessageMapper;
import com.petschool.ai.mapper.ChatSessionMapper;
import com.petschool.ai.mapper.MemoryProfileMapper;
import com.petschool.ai.service.MemoryService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 会话记忆管理服务实现
 */
@Service
public class MemoryServiceImpl implements MemoryService {

    private final ChatSessionMapper chatSessionMapper;
    private final ChatMessageMapper chatMessageMapper;
    private final MemoryProfileMapper memoryProfileMapper;

    public MemoryServiceImpl(ChatSessionMapper chatSessionMapper,
                             ChatMessageMapper chatMessageMapper,
                             MemoryProfileMapper memoryProfileMapper) {
        this.chatSessionMapper = chatSessionMapper;
        this.chatMessageMapper = chatMessageMapper;
        this.memoryProfileMapper = memoryProfileMapper;
    }

    @Override
    public ChatSession createSession(Long userId, String title) {
        ChatSession session = new ChatSession();
        session.setUserId(userId);
        session.setTitle(title != null ? title : "新对话");
        session.setCreatedAt(LocalDateTime.now());
        session.setUpdatedAt(LocalDateTime.now());
        chatSessionMapper.insert(session);
        return session;
    }

    @Override
    public List<ChatSession> getSessions(Long userId) {
        return chatSessionMapper.selectByUserId(userId);
    }

    @Override
    public ChatSession getSession(Long sessionId) {
        return chatSessionMapper.selectById(sessionId);
    }

    @Override
    public ChatMessage saveMessage(Long sessionId, String role, String content,
                                    String sources, String intentType, Integer tokenCount) {
        ChatMessage message = new ChatMessage();
        message.setSessionId(sessionId);
        message.setRole(role);
        message.setContent(content);
        message.setSources(sources);
        message.setIntentType(intentType);
        message.setTokenCount(tokenCount);
        message.setCreatedAt(LocalDateTime.now());
        chatMessageMapper.insert(message);

        // 更新会话时间
        ChatSession session = chatSessionMapper.selectById(sessionId);
        if (session != null) {
            session.setUpdatedAt(LocalDateTime.now());
            chatSessionMapper.update(session);
        }

        return message;
    }

    @Override
    public List<ChatMessage> getMessages(Long sessionId) {
        return chatMessageMapper.selectBySessionId(sessionId);
    }

    @Override
    public boolean deleteSession(Long sessionId) {
        // 先删除会话下的消息
        chatMessageMapper.deleteBySessionId(sessionId);
        return chatSessionMapper.deleteById(sessionId) > 0;
    }

    @Override
    public List<MemoryProfile> getMemoryProfiles(Long userId) {
        return memoryProfileMapper.selectByUserId(userId);
    }

    @Override
    public boolean saveMemoryProfile(Long userId, String key, String value, String source) {
        // 检查是否已存在相同key的记忆
        MemoryProfile existing = memoryProfileMapper.selectByUserIdAndKey(userId, key);
        if (existing != null) {
            existing.setValue(value);
            existing.setSource(source);
            existing.setUpdatedAt(LocalDateTime.now());
            return memoryProfileMapper.update(existing) > 0;
        } else {
            MemoryProfile profile = new MemoryProfile();
            profile.setUserId(userId);
            profile.setKey(key);
            profile.setValue(value);
            profile.setSource(source);
            profile.setCreatedAt(LocalDateTime.now());
            profile.setUpdatedAt(LocalDateTime.now());
            return memoryProfileMapper.insert(profile) > 0;
        }
    }
}
