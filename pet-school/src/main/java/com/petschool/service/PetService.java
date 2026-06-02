package com.petschool.service;

import com.petschool.entity.Pet;
import java.util.List;

public interface PetService {
    int add(Pet pet);
    Pet getById(Long id);
    List<Pet> getByUserId(Long userId);
}
