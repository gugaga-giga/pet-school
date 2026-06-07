package com.petschool.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class BoardingOrder implements Serializable {
    private Long id;
    private String orderNo;
    private Long userId;
    private Long petId;
    private Long roomId;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private BigDecimal totalPrice;
    private Integer status;
    private LocalDateTime createTime;
    private String roomTypeName;
    private String roomNumber;
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
    public Long getRoomId() { return roomId; }
    public void setRoomId(Long roomId) { this.roomId = roomId; }
    public LocalDate getCheckIn() { return checkIn; }
    public void setCheckIn(LocalDate checkIn) { this.checkIn = checkIn; }
    public LocalDate getCheckOut() { return checkOut; }
    public void setCheckOut(LocalDate checkOut) { this.checkOut = checkOut; }
    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public String getRoomTypeName() { return roomTypeName; }
    public void setRoomTypeName(String roomTypeName) { this.roomTypeName = roomTypeName; }
    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }
    public String getPetName() { return petName; }
    public void setPetName(String petName) { this.petName = petName; }
    public Long getCouponId() { return couponId; }
    public void setCouponId(Long couponId) { this.couponId = couponId; }
    public BigDecimal getCouponAmount() { return couponAmount; }
    public void setCouponAmount(BigDecimal couponAmount) { this.couponAmount = couponAmount; }
    public BigDecimal getFinalPrice() { return finalPrice; }
    public void setFinalPrice(BigDecimal finalPrice) { this.finalPrice = finalPrice; }
}
