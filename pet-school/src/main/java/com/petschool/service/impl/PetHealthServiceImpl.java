package com.petschool.service.impl;

import com.petschool.entity.HealthRule;
import com.petschool.entity.Pet;
import com.petschool.entity.PetHealthRecord;
import com.petschool.entity.User;
import com.petschool.mapper.HealthRuleMapper;
import com.petschool.mapper.PetHealthRecordMapper;
import com.petschool.mapper.PetMapper;
import com.petschool.mapper.UserMapper;
import com.petschool.service.PetHealthService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PetHealthServiceImpl implements PetHealthService {

    private final PetHealthRecordMapper healthRecordMapper;
    private final HealthRuleMapper healthRuleMapper;
    private final PetMapper petMapper;
    private final UserMapper userMapper;

    public PetHealthServiceImpl(PetHealthRecordMapper healthRecordMapper,
                                HealthRuleMapper healthRuleMapper,
                                PetMapper petMapper,
                                UserMapper userMapper) {
        this.healthRecordMapper = healthRecordMapper;
        this.healthRuleMapper = healthRuleMapper;
        this.petMapper = petMapper;
        this.userMapper = userMapper;
    }

    @Override
    public Map<String, Object> page(String keyword, Integer riskLevel, String startDate, String endDate, int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        List<PetHealthRecord> list = healthRecordMapper.selectPage(keyword, riskLevel, startDate, endDate, offset, pageSize);
        int total = healthRecordMapper.selectPageCount(keyword, riskLevel, startDate, endDate);
        int totalPages = (total + pageSize - 1) / pageSize;
        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);
        result.put("page", page);
        result.put("pageSize", pageSize);
        result.put("totalPages", totalPages);
        return result;
    }

    @Override
    public PetHealthRecord getById(Long id) {
        return healthRecordMapper.selectById(id);
    }

    @Override
    public List<PetHealthRecord> getByPetId(Long petId) {
        return healthRecordMapper.selectByPetId(petId);
    }

    @Override
    public PetHealthRecord getLatestByPetId(Long petId) {
        return healthRecordMapper.selectLatestByPetId(petId);
    }

    @Override
    public List<PetHealthRecord> getTrendByPetId(Long petId, int limit) {
        return healthRecordMapper.selectTrendByPetId(petId, limit);
    }

    @Override
    public PetHealthRecord create(PetHealthRecord record) {
        Pet pet = petMapper.selectById(record.getPetId());
        if (pet != null) {
            record.setPetName(pet.getName());
            record.setPetType(detectPetType(pet.getBreed()));
            record.setAge(pet.getAge());
            User user = userMapper.selectById(pet.getUserId());
            if (user != null) {
                record.setOwnerName(user.getUsername());
            }
        } else {
            if (record.getPetType() == null) {
                record.setPetType("dog");
            }
        }
        int score = calculateHealthScore(record);
        record.setHealthScore(score);
        record.setRiskLevel(determineRiskLevel(score));
        record.setAiAdvice(generateAiAdvice(record));
        record.setCreateTime(LocalDateTime.now());
        record.setUpdateTime(LocalDateTime.now());
        healthRecordMapper.insert(record);
        return record;
    }

    @Override
    public PetHealthRecord update(PetHealthRecord record) {
        if (record.getPetId() != null) {
            Pet pet = petMapper.selectById(record.getPetId());
            if (pet != null) {
                record.setPetName(pet.getName());
                record.setPetType(detectPetType(pet.getBreed()));
                record.setAge(pet.getAge());
                User user = userMapper.selectById(pet.getUserId());
                if (user != null) {
                    record.setOwnerName(user.getUsername());
                }
            }
        }
        int score = calculateHealthScore(record);
        record.setHealthScore(score);
        record.setRiskLevel(determineRiskLevel(score));
        record.setAiAdvice(generateAiAdvice(record));
        record.setUpdateTime(LocalDateTime.now());
        healthRecordMapper.update(record);
        return record;
    }

    @Override
    public int deleteById(Long id) {
        return healthRecordMapper.deleteById(id);
    }

    @Override
    public int calculateHealthScore(PetHealthRecord record) {
        int score = 0;
        String petType = record.getPetType() != null ? record.getPetType() : "dog";
        List<HealthRule> rules = healthRuleMapper.selectByPetType(petType);
        Map<String, HealthRule> ruleMap = rules.stream()
                .collect(Collectors.toMap(HealthRule::getIndicatorName, r -> r, (a, b) -> a));

        HealthRule tempRule = ruleMap.get("temperature");
        if (tempRule != null && record.getTemperature() != null) {
            BigDecimal temp = record.getTemperature();
            if (isInRange(temp, tempRule.getMinValue(), tempRule.getMaxValue())) {
                score += 20;
            } else {
                BigDecimal deviation = temp.compareTo(tempRule.getMinValue()) < 0
                        ? tempRule.getMinValue().subtract(temp)
                        : temp.subtract(tempRule.getMaxValue());
                if (deviation.compareTo(BigDecimal.valueOf(0.5)) < 0) {
                    score += 12;
                } else {
                    score += 4;
                }
            }
        } else {
            score += 10;
        }

        if (record.getWeight() != null && record.getWeight().compareTo(BigDecimal.ZERO) > 0) {
            score += 20;
        } else {
            score += 10;
        }

        HealthRule hrRule = ruleMap.get("heartRate");
        if (hrRule != null && record.getHeartRate() != null) {
            if (isInRange(BigDecimal.valueOf(record.getHeartRate()), hrRule.getMinValue(), hrRule.getMaxValue())) {
                score += 15;
            } else {
                score += 6;
            }
        } else {
            score += 8;
        }

        HealthRule respRule = ruleMap.get("respirationRate");
        if (respRule != null && record.getRespirationRate() != null) {
            if (isInRange(BigDecimal.valueOf(record.getRespirationRate()), respRule.getMinValue(), respRule.getMaxValue())) {
                score += 15;
            } else {
                score += 6;
            }
        } else {
            score += 8;
        }

        if (record.getAppetite() != null) {
            switch (record.getAppetite()) {
                case 2: score += 10; break;
                case 1: score += 5; break;
                default: score += 2; break;
            }
        }

        if (record.getMentalStatus() != null) {
            switch (record.getMentalStatus()) {
                case 2: score += 10; break;
                case 1: score += 5; break;
                default: score += 2; break;
            }
        }

        if (record.getVaccineStatus() != null) {
            switch (record.getVaccineStatus()) {
                case 2: score += 5; break;
                case 1: score += 3; break;
                default: score += 1; break;
            }
        }

        if (record.getDewormingStatus() != null) {
            switch (record.getDewormingStatus()) {
                case 1: score += 5; break;
                default: score += 1; break;
            }
        }

        return Math.min(score, 100);
    }

    @Override
    public int determineRiskLevel(int score) {
        if (score >= 90) return 0;
        if (score >= 75) return 1;
        if (score >= 60) return 2;
        return 3;
    }

    @Override
    public String generateAiAdvice(PetHealthRecord record) {
        List<String> advices = new ArrayList<>();
        String petType = record.getPetType() != null ? record.getPetType() : "dog";
        List<HealthRule> rules = healthRuleMapper.selectByPetType(petType);
        Map<String, HealthRule> ruleMap = rules.stream()
                .collect(Collectors.toMap(HealthRule::getIndicatorName, r -> r, (a, b) -> a));

        if (record.getTemperature() != null) {
            HealthRule rule = ruleMap.get("temperature");
            if (rule != null && !isInRange(record.getTemperature(), rule.getMinValue(), rule.getMaxValue())) {
                advices.add(rule.getWarningText() + "，" + rule.getSuggestion());
            }
        }

        if (record.getHeartRate() != null) {
            HealthRule rule = ruleMap.get("heartRate");
            if (rule != null && !isInRange(BigDecimal.valueOf(record.getHeartRate()), rule.getMinValue(), rule.getMaxValue())) {
                advices.add(rule.getWarningText() + "，" + rule.getSuggestion());
            }
        }

        if (record.getRespirationRate() != null) {
            HealthRule rule = ruleMap.get("respirationRate");
            if (rule != null && !isInRange(BigDecimal.valueOf(record.getRespirationRate()), rule.getMinValue(), rule.getMaxValue())) {
                advices.add(rule.getWarningText() + "，" + rule.getSuggestion());
            }
        }

        if (record.getAppetite() != null) {
            switch (record.getAppetite()) {
                case 0: advices.add("食欲差，建议观察饮食规律，若持续不进食请就医。"); break;
                case 1: advices.add("食欲一般，建议关注饮食变化，适当调整食物种类。"); break;
                default: break;
            }
        }

        if (record.getMentalStatus() != null) {
            switch (record.getMentalStatus()) {
                case 0: advices.add("精神萎靡，可能存在不适，建议减少活动量并观察。"); break;
                case 1: advices.add("精神状态一般，建议增加互动观察是否有改善。"); break;
                default: break;
            }
        }

        if (record.getHairCondition() != null) {
            switch (record.getHairCondition()) {
                case 0: advices.add("毛发状况差，建议检查皮肤健康并改善营养摄入。"); break;
                case 1: advices.add("毛发状况一般，建议定期梳理并补充适量营养。"); break;
                default: break;
            }
        }

        if (record.getFecesStatus() != null) {
            switch (record.getFecesStatus()) {
                case 0: advices.add("排便异常，建议观察粪便颜色和形态，若有血便或持续腹泻请就医。"); break;
                case 1: advices.add("排便一般，建议关注饮食结构，适当调整。"); break;
                default: break;
            }
        }

        if (record.getVaccineStatus() != null && record.getVaccineStatus() == 0) {
            advices.add("疫苗未接种，建议尽快补种核心疫苗。");
        }

        if (record.getDewormingStatus() != null && record.getDewormingStatus() == 0) {
            advices.add("未进行驱虫，建议定期驱虫以预防寄生虫。");
        }

        if (advices.isEmpty()) {
            return "各项指标正常，宠物状态良好。继续保持定期体检和科学喂养。";
        }

        return String.join("\n", advices);
    }

    @Override
    public List<PetHealthRecord> getByUserId(Long userId) {
        List<Pet> pets = petMapper.selectByUserId(userId);
        List<PetHealthRecord> records = new ArrayList<>();
        for (Pet pet : pets) {
            PetHealthRecord latest = healthRecordMapper.selectLatestByPetId(pet.getId());
            if (latest != null) {
                records.add(latest);
            }
        }
        return records;
    }

    @Override
    public List<HealthRule> getRules() {
        return healthRuleMapper.selectAll();
    }

    @Override
    public List<HealthRule> getRulesByPetType(String petType) {
        return healthRuleMapper.selectByPetType(petType);
    }

    @Override
    public int createRule(HealthRule rule) {
        rule.setCreateTime(LocalDateTime.now());
        return healthRuleMapper.insert(rule);
    }

    @Override
    public int updateRule(HealthRule rule) {
        return healthRuleMapper.update(rule);
    }

    @Override
    public int deleteRule(Long id) {
        return healthRuleMapper.deleteById(id);
    }

    private String detectPetType(String breed) {
        if (breed == null) return "dog";
        String lower = breed.toLowerCase();
        if (lower.contains("猫") || lower.contains("cat")) return "cat";
        return "dog";
    }

    private boolean isInRange(BigDecimal value, BigDecimal min, BigDecimal max) {
        if (value == null || min == null || max == null) return false;
        return value.compareTo(min) >= 0 && value.compareTo(max) <= 0;
    }
}
