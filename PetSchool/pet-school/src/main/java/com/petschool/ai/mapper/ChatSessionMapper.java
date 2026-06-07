package com.petschool.ai.mapper;

import com.petschool.ai.entity.ChatSession;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 聊天会话Mapper
 */
@Mapper
public interface ChatSessionMapper {

    int insert(ChatSession session);

    ChatSession selectById(@Param("id") Long id);

    List<ChatSession> selectByUserId(@Param("userId") Long userId);

    int update(ChatSession session);

    int deleteById(@Param("id") Long id);
}
