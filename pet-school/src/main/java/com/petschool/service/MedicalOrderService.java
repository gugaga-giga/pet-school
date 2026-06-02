package com.petschool.service;

import com.petschool.entity.MedicalOrder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface MedicalOrderService {
    MedicalOrder create(Long userId, Long petId, Long doctorId, Long departmentId, LocalDateTime appointmentTime, String symptom);
    List<MedicalOrder> getByUserId(Long userId);
    Map<String, Object> page(String keyword, Integer status, int page, int pageSize);
    int updateStatus(Long id, Integer status);
    int cancel(Long id, Long userId);
    MedicalOrder getById(Long id);
}
