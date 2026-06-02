package com.petschool.service;

import com.petschool.entity.SurgeryRecord;
import java.util.List;

public interface SurgeryRecordService {
    int add(SurgeryRecord record);
    List<SurgeryRecord> getByPetId(Long petId);
}
