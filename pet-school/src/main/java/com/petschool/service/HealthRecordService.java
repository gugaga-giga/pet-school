package com.petschool.service;

import com.petschool.entity.HealthRecord;
import java.util.List;

public interface HealthRecordService {
    int add(HealthRecord record);
    List<HealthRecord> getByPetId(Long petId);
    HealthRecord getLatestByPetId(Long petId);
}
