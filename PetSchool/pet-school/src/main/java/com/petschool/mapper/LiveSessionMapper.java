package com.petschool.mapper;

import com.petschool.entity.LiveSession;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface LiveSessionMapper {
    int insert(LiveSession session);
    List<LiveSession> selectAll();
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);
    int updateEndTime(@Param("id") Long id);
}
