package com.petschool.service.impl;

import com.petschool.entity.Pet;
import com.petschool.mapper.PetMapper;
import com.petschool.service.PetService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PetServiceImpl implements PetService {

    private final PetMapper petMapper;

    public PetServiceImpl(PetMapper petMapper) {
        this.petMapper = petMapper;
    }

    @Override
    public int add(Pet pet) {
        return petMapper.insert(pet);
    }

    @Override
    public Pet getById(Long id) {
        return petMapper.selectById(id);
    }

    @Override
    public List<Pet> getByUserId(Long userId) {
        return petMapper.selectByUserId(userId);
    }
}
