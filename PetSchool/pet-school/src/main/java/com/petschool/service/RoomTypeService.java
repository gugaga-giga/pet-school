package com.petschool.service;

import com.petschool.entity.RoomType;
import java.util.List;

public interface RoomTypeService {
    List<RoomType> listAll();
    RoomType getById(Long id);
    int add(RoomType roomType);
    int update(RoomType roomType);
    int deleteById(Long id);
    int decrementCapacity(Long id);
    int incrementCapacity(Long id);
}
