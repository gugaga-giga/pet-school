package com.petschool.mapper;

import com.petschool.entity.Room;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RoomMapper {
    List<Room> selectByTypeId(@Param("typeId") Long typeId);
    Room selectById(@Param("id") Long id);
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);
    int insert(Room room);
    int update(Room room);
    int deleteById(@Param("id") Long id);
    int countOccupiedByTypeId(@Param("typeId") Long typeId);
}
