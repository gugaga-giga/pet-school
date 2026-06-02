package com.petschool.entity;

import java.io.Serializable;
import java.math.BigDecimal;

public class Course implements Serializable {
    private Long id;
    private Long categoryId;
    private String name;
    private Integer duration;
    private BigDecimal monthlyPrice;
    private String description;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) { this.duration = duration; }
    public BigDecimal getMonthlyPrice() { return monthlyPrice; }
    public void setMonthlyPrice(BigDecimal monthlyPrice) { this.monthlyPrice = monthlyPrice; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
