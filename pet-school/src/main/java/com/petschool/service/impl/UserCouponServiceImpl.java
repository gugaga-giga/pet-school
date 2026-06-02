package com.petschool.service.impl;

import com.petschool.entity.UserCoupon;
import com.petschool.mapper.UserCouponMapper;
import com.petschool.service.UserCouponService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserCouponServiceImpl implements UserCouponService {

    private final UserCouponMapper userCouponMapper;

    public UserCouponServiceImpl(UserCouponMapper userCouponMapper) {
        this.userCouponMapper = userCouponMapper;
    }

    @Override
    public int claim(UserCoupon uc) {
        uc.setStatus(0);
        return userCouponMapper.insert(uc);
    }

    @Override
    public List<UserCoupon> getByUserId(Long userId) {
        return userCouponMapper.selectByUserId(userId);
    }

    @Override
    public int useCoupon(Long id) {
        return userCouponMapper.updateStatus(id, 1);
    }
}
