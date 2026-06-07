package com.petschool.service.impl;

import com.petschool.entity.HealthRecord;
import com.petschool.mapper.HealthRecordMapper;
import com.petschool.service.HealthRecordService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HealthRecordServiceImpl implements HealthRecordService {

    private final HealthRecordMapper healthRecordMapper;

    public HealthRecordServiceImpl(HealthRecordMapper healthRecordMapper) {
        this.healthRecordMapper = healthRecordMapper;
    }

    @Override
    public int add(HealthRecord record) {
        return healthRecordMapper.insert(record);
    }

    @Override
    public List<HealthRecord> getByPetId(Long petId) {
        return healthRecordMapper.selectByPetId(petId);
    }

    @Override
    public HealthRecord getLatestByPetId(Long petId) {
        return healthRecordMapper.selectLatestByPetId(petId);
    }
}
