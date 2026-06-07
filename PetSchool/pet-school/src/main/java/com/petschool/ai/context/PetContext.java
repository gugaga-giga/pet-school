package com.petschool.ai.context;

import java.math.BigDecimal;

/**
 * 宠物上下文，包含宠物基本信息及健康/疫苗状态
 */
public class PetContext {

    private Long petId;
    private String petName;
    private String breed;
    private Integer age;
    private BigDecimal weight;
    private Integer gender; // 0母 1公
    private String genderName;
    private Integer sterilized; // 0否 1是
    private String allergyInfo;
    private String healthStatus; // 从health_record推断
    private String vaccineStatus; // 从vaccine_record推断

    public PetContext() {}

    public Long getPetId() { return petId; }
    public void setPetId(Long petId) { this.petId = petId; }
    public String getPetName() { return petName; }
    public void setPetName(String petName) { this.petName = petName; }
    public String getBreed() { return breed; }
    public void setBreed(String breed) { this.breed = breed; }
    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }
    public BigDecimal getWeight() { return weight; }
    public void setWeight(BigDecimal weight) { this.weight = weight; }
    public Integer getGender() { return gender; }
    public void setGender(Integer gender) {
        this.gender = gender;
        this.genderName = (gender != null && gender == 1) ? "公" : "母";
    }
    public String getGenderName() { return genderName; }
    public void setGenderName(String genderName) { this.genderName = genderName; }
    public Integer getSterilized() { return sterilized; }
    public void setSterilized(Integer sterilized) { this.sterilized = sterilized; }
    public String getAllergyInfo() { return allergyInfo; }
    public void setAllergyInfo(String allergyInfo) { this.allergyInfo = allergyInfo; }
    public String getHealthStatus() { return healthStatus; }
    public void setHealthStatus(String healthStatus) { this.healthStatus = healthStatus; }
    public String getVaccineStatus() { return vaccineStatus; }
    public void setVaccineStatus(String vaccineStatus) { this.vaccineStatus = vaccineStatus; }

    /**
     * 获取绝育状态描述
     */
    public String getSterilizedDesc() {
        if (sterilized == null) return "未知";
        return sterilized == 1 ? "已绝育" : "未绝育";
    }
}
