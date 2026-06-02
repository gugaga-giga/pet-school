package com.petschool.controller;

import com.petschool.entity.PetOrder;
import com.petschool.service.PetOrderService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/order")
public class PetOrderController {

    private final PetOrderService petOrderService;

    public PetOrderController(PetOrderService petOrderService) {
        this.petOrderService = petOrderService;
    }

    @PostMapping("/course/create")
    public Map<String, Object> createCourseOrder(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        try {
            Long userId = (Long) request.getAttribute("userId");
            if (userId == null) { result.put("code", 401); result.put("message", "未登录"); return result; }
            Long petId = Long.valueOf(params.get("petId").toString());
            Long packageId = Long.valueOf(params.get("packageId").toString());
            Integer months = params.get("months") != null ? Integer.valueOf(params.get("months").toString()) : 1;
            Long couponId = params.get("couponId") != null ? Long.valueOf(params.get("couponId").toString()) : null;

            PetOrder order = petOrderService.create(userId, petId, packageId, months, couponId);
            result.put("code", 200);
            result.put("message", "下单成功");
            result.put("data", order);
        } catch (RuntimeException e) {
            result.put("code", 400);
            result.put("message", e.getMessage());
        }
        return result;
    }

    @GetMapping("/course/user/{userId}")
    public Map<String, Object> getCourseOrders(@PathVariable Long userId) {
        Map<String, Object> result = new HashMap<>();
        List<PetOrder> orders = petOrderService.getByUserId(userId);
        result.put("code", 200);
        result.put("data", orders);
        return result;
    }

    @PutMapping("/course/pay/{id}")
    public Map<String, Object> payCourseOrder(@PathVariable Long id, HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        try {
            Long userId = (Long) request.getAttribute("userId");
            if (userId == null) { result.put("code", 401); result.put("message", "未登录"); return result; }
            int rows = petOrderService.pay(id, userId);
            if (rows > 0) {
                result.put("code", 200);
                result.put("message", "支付成功");
            } else {
                result.put("code", 500);
                result.put("message", "支付失败");
            }
        } catch (RuntimeException e) {
            result.put("code", 400);
            result.put("message", e.getMessage());
        }
        return result;
    }

    @PutMapping("/course/cancel/{id}")
    public Map<String, Object> cancelCourseOrder(@PathVariable Long id, HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        try {
            Long userId = (Long) request.getAttribute("userId");
            if (userId == null) { result.put("code", 401); result.put("message", "未登录"); return result; }
            int rows = petOrderService.cancel(id, userId);
            if (rows > 0) {
                result.put("code", 200);
                result.put("message", "已取消");
            } else {
                result.put("code", 500);
                result.put("message", "取消失败");
            }
        } catch (RuntimeException e) {
            result.put("code", 400);
            result.put("message", e.getMessage());
        }
        return result;
    }
}
