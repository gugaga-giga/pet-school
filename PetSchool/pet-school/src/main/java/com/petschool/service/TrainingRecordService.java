package com.petschool.service;

import com.petschool.entity.TrainingRecord;
import java.util.List;

public interface TrainingRecordService {
    int add(TrainingRecord record);
    List<TrainingRecord> getByPetId(Long petId);
}
