package com.petschool.ai.mapper;

import com.petschool.ai.entity.ChatMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 聊天消息Mapper
 */
@Mapper
public interface ChatMessageMapper {

    int insert(ChatMessage message);

    ChatMessage selectById(@Param("id") Long id);

    List<ChatMessage> selectBySessionId(@Param("sessionId") Long sessionId);

    int deleteBySessionId(@Param("sessionId") Long sessionId);
}
