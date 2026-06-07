package com.petschool.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Wallet {
    private Long id;
    private Long userId;
    private BigDecimal balance;
    private BigDecimal totalRecharge;
    private BigDecimal totalConsume;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private String username;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }
    public BigDecimal getTotalRecharge() { return totalRecharge; }
    public void setTotalRecharge(BigDecimal totalRecharge) { this.totalRecharge = totalRecharge; }
    public BigDecimal getTotalConsume() { return totalConsume; }
    public void setTotalConsume(BigDecimal totalConsume) { this.totalConsume = totalConsume; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
}
