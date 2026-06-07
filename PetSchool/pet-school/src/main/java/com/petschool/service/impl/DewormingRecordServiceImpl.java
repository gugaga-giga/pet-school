package com.petschool.service.impl;

import com.petschool.entity.DewormingRecord;
import com.petschool.mapper.DewormingRecordMapper;
import com.petschool.service.DewormingRecordService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DewormingRecordServiceImpl implements DewormingRecordService {

    private final DewormingRecordMapper dewormingRecordMapper;

    public DewormingRecordServiceImpl(DewormingRecordMapper dewormingRecordMapper) {
        this.dewormingRecordMapper = dewormingRecordMapper;
    }

    @Override
    public int add(DewormingRecord record) {
        return dewormingRecordMapper.insert(record);
    }

    @Override
    public List<DewormingRecord> getByPetId(Long petId) {
        return dewormingRecordMapper.selectByPetId(petId);
    }

    @Override
    public List<DewormingRecord> getUpcoming(Long userId, int days) {
        return dewormingRecordMapper.selectUpcoming(userId, days);
    }

    @Override
    public List<DewormingRecord> getExpired(Long userId) {
        return dewormingRecordMapper.selectExpired(userId);
    }

    @Override
    public List<DewormingRecord> listAll() {
        return dewormingRecordMapper.selectAll();
    }

    @Override
    public int update(DewormingRecord record) {
        return dewormingRecordMapper.update(record);
    }

    @Override
    public int deleteById(Long id) {
        return dewormingRecordMapper.deleteById(id);
    }
}
