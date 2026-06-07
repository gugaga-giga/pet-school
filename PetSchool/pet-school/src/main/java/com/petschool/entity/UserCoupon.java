package com.petschool.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class UserCoupon implements Serializable {
    private Long id;
    private Long userId;
    private Long couponId;
    private Integer status;
    private LocalDateTime receiveTime;
    private LocalDateTime useTime;
    private LocalDateTime expireTime;
    private Long orderId;
    private String couponName;
    private BigDecimal discountAmount;
    private Coupon coupon;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getCouponId() { return couponId; }
    public void setCouponId(Long couponId) { this.couponId = couponId; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public LocalDateTime getReceiveTime() { return receiveTime; }
    public void setReceiveTime(LocalDateTime receiveTime) { this.receiveTime = receiveTime; }
    public LocalDateTime getUseTime() { return useTime; }
    public void setUseTime(LocalDateTime useTime) { this.useTime = useTime; }
    public LocalDateTime getExpireTime() { return expireTime; }
    public void setExpireTime(LocalDateTime expireTime) { this.expireTime = expireTime; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public String getCouponName() { return couponName; }
    public void setCouponName(String couponName) { this.couponName = couponName; }
    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }
    public Coupon getCoupon() { return coupon; }
    public void setCoupon(Coupon coupon) { this.coupon = coupon; }
}
