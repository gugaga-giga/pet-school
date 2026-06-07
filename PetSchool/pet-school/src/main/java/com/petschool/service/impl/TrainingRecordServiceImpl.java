package com.petschool.service.impl;

import com.petschool.entity.TrainingRecord;
import com.petschool.mapper.TrainingRecordMapper;
import com.petschool.service.TrainingRecordService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TrainingRecordServiceImpl implements TrainingRecordService {

    private final TrainingRecordMapper trainingRecordMapper;

    public TrainingRecordServiceImpl(TrainingRecordMapper trainingRecordMapper) {
        this.trainingRecordMapper = trainingRecordMapper;
    }

    @Override
    public int add(TrainingRecord record) {
        return trainingRecordMapper.insert(record);
    }

    @Override
    public List<TrainingRecord> getByPetId(Long petId) {
        return trainingRecordMapper.selectByPetId(petId);
    }
}
