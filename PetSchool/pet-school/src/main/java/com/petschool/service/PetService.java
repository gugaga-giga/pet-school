package com.petschool.service;

import com.petschool.entity.Pet;
import java.util.List;
import java.util.Map;

public interface PetService {
    int add(Pet pet);
    Pet getById(Long id);
    List<Pet> getByUserId(Long userId);
    int countByUserId(Long userId);
    int update(Pet pet);
    int deleteById(Long id);
    Map<String, Object> page(String keyword, String petType, Integer status, int page, int pageSize);
    Pet getDetailById(Long id);
    Map<String, Object> getStats();
    void checkCanDelete(Long id);
}
