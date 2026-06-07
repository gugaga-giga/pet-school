package com.petschool.mapper;

import com.petschool.entity.Message;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MessageMapper {
    int insert(Message message);
    List<Message> selectByUserId(@Param("userId") Long userId);
    int updateRead(@Param("id") Long id);
}
