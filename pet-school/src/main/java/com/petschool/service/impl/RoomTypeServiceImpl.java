package com.petschool.service.impl;

import com.petschool.entity.RoomType;
import com.petschool.mapper.RoomTypeMapper;
import com.petschool.service.RoomTypeService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoomTypeServiceImpl implements RoomTypeService {

    private final RoomTypeMapper roomTypeMapper;

    public RoomTypeServiceImpl(RoomTypeMapper roomTypeMapper) {
        this.roomTypeMapper = roomTypeMapper;
    }

    @Override
    public List<RoomType> listAll() {
        return roomTypeMapper.selectAll();
    }

    @Override
    public RoomType getById(Long id) {
        return roomTypeMapper.selectById(id);
    }

    @Override
    public int add(RoomType roomType) {
        return roomTypeMapper.insert(roomType);
    }

    @Override
    public int update(RoomType roomType) {
        return roomTypeMapper.update(roomType);
    }

    @Override
    public int deleteById(Long id) {
        return roomTypeMapper.deleteById(id);
    }

    @Override
    public int decrementCapacity(Long id) {
        return roomTypeMapper.decrementCapacity(id);
    }

    @Override
    public int incrementCapacity(Long id) {
        return roomTypeMapper.incrementCapacity(id);
    }
}
