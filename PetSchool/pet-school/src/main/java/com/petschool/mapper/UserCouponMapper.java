package com.petschool.mapper;

import com.petschool.entity.UserCoupon;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserCouponMapper {
    int insert(UserCoupon uc);
    UserCoupon selectById(@Param("id") Long id);
    List<UserCoupon> selectByUserId(@Param("userId") Long userId);
    List<UserCoupon> selectByUserIdAndStatus(@Param("userId") Long userId, @Param("status") Integer status);
    UserCoupon selectByUserIdAndCouponId(@Param("userId") Long userId, @Param("couponId") Long couponId);
    List<UserCoupon> selectAvailableByUserId(@Param("userId") Long userId);
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);
    int updateUseInfo(@Param("id") Long id, @Param("orderId") Long orderId, @Param("discountAmount") java.math.BigDecimal discountAmount);
}
