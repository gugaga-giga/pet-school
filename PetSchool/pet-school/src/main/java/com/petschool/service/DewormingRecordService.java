package com.petschool.service;

import com.petschool.entity.DewormingRecord;
import java.util.List;

public interface DewormingRecordService {
    int add(DewormingRecord record);
    List<DewormingRecord> getByPetId(Long petId);
    List<DewormingRecord> getUpcoming(Long userId, int days);
    List<DewormingRecord> getExpired(Long userId);
    List<DewormingRecord> listAll();
    int update(DewormingRecord record);
    int deleteById(Long id);
}
