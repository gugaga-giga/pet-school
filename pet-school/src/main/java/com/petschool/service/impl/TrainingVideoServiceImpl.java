package com.petschool.service.impl;

import com.petschool.entity.TrainingVideo;
import com.petschool.mapper.TrainingVideoMapper;
import com.petschool.service.TrainingVideoService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TrainingVideoServiceImpl implements TrainingVideoService {

    private final TrainingVideoMapper trainingVideoMapper;

    public TrainingVideoServiceImpl(TrainingVideoMapper trainingVideoMapper) {
        this.trainingVideoMapper = trainingVideoMapper;
    }

    @Override
    public int add(TrainingVideo video) {
        return trainingVideoMapper.insert(video);
    }

    @Override
    public List<TrainingVideo> getByRecordId(Long recordId) {
        return trainingVideoMapper.selectByRecordId(recordId);
    }
}
