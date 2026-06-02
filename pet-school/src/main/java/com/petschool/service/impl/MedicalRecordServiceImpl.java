package com.petschool.service.impl;

import com.petschool.entity.MedicalOrder;
import com.petschool.entity.MedicalRecord;
import com.petschool.mapper.MedicalOrderMapper;
import com.petschool.mapper.MedicalRecordMapper;
import com.petschool.service.MedicalRecordService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MedicalRecordServiceImpl implements MedicalRecordService {

    private final MedicalRecordMapper medicalRecordMapper;
    private final MedicalOrderMapper medicalOrderMapper;

    public MedicalRecordServiceImpl(MedicalRecordMapper medicalRecordMapper,
                                    MedicalOrderMapper medicalOrderMapper) {
        this.medicalRecordMapper = medicalRecordMapper;
        this.medicalOrderMapper = medicalOrderMapper;
    }

    @Override
    @Transactional
    public MedicalRecord create(Long medicalOrderId, String chiefComplaint, String physicalExam, String diagnosis, String medicalAdvice, String medication, String remark) {
        MedicalOrder order = medicalOrderMapper.selectById(medicalOrderId);
        if (order == null) {
            throw new RuntimeException("医疗订单不存在");
        }

        MedicalRecord record = new MedicalRecord();
        record.setMedicalOrderId(medicalOrderId);
        record.setPetId(order.getPetId());
        record.setDoctorName(order.getDoctorName());
        record.setChiefComplaint(chiefComplaint);
        record.setPhysicalExam(physicalExam);
        record.setDiagnosis(diagnosis);
        record.setMedicalAdvice(medicalAdvice);
        record.setMedication(medication);
        record.setRemark(remark);
        record.setVisitTime(LocalDateTime.now());

        medicalRecordMapper.insert(record);
        medicalOrderMapper.updateStatus(medicalOrderId, 3);

        return record;
    }

    @Override
    public MedicalRecord getByMedicalOrderId(Long orderId) {
        return medicalRecordMapper.selectByMedicalOrderId(orderId);
    }

    @Override
    public List<MedicalRecord> getByPetId(Long petId) {
        return medicalRecordMapper.selectByPetId(petId);
    }

    @Override
    public Map<String, Object> page(String keyword, int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        List<MedicalRecord> list = medicalRecordMapper.selectPage(keyword, offset, pageSize);
        int total = medicalRecordMapper.selectPageCount(keyword);
        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);
        result.put("page", page);
        result.put("pageSize", pageSize);
        result.put("totalPages", (total + pageSize - 1) / pageSize);
        return result;
    }

    @Override
    public int update(MedicalRecord record) {
        return medicalRecordMapper.update(record);
    }

    @Override
    public int deleteById(Long id) {
        return medicalRecordMapper.deleteById(id);
    }
}
