package com.petschool.service.impl;

import com.petschool.entity.Room;
import com.petschool.mapper.RoomMapper;
import com.petschool.service.RoomService;
import com.petschool.service.RoomTypeService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoomServiceImpl implements RoomService {

    private final RoomMapper roomMapper;
    private final RoomTypeService roomTypeService;

    public RoomServiceImpl(RoomMapper roomMapper, RoomTypeService roomTypeService) {
        this.roomMapper = roomMapper;
        this.roomTypeService = roomTypeService;
    }

    @Override
    public List<Room> getByTypeId(Long typeId) {
        return roomMapper.selectByTypeId(typeId);
    }

    @Override
    public Room getById(Long id) {
        return roomMapper.selectById(id);
    }

    @Override
    public int updateStatus(Long id, Integer status) {
        return roomMapper.updateStatus(id, status);
    }

    @Override
    public int add(Room room) {
        int rows = roomMapper.insert(room);
        if (rows > 0 && room.getTypeId() != null) {
            roomTypeService.incrementCapacity(room.getTypeId());
        }
        return rows;
    }

    @Override
    public int update(Room room) {
        return roomMapper.update(room);
    }

    @Override
    public int deleteById(Long id) {
        Room room = roomMapper.selectById(id);
        int rows = roomMapper.deleteById(id);
        if (rows > 0 && room != null && room.getTypeId() != null) {
            roomTypeService.decrementCapacity(room.getTypeId());
        }
        return rows;
    }

    @Override
    public int countOccupiedByTypeId(Long typeId) {
        return roomMapper.countOccupiedByTypeId(typeId);
    }
}
