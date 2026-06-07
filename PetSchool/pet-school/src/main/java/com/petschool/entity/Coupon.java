package com.petschool.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Coupon implements Serializable {
    private Long id;
    private String name;
    private Integer type;
    private Integer discountType;
    private BigDecimal discountValue;
    private BigDecimal minAmount;
    private BigDecimal maxDiscount;
    private Integer scopeType;
    private Long scopeId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer totalCount;
    private Integer receiveCount;
    private Integer perLimit;
    private Integer status;
    private String description;
    private LocalDateTime createTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getType() { return type; }
    public void setType(Integer type) { this.type = type; }
    public Integer getDiscountType() { return discountType; }
    public void setDiscountType(Integer discountType) { this.discountType = discountType; }
    public BigDecimal getDiscountValue() { return discountValue; }
    public void setDiscountValue(BigDecimal discountValue) { this.discountValue = discountValue; }
    public BigDecimal getMinAmount() { return minAmount; }
    public void setMinAmount(BigDecimal minAmount) { this.minAmount = minAmount; }
    public BigDecimal getMaxDiscount() { return maxDiscount; }
    public void setMaxDiscount(BigDecimal maxDiscount) { this.maxDiscount = maxDiscount; }
    public Integer getScopeType() { return scopeType; }
    public void setScopeType(Integer scopeType) { this.scopeType = scopeType; }
    public Long getScopeId() { return scopeId; }
    public void setScopeId(Long scopeId) { this.scopeId = scopeId; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public Integer getTotalCount() { return totalCount; }
    public void setTotalCount(Integer totalCount) { this.totalCount = totalCount; }
    public Integer getReceiveCount() { return receiveCount; }
    public void setReceiveCount(Integer receiveCount) { this.receiveCount = receiveCount; }
    public Integer getPerLimit() { return perLimit; }
    public void setPerLimit(Integer perLimit) { this.perLimit = perLimit; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}
