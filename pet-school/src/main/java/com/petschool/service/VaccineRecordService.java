package com.petschool.service;

import com.petschool.entity.VaccineRecord;
import java.util.List;

public interface VaccineRecordService {
    int add(VaccineRecord record);
    List<VaccineRecord> getByPetId(Long petId);
    List<VaccineRecord> getUpcoming(Long userId, int days);
    List<VaccineRecord> getExpired(Long userId);
    List<VaccineRecord> listAll();
    int update(VaccineRecord record);
    int deleteById(Long id);
}
