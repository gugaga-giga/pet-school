package com.petschool.service.impl;

import com.petschool.entity.*;
import com.petschool.mapper.*;
import com.petschool.service.CouponService;
import com.petschool.service.PetOrderService;
import com.petschool.service.WalletService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class PetOrderServiceImpl implements PetOrderService {

    private final PetOrderMapper petOrderMapper;
    private final CoursePackageMapper coursePackageMapper;
    private final CourseMapper courseMapper;
    private final PetMapper petMapper;
    private final CouponService couponService;
    private final WalletService walletService;

    public PetOrderServiceImpl(PetOrderMapper petOrderMapper, CoursePackageMapper coursePackageMapper,
                               CourseMapper courseMapper, PetMapper petMapper, CouponService couponService, WalletService walletService) {
        this.petOrderMapper = petOrderMapper;
        this.coursePackageMapper = coursePackageMapper;
        this.courseMapper = courseMapper;
        this.petMapper = petMapper;
        this.couponService = couponService;
        this.walletService = walletService;
    }

    @Override
    @Transactional
    public PetOrder create(Long userId, Long petId, Long packageId, Integer months, Long couponId) {
        CoursePackage pkg = coursePackageMapper.selectById(packageId);
        if (pkg == null) throw new RuntimeException("套餐不存在");

        Course course = courseMapper.selectById(pkg.getCourseId());
        if (course == null) throw new RuntimeException("课程不存在");

        Pet pet = petMapper.selectById(petId);
        if (pet == null) throw new RuntimeException("宠物不存在");
        if (!pet.getUserId().equals(userId)) throw new RuntimeException("该宠物不属于当前用户");

        if (months == null || months < 1) months = 1;

        BigDecimal originalPrice = pkg.getPrice().multiply(BigDecimal.valueOf(months));

        PetOrder order = new PetOrder();
        order.setOrderNo(generateOrderNo("C"));
        order.setUserId(userId);
        order.setPetId(petId);
        order.setPackageId(packageId);
        order.setMonths(months);
        order.setTotalPrice(originalPrice);
        order.setStatus(0);
        order.setCourseName(course.getName());
        order.setPackageName(pkg.getName());
        order.setPetName(pet.getName());

        if (couponId != null) {
            BigDecimal discount = couponService.calculateDiscount(couponId, "course", originalPrice, pkg.getCourseId());
            if (discount.compareTo(BigDecimal.ZERO) > 0) {
                order.setCouponId(couponId);
                order.setCouponAmount(discount);
                order.setFinalPrice(originalPrice.subtract(discount));
            } else {
                order.setCouponId(null);
                order.setCouponAmount(BigDecimal.ZERO);
                order.setFinalPrice(originalPrice);
            }
        } else {
            order.setCouponId(null);
            order.setCouponAmount(BigDecimal.ZERO);
            order.setFinalPrice(originalPrice);
        }

        petOrderMapper.insert(order);

        if (couponId != null && order.getCouponAmount() != null && order.getCouponAmount().compareTo(BigDecimal.ZERO) > 0) {
            couponService.useCoupon(couponId, order.getId(), order.getCouponAmount());
        }

        return order;
    }

    @Override
    public PetOrder getById(Long id) {
        return petOrderMapper.selectById(id);
    }

    @Override
    public List<PetOrder> getByUserId(Long userId) {
        return petOrderMapper.selectByUserId(userId);
    }

    @Override
    @Transactional
    public int pay(Long id, Long userId) {
        PetOrder order = petOrderMapper.selectById(id);
        if (order == null) throw new RuntimeException("订单不存在");
        if (!order.getUserId().equals(userId)) throw new RuntimeException("无权操作此订单");
        if (order.getStatus() != 0) throw new RuntimeException("订单状态不允许支付");
        BigDecimal payAmount = order.getFinalPrice() != null ? order.getFinalPrice() : order.getTotalPrice();
        walletService.deduct(userId, payAmount, "course", order.getId(), "课程支付-" + order.getOrderNo());
        return petOrderMapper.updateStatus(id, 1);
    }

    @Override
    @Transactional
    public int cancel(Long id, Long userId) {
        PetOrder order = petOrderMapper.selectById(id);
        if (order == null) throw new RuntimeException("订单不存在");
        if (!order.getUserId().equals(userId)) throw new RuntimeException("无权操作此订单");
        if (order.getStatus() != 0 && order.getStatus() != 1) throw new RuntimeException("订单状态不允许取消");
        if (order.getStatus() == 1) {
            BigDecimal payAmount = order.getFinalPrice() != null ? order.getFinalPrice() : order.getTotalPrice();
            walletService.refund(userId, payAmount, "course", order.getId(), "课程退款-" + order.getOrderNo());
        }
        if (order.getCouponId() != null) {
            couponService.returnCoupon(order.getCouponId());
        }
        return petOrderMapper.updateStatus(id, 2);
    }

    @Override
    public Map<String, Object> page(String keyword, Integer status, int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        List<PetOrder> list = petOrderMapper.selectPage(keyword, status, offset, pageSize);
        int total = petOrderMapper.selectPageCount(keyword, status);
        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);
        result.put("page", page);
        result.put("pageSize", pageSize);
        result.put("totalPages", (total + pageSize - 1) / pageSize);
        return result;
    }

    @Override
    public List<PetOrder> listAll() {
        return petOrderMapper.selectAll();
    }

    @Override
    public int updateStatus(Long id, Integer status) {
        return petOrderMapper.updateStatus(id, status);
    }

    @Override
    public int deleteById(Long id) {
        return petOrderMapper.deleteById(id);
    }

    @Override
    public int deleteBatch(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return 0;
        return petOrderMapper.deleteBatch(ids);
    }

    @Override
    @Transactional
    public void adminRefund(Long id) {
        PetOrder order = petOrderMapper.selectById(id);
        if (order == null) throw new RuntimeException("订单不存在");
        if (order.getStatus() != 1) throw new RuntimeException("只有已支付订单才能退款");
        BigDecimal payAmount = order.getFinalPrice() != null ? order.getFinalPrice() : order.getTotalPrice();
        walletService.refund(order.getUserId(), payAmount, "course", order.getId(), "管理员退款-" + order.getOrderNo());
        if (order.getCouponId() != null) {
            couponService.returnCoupon(order.getCouponId());
        }
        petOrderMapper.updateStatus(id, 4);
    }

    private String generateOrderNo(String prefix) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int random = ThreadLocalRandom.current().nextInt(1000, 9999);
        return prefix + timestamp + random;
    }
}
