package com.petschool.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class PetHealthRecord implements Serializable {
    private Long id;
    private Long petId;
    private String petType;
    private Integer age;
    private BigDecimal weight;
    private BigDecimal temperature;
    private Integer heartRate;
    private Integer respirationRate;
    private Integer appetite;
    private Integer mentalStatus;
    private Integer hairCondition;
    private Integer fecesStatus;
    private Integer vaccineStatus;
    private Integer dewormingStatus;
    private Integer healthScore;
    private Integer riskLevel;
    private String aiAdvice;
    private String remark;
    private LocalDate inspectionDate;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    private transient String petName;
    private transient String ownerName;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getPetId() { return petId; }
    public void setPetId(Long petId) { this.petId = petId; }
    public String getPetType() { return petType; }
    public void setPetType(String petType) { this.petType = petType; }
    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }
    public BigDecimal getWeight() { return weight; }
    public void setWeight(BigDecimal weight) { this.weight = weight; }
    public BigDecimal getTemperature() { return temperature; }
    public void setTemperature(BigDecimal temperature) { this.temperature = temperature; }
    public Integer getHeartRate() { return heartRate; }
    public void setHeartRate(Integer heartRate) { this.heartRate = heartRate; }
    public Integer getRespirationRate() { return respirationRate; }
    public void setRespirationRate(Integer respirationRate) { this.respirationRate = respirationRate; }
    public Integer getAppetite() { return appetite; }
    public void setAppetite(Integer appetite) { this.appetite = appetite; }
    public Integer getMentalStatus() { return mentalStatus; }
    public void setMentalStatus(Integer mentalStatus) { this.mentalStatus = mentalStatus; }
    public Integer getHairCondition() { return hairCondition; }
    public void setHairCondition(Integer hairCondition) { this.hairCondition = hairCondition; }
    public Integer getFecesStatus() { return fecesStatus; }
    public void setFecesStatus(Integer fecesStatus) { this.fecesStatus = fecesStatus; }
    public Integer getVaccineStatus() { return vaccineStatus; }
    public void setVaccineStatus(Integer vaccineStatus) { this.vaccineStatus = vaccineStatus; }
    public Integer getDewormingStatus() { return dewormingStatus; }
    public void setDewormingStatus(Integer dewormingStatus) { this.dewormingStatus = dewormingStatus; }
    public Integer getHealthScore() { return healthScore; }
    public void setHealthScore(Integer healthScore) { this.healthScore = healthScore; }
    public Integer getRiskLevel() { return riskLevel; }
    public void setRiskLevel(Integer riskLevel) { this.riskLevel = riskLevel; }
    public String getAiAdvice() { return aiAdvice; }
    public void setAiAdvice(String aiAdvice) { this.aiAdvice = aiAdvice; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public LocalDate getInspectionDate() { return inspectionDate; }
    public void setInspectionDate(LocalDate inspectionDate) { this.inspectionDate = inspectionDate; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
    public String getPetName() { return petName; }
    public void setPetName(String petName) { this.petName = petName; }
    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }
}
