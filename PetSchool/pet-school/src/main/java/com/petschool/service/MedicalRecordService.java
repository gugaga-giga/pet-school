package com.petschool.service;

import com.petschool.entity.MedicalRecord;

import java.util.List;
import java.util.Map;

public interface MedicalRecordService {
    MedicalRecord create(Long medicalOrderId, String chiefComplaint, String physicalExam, String diagnosis, String medicalAdvice, String medication, String remark);
    MedicalRecord getByMedicalOrderId(Long orderId);
    List<MedicalRecord> getByPetId(Long petId);
    Map<String, Object> page(String keyword, int page, int pageSize);
    int update(MedicalRecord record);
    int deleteById(Long id);
}
