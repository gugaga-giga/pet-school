package com.petschool.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class HealthRule implements Serializable {
    private Long id;
    private String petType;
    private String indicatorName;
    private BigDecimal minValue;
    private BigDecimal maxValue;
    private String warningText;
    private String suggestion;
    private Integer riskLevel;
    private LocalDateTime createTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getPetType() { return petType; }
    public void setPetType(String petType) { this.petType = petType; }
    public String getIndicatorName() { return indicatorName; }
    public void setIndicatorName(String indicatorName) { this.indicatorName = indicatorName; }
    public BigDecimal getMinValue() { return minValue; }
    public void setMinValue(BigDecimal minValue) { this.minValue = minValue; }
    public BigDecimal getMaxValue() { return maxValue; }
    public void setMaxValue(BigDecimal maxValue) { this.maxValue = maxValue; }
    public String getWarningText() { return warningText; }
    public void setWarningText(String warningText) { this.warningText = warningText; }
    public String getSuggestion() { return suggestion; }
    public void setSuggestion(String suggestion) { this.suggestion = suggestion; }
    public Integer getRiskLevel() { return riskLevel; }
    public void setRiskLevel(Integer riskLevel) { this.riskLevel = riskLevel; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}
