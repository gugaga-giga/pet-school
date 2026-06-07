package com.petschool.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PetOrder implements Serializable {
    private Long id;
    private String orderNo;
    private Long userId;
    private Long petId;
    private Long packageId;
    private Integer months;
    private BigDecimal totalPrice;
    private Integer status;
    private LocalDateTime createTime;
    private String courseName;
    private String packageName;
    private String petName;
    private Long couponId;
    private BigDecimal couponAmount;
    private BigDecimal finalPrice;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getPetId() { return petId; }
    public void setPetId(Long petId) { this.petId = petId; }
    public Long getPackageId() { return packageId; }
    public void setPackageId(Long packageId) { this.packageId = packageId; }
    public Integer getMonths() { return months; }
    public void setMonths(Integer months) { this.months = months; }
    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }
    public String getPackageName() { return packageName; }
    public void setPackageName(String packageName) { this.packageName = packageName; }
    public String getPetName() { return petName; }
    public void setPetName(String petName) { this.petName = petName; }
    public Long getCouponId() { return couponId; }
    public void setCouponId(Long couponId) { this.couponId = couponId; }
    public BigDecimal getCouponAmount() { return couponAmount; }
    public void setCouponAmount(BigDecimal couponAmount) { this.couponAmount = couponAmount; }
    public BigDecimal getFinalPrice() { return finalPrice; }
    public void setFinalPrice(BigDecimal finalPrice) { this.finalPrice = finalPrice; }
}
