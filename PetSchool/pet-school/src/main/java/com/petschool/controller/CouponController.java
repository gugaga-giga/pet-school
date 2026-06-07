package com.petschool.controller;

import com.petschool.entity.Coupon;
import com.petschool.entity.UserCoupon;
import com.petschool.service.CouponService;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/coupon")
public class CouponController {

    private final CouponService couponService;

    public CouponController(CouponService couponService) {
        this.couponService = couponService;
    }

    private Map<String, Object> ok(Object data) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", data);
        return result;
    }

    private Map<String, Object> okMsg(String message) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", message);
        return result;
    }

    private Map<String, Object> fail(String message) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 400);
        result.put("message", message);
        return result;
    }

    @GetMapping("/page")
    public Map<String, Object> page(@RequestParam(required = false) String keyword,
                                    @RequestParam(required = false) Integer type,
                                    @RequestParam(required = false) Integer status,
                                    @RequestParam(defaultValue = "1") int page,
                                    @RequestParam(defaultValue = "10") int pageSize) {
        return ok(couponService.page(keyword, type, status, page, pageSize));
    }

    @PostMapping("/create")
    public Map<String, Object> create(@RequestBody Coupon coupon) {
        try {
            int rows = couponService.create(coupon);
            if (rows > 0) return okMsg("创建成功");
            return fail("创建失败");
        } catch (RuntimeException e) {
            return fail(e.getMessage());
        }
    }

    @PutMapping("/update")
    public Map<String, Object> update(@RequestBody Coupon coupon) {
        try {
            int rows = couponService.update(coupon);
            if (rows > 0) return okMsg("更新成功");
            return fail("更新失败");
        } catch (RuntimeException e) {
            return fail(e.getMessage());
        }
    }

    @DeleteMapping("/delete/{id}")
    public Map<String, Object> delete(@PathVariable Long id) {
        try {
            int rows = couponService.deleteById(id);
            if (rows > 0) return okMsg("删除成功");
            return fail("删除失败");
        } catch (RuntimeException e) {
            return fail(e.getMessage());
        }
    }

    @PostMapping("/send")
    public Map<String, Object> send(@RequestBody Map<String, Object> params) {
        try {
            Long couponId = Long.valueOf(params.get("couponId").toString());
            Boolean all = params.get("all") != null && Boolean.parseBoolean(params.get("all").toString());
            if (all) {
                couponService.sendToAllUsers(couponId);
                return okMsg("发放成功");
            } else {
                @SuppressWarnings("unchecked")
                List<Number> userIds = (List<Number>) params.get("userIds");
                if (userIds != null) {
                    for (Number uid : userIds) {
                        try {
                            couponService.sendToUser(couponId, uid.longValue());
                        } catch (RuntimeException ignored) {
                        }
                    }
                }
                return okMsg("发放成功");
            }
        } catch (RuntimeException e) {
            return fail(e.getMessage());
        }
    }

    @GetMapping("/active")
    public Map<String, Object> active() {
        return ok(couponService.getActiveCoupons());
    }

    @PostMapping("/receive/{couponId}")
    public Map<String, Object> receive(@PathVariable Long couponId, @RequestParam Long userId) {
        try {
            couponService.receiveCoupon(userId, couponId);
            return okMsg("领取成功");
        } catch (RuntimeException e) {
            return fail(e.getMessage());
        }
    }

    @GetMapping("/my")
    public Map<String, Object> my(@RequestParam Long userId) {
        return ok(couponService.getUserCoupons(userId));
    }

    @GetMapping("/my/available")
    public Map<String, Object> myAvailable(@RequestParam Long userId) {
        return ok(couponService.getAvailableCoupons(userId));
    }

    @GetMapping("/my/available-for-order")
    public Map<String, Object> myAvailableForOrder(@RequestParam Long userId,
                                                    @RequestParam String orderType,
                                                    @RequestParam BigDecimal orderAmount,
                                                    @RequestParam(required = false) Long scopeId) {
        return ok(couponService.getAvailableCouponsForOrder(userId, orderType, orderAmount, scopeId));
    }

    @PostMapping("/calculate")
    public Map<String, Object> calculate(@RequestBody Map<String, Object> params) {
        try {
            Long couponId = Long.valueOf(params.get("couponId").toString());
            String orderType = params.get("orderType") != null ? params.get("orderType").toString() : null;
            BigDecimal orderAmount = new BigDecimal(params.get("orderAmount").toString());
            Long scopeId = params.get("scopeId") != null ? Long.valueOf(params.get("scopeId").toString()) : null;
            BigDecimal discount = couponService.calculateDiscount(couponId, orderType, orderAmount, scopeId);
            Map<String, Object> data = new HashMap<>();
            data.put("discountAmount", discount);
            return ok(data);
        } catch (RuntimeException e) {
            return fail(e.getMessage());
        }
    }
}
