package com.petschool.service;

import com.petschool.entity.UserCoupon;
import java.util.List;

public interface UserCouponService {
    int claim(UserCoupon uc);
    List<UserCoupon> getByUserId(Long userId);
    int useCoupon(Long id);
}
