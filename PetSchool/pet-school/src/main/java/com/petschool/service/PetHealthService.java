package com.petschool.service;

import com.petschool.entity.HealthRule;
import com.petschool.entity.PetHealthRecord;

import java.util.List;
import java.util.Map;

public interface PetHealthService {
    Map<String, Object> page(String keyword, Integer riskLevel, String startDate, String endDate, int page, int pageSize);
    PetHealthRecord getById(Long id);
    List<PetHealthRecord> getByPetId(Long petId);
    PetHealthRecord getLatestByPetId(Long petId);
    List<PetHealthRecord> getTrendByPetId(Long petId, int limit);
    PetHealthRecord create(PetHealthRecord record);
    PetHealthRecord update(PetHealthRecord record);
    int deleteById(Long id);
    int calculateHealthScore(PetHealthRecord record);
    int determineRiskLevel(int score);
    String generateAiAdvice(PetHealthRecord record);
    List<HealthRule> getRules();
    List<HealthRule> getRulesByPetType(String petType);
    int createRule(HealthRule rule);
    int updateRule(HealthRule rule);
    int deleteRule(Long id);
    List<PetHealthRecord> getByUserId(Long userId);
}
