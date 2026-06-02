package com.petschool.service;

import com.petschool.entity.Coupon;
import com.petschool.entity.UserCoupon;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface CouponService {
    Map<String, Object> page(String keyword, Integer type, Integer status, int page, int pageSize);
    List<Coupon> listAll();
    Coupon getById(Long id);
    int create(Coupon coupon);
    int update(Coupon coupon);
    int deleteById(Long id);
    List<Coupon> getActiveCoupons();
    int receiveCoupon(Long userId, Long couponId);
    List<UserCoupon> getUserCoupons(Long userId);
    List<UserCoupon> getUserCouponsByStatus(Long userId, Integer status);
    List<UserCoupon> getAvailableCoupons(Long userId);
    List<UserCoupon> getAvailableCouponsForOrder(Long userId, String orderType, BigDecimal orderAmount, Long scopeId);
    BigDecimal calculateDiscount(Long userCouponId, String orderType, BigDecimal orderAmount, Long scopeId);
    void useCoupon(Long userCouponId, Long orderId, BigDecimal discountAmount);
    void returnCoupon(Long userCouponId);
    void sendToAllUsers(Long couponId);
    void sendToUser(Long couponId, Long userId);
}
