package com.petschool.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class WalletRecord {
    private Long id;
    private Long userId;
    private Integer type;
    private BigDecimal amount;
    private BigDecimal balanceBefore;
    private BigDecimal balanceAfter;
    private String businessType;
    private Long businessId;
    private String transactionNo;
    private String remark;
    private LocalDateTime createTime;
    private String username;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Integer getType() { return type; }
    public void setType(Integer type) { this.type = type; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public BigDecimal getBalanceBefore() { return balanceBefore; }
    public void setBalanceBefore(BigDecimal balanceBefore) { this.balanceBefore = balanceBefore; }
    public BigDecimal getBalanceAfter() { return balanceAfter; }
    public void setBalanceAfter(BigDecimal balanceAfter) { this.balanceAfter = balanceAfter; }
    public String getBusinessType() { return businessType; }
    public void setBusinessType(String businessType) { this.businessType = businessType; }
    public Long getBusinessId() { return businessId; }
    public void setBusinessId(Long businessId) { this.businessId = businessId; }
    public String getTransactionNo() { return transactionNo; }
    public void setTransactionNo(String transactionNo) { this.transactionNo = transactionNo; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
}
