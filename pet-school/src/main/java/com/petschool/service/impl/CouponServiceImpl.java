package com.petschool.service.impl;

import com.petschool.entity.Coupon;
import com.petschool.entity.User;
import com.petschool.entity.UserCoupon;
import com.petschool.mapper.CouponMapper;
import com.petschool.mapper.UserCouponMapper;
import com.petschool.mapper.UserMapper;
import com.petschool.service.CouponService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CouponServiceImpl implements CouponService {

    private final CouponMapper couponMapper;
    private final UserCouponMapper userCouponMapper;
    private final UserMapper userMapper;

    public CouponServiceImpl(CouponMapper couponMapper, UserCouponMapper userCouponMapper, UserMapper userMapper) {
        this.couponMapper = couponMapper;
        this.userCouponMapper = userCouponMapper;
        this.userMapper = userMapper;
    }

    @Override
    public Map<String, Object> page(String keyword, Integer type, Integer status, int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        List<Coupon> list = couponMapper.selectPage(keyword, type, status, offset, pageSize);
        int total = couponMapper.selectPageCount(keyword, type, status);
        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);
        result.put("page", page);
        result.put("pageSize", pageSize);
        result.put("totalPages", (total + pageSize - 1) / pageSize);
        return result;
    }

    @Override
    public List<Coupon> listAll() {
        return couponMapper.selectAll();
    }

    @Override
    public Coupon getById(Long id) {
        return couponMapper.selectById(id);
    }

    @Override
    public int create(Coupon coupon) {
        if (coupon.getReceiveCount() == null) coupon.setReceiveCount(0);
        if (coupon.getPerLimit() == null) coupon.setPerLimit(1);
        if (coupon.getMinAmount() == null) coupon.setMinAmount(BigDecimal.ZERO);
        if (coupon.getStatus() == null) coupon.setStatus(1);
        if (coupon.getCreateTime() == null) coupon.setCreateTime(LocalDateTime.now());
        return couponMapper.insert(coupon);
    }

    @Override
    public int update(Coupon coupon) {
        return couponMapper.update(coupon);
    }

    @Override
    public int deleteById(Long id) {
        return couponMapper.deleteById(id);
    }

    @Override
    public List<Coupon> getActiveCoupons() {
        return couponMapper.selectActive();
    }

    @Override
    @Transactional
    public int receiveCoupon(Long userId, Long couponId) {
        Coupon coupon = couponMapper.selectById(couponId);
        if (coupon == null) throw new RuntimeException("优惠券不存在");
        if (coupon.getStatus() != 1) throw new RuntimeException("优惠券已禁用");

        LocalDateTime now = LocalDateTime.now();
        if (coupon.getStartTime() != null && now.isBefore(coupon.getStartTime())) {
            throw new RuntimeException("优惠券尚未开始");
        }
        if (coupon.getEndTime() != null && now.isAfter(coupon.getEndTime())) {
            throw new RuntimeException("优惠券已过期");
        }

        if (coupon.getTotalCount() != null && coupon.getReceiveCount() != null && coupon.getReceiveCount() >= coupon.getTotalCount()) {
            throw new RuntimeException("优惠券已领完");
        }

        List<UserCoupon> existing = userCouponMapper.selectByUserId(userId);
        long userCount = existing.stream().filter(uc -> uc.getCouponId().equals(couponId)).count();
        if (coupon.getPerLimit() != null && userCount >= coupon.getPerLimit()) {
            throw new RuntimeException("已达到领取上限");
        }

        UserCoupon duplicate = userCouponMapper.selectByUserIdAndCouponId(userId, couponId);
        if (duplicate != null && duplicate.getStatus() != 2) {
            throw new RuntimeException("已领取该优惠券");
        }

        UserCoupon uc = new UserCoupon();
        uc.setUserId(userId);
        uc.setCouponId(couponId);
        uc.setStatus(0);
        uc.setReceiveTime(now);
        uc.setExpireTime(coupon.getEndTime());
        uc.setCouponName(coupon.getName());
        userCouponMapper.insert(uc);

        couponMapper.updateReceiveCount(couponId);

        return 1;
    }

    @Override
    public List<UserCoupon> getUserCoupons(Long userId) {
        List<UserCoupon> list = userCouponMapper.selectByUserId(userId);
        for (UserCoupon uc : list) {
            Coupon coupon = couponMapper.selectById(uc.getCouponId());
            if (coupon != null) {
                uc.setCoupon(coupon);
                if (uc.getCouponName() == null) uc.setCouponName(coupon.getName());
            }
        }
        return list;
    }

    @Override
    public List<UserCoupon> getUserCouponsByStatus(Long userId, Integer status) {
        return userCouponMapper.selectByUserIdAndStatus(userId, status);
    }

    @Override
    public List<UserCoupon> getAvailableCoupons(Long userId) {
        List<UserCoupon> list = userCouponMapper.selectAvailableByUserId(userId);
        for (UserCoupon uc : list) {
            Coupon coupon = couponMapper.selectById(uc.getCouponId());
            if (coupon != null) {
                uc.setCoupon(coupon);
                if (uc.getCouponName() == null) uc.setCouponName(coupon.getName());
            }
        }
        return list;
    }

    @Override
    public List<UserCoupon> getAvailableCouponsForOrder(Long userId, String orderType, BigDecimal orderAmount, Long scopeId) {
        List<UserCoupon> available = userCouponMapper.selectAvailableByUserId(userId);
        List<UserCoupon> result = new ArrayList<>();
        for (UserCoupon uc : available) {
            Coupon coupon = couponMapper.selectById(uc.getCouponId());
            if (coupon == null) continue;
            if (!isApplicable(coupon, orderType, orderAmount, scopeId)) continue;
            uc.setCouponName(coupon.getName());
            uc.setCoupon(coupon);
            result.add(uc);
        }
        return result;
    }

    @Override
    public BigDecimal calculateDiscount(Long userCouponId, String orderType, BigDecimal orderAmount, Long scopeId) {
        UserCoupon uc = userCouponMapper.selectById(userCouponId);
        if (uc == null) throw new RuntimeException("用户优惠券不存在");
        if (uc.getStatus() != 0) throw new RuntimeException("优惠券不可用");

        Coupon coupon = couponMapper.selectById(uc.getCouponId());
        if (coupon == null) throw new RuntimeException("优惠券不存在");

        if (!isApplicable(coupon, orderType, orderAmount, scopeId)) {
            return BigDecimal.ZERO;
        }

        BigDecimal minAmount = coupon.getMinAmount() != null ? coupon.getMinAmount() : BigDecimal.ZERO;
        if (orderAmount.compareTo(minAmount) < 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal discount;
        if (coupon.getDiscountType() == 1) {
            discount = coupon.getDiscountValue();
        } else if (coupon.getDiscountType() == 2) {
            discount = orderAmount.multiply(BigDecimal.ONE.subtract(coupon.getDiscountValue().divide(BigDecimal.valueOf(100), 2, BigDecimal.ROUND_HALF_UP)));
            if (coupon.getMaxDiscount() != null && discount.compareTo(coupon.getMaxDiscount()) > 0) {
                discount = coupon.getMaxDiscount();
            }
        } else {
            discount = BigDecimal.ZERO;
        }

        if (discount.compareTo(BigDecimal.ZERO) < 0) {
            discount = BigDecimal.ZERO;
        }
        if (discount.compareTo(orderAmount) > 0) {
            discount = orderAmount;
        }

        return discount;
    }

    @Override
    public void useCoupon(Long userCouponId, Long orderId, BigDecimal discountAmount) {
        userCouponMapper.updateUseInfo(userCouponId, orderId, discountAmount);
    }

    @Override
    public void returnCoupon(Long userCouponId) {
        userCouponMapper.updateStatus(userCouponId, 0);
    }

    @Override
    @Transactional
    public void sendToAllUsers(Long couponId) {
        List<User> users = userMapper.selectAll();
        for (User user : users) {
            try {
                receiveCoupon(user.getId(), couponId);
            } catch (RuntimeException ignored) {
            }
        }
    }

    @Override
    public void sendToUser(Long couponId, Long userId) {
        receiveCoupon(userId, couponId);
    }

    private boolean isApplicable(Coupon coupon, String orderType, BigDecimal orderAmount, Long scopeId) {
        if (coupon.getType() == 1 && !"course".equals(orderType)) return false;
        if (coupon.getType() == 2 && !"boarding".equals(orderType)) return false;

        if (coupon.getScopeType() != null) {
            if (coupon.getScopeType() == 2) {
                if (scopeId == null || !scopeId.equals(coupon.getScopeId())) return false;
            }
            if (coupon.getScopeType() == 3 && !"boarding".equals(orderType)) return false;
        }

        BigDecimal minAmount = coupon.getMinAmount() != null ? coupon.getMinAmount() : BigDecimal.ZERO;
        if (orderAmount.compareTo(minAmount) < 0) return false;

        return true;
    }
}
