package com.petschool.service;

import com.petschool.entity.Room;
import java.util.List;

public interface RoomService {
    List<Room> getByTypeId(Long typeId);
    Room getById(Long id);
    int updateStatus(Long id, Integer status);
    int add(Room room);
    int update(Room room);
    int deleteById(Long id);
    int countOccupiedByTypeId(Long typeId);
}
