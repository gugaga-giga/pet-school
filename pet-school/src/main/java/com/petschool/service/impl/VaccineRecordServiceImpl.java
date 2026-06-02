package com.petschool.service.impl;

import com.petschool.entity.VaccineRecord;
import com.petschool.mapper.VaccineRecordMapper;
import com.petschool.service.VaccineRecordService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VaccineRecordServiceImpl implements VaccineRecordService {

    private final VaccineRecordMapper vaccineRecordMapper;

    public VaccineRecordServiceImpl(VaccineRecordMapper vaccineRecordMapper) {
        this.vaccineRecordMapper = vaccineRecordMapper;
    }

    @Override
    public int add(VaccineRecord record) {
        return vaccineRecordMapper.insert(record);
    }

    @Override
    public List<VaccineRecord> getByPetId(Long petId) {
        return vaccineRecordMapper.selectByPetId(petId);
    }

    @Override
    public List<VaccineRecord> getUpcoming(Long userId, int days) {
        return vaccineRecordMapper.selectUpcoming(userId, days);
    }

    @Override
    public List<VaccineRecord> getExpired(Long userId) {
        return vaccineRecordMapper.selectExpired(userId);
    }

    @Override
    public List<VaccineRecord> listAll() {
        return vaccineRecordMapper.selectAll();
    }

    @Override
    public int update(VaccineRecord record) {
        return vaccineRecordMapper.update(record);
    }

    @Override
    public int deleteById(Long id) {
        return vaccineRecordMapper.deleteById(id);
    }
}
