package com.petschool.mapper;

import com.petschool.entity.RoomType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RoomTypeMapper {
    List<RoomType> selectAll();
    RoomType selectById(@Param("id") Long id);
    int insert(RoomType roomType);
    int update(RoomType roomType);
    int deleteById(@Param("id") Long id);
    int decrementCapacity(@Param("id") Long id);
    int incrementCapacity(@Param("id") Long id);
}
