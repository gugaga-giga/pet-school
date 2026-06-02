package com.petschool.service.impl;

import com.petschool.entity.HealthRecord;
import com.petschool.entity.Pet;
import com.petschool.entity.VaccineRecord;
import com.petschool.mapper.HealthRecordMapper;
import com.petschool.mapper.PetMapper;
import com.petschool.mapper.VaccineRecordMapper;
import com.petschool.service.AiService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AiServiceImpl implements AiService {

    private static final BigDecimal TEMP_THRESHOLD = new BigDecimal("39");

    private final HealthRecordMapper healthRecordMapper;
    private final PetMapper petMapper;
    private final VaccineRecordMapper vaccineRecordMapper;

    public AiServiceImpl(HealthRecordMapper healthRecordMapper, PetMapper petMapper, VaccineRecordMapper vaccineRecordMapper) {
        this.healthRecordMapper = healthRecordMapper;
        this.petMapper = petMapper;
        this.vaccineRecordMapper = vaccineRecordMapper;
    }

    @Override
    public Map<String, Object> healthWarning(Long petId) {
        Map<String, Object> result = new HashMap<>();
        Pet pet = petMapper.selectById(petId);
        if (pet == null) {
            result.put("error", "宠物不存在");
            return result;
        }
        HealthRecord hr = healthRecordMapper.selectLatestByPetId(petId);
        result.put("petName", pet.getName());
        List<String> warnings = new ArrayList<>();
        if (hr != null) {
            result.put("temperature", hr.getTemperature());
            result.put("appetite", hr.getAppetite());
            result.put("spirit", hr.getSpirit());
            if (hr.getTemperature() != null && hr.getTemperature().compareTo(TEMP_THRESHOLD) > 0) {
                warnings.add("体温异常：当前" + hr.getTemperature() + "℃，超过39℃阈值，请及时就医");
            }
            if (hr.getAppetite() != null && hr.getAppetite() < 2) {
                warnings.add("食欲下降，可能存在健康问题，建议检查");
            }
            if (hr.getSpirit() != null && hr.getSpirit() < 2) {
                warnings.add("精神状态低落，建议关注行为变化");
            }
        }
        if (warnings.isEmpty()) {
            warnings.add("各项指标正常，宠物状态良好");
        }
        result.put("warnings", warnings);
        result.put("hasWarning", warnings.size() > 0 && !"各项指标正常，宠物状态良好".equals(warnings.get(0)));
        return result;
    }

    @Override
    public Map<String, Object> courseRecommend(Long petId) {
        Map<String, Object> result = new HashMap<>();
        Pet pet = petMapper.selectById(petId);
        if (pet == null) {
            result.put("error", "宠物不存在");
            return result;
        }
        result.put("petName", pet.getName());
        result.put("age", pet.getAge());
        if (pet.getAge() != null && pet.getAge() < 2) {
            result.put("recommendation", "基础训练");
            result.put("reason", "年龄" + pet.getAge() + "岁，建议从基础训练开始");
        } else {
            result.put("recommendation", "进阶训练");
            result.put("reason", "年龄" + pet.getAge() + "岁，可参加进阶训练课程");
        }
        return result;
    }

    @Override
    public Map<String, Object> vaccineReminder(Long petId) {
        Map<String, Object> result = new HashMap<>();
        Pet pet = petMapper.selectById(petId);
        if (pet == null) {
            result.put("error", "宠物不存在");
            return result;
        }
        result.put("petName", pet.getName());
        List<VaccineRecord> records = vaccineRecordMapper.selectByPetId(petId);
        List<Map<String, Object>> reminders = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (VaccineRecord vr : records) {
            if (vr.getNextDueDate() != null) {
                long daysUntil = ChronoUnit.DAYS.between(today, vr.getNextDueDate());
                if (daysUntil <= 30 && daysUntil >= 0) {
                    Map<String, Object> r = new HashMap<>();
                    r.put("vaccineName", vr.getVaccineName());
                    r.put("nextDate", vr.getNextDueDate());
                    r.put("daysUntil", daysUntil);
                    r.put("urgent", daysUntil <= 7);
                    reminders.add(r);
                } else if (daysUntil < 0) {
                    Map<String, Object> r = new HashMap<>();
                    r.put("vaccineName", vr.getVaccineName());
                    r.put("nextDate", vr.getNextDueDate());
                    r.put("daysUntil", daysUntil);
                    r.put("overdue", true);
                    reminders.add(r);
                }
            }
        }
        result.put("reminders", reminders);
        result.put("needAttention", !reminders.isEmpty());
        return result;
    }
}
