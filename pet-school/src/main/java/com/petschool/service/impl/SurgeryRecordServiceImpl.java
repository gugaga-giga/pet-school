package com.petschool.service.impl;

import com.petschool.entity.SurgeryRecord;
import com.petschool.mapper.SurgeryRecordMapper;
import com.petschool.service.SurgeryRecordService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SurgeryRecordServiceImpl implements SurgeryRecordService {

    private final SurgeryRecordMapper surgeryRecordMapper;

    public SurgeryRecordServiceImpl(SurgeryRecordMapper surgeryRecordMapper) {
        this.surgeryRecordMapper = surgeryRecordMapper;
    }

    @Override
    public int add(SurgeryRecord record) {
        record.setStatus(0);
        return surgeryRecordMapper.insert(record);
    }

    @Override
    public List<SurgeryRecord> getByPetId(Long petId) {
        return surgeryRecordMapper.selectByPetId(petId);
    }
}
