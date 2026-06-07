package com.petschool.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

public class HealthRecord implements Serializable {
    private Long id;
    private Long petId;
    private BigDecimal temperature;
    private BigDecimal weight;
    private Integer appetite;
    private Integer spirit;
    private LocalDate checkDate;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getPetId() { return petId; }
    public void setPetId(Long petId) { this.petId = petId; }
    public BigDecimal getTemperature() { return temperature; }
    public void setTemperature(BigDecimal temperature) { this.temperature = temperature; }
    public BigDecimal getWeight() { return weight; }
    public void setWeight(BigDecimal weight) { this.weight = weight; }
    public Integer getAppetite() { return appetite; }
    public void setAppetite(Integer appetite) { this.appetite = appetite; }
    public Integer getSpirit() { return spirit; }
    public void setSpirit(Integer spirit) { this.spirit = spirit; }
    public LocalDate getCheckDate() { return checkDate; }
    public void setCheckDate(LocalDate checkDate) { this.checkDate = checkDate; }
}
