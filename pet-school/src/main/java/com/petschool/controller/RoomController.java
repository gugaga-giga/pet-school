package com.petschool.controller;

import com.petschool.entity.*;
import com.petschool.service.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/room")
public class RoomController {

    private final RoomTypeService roomTypeService;
    private final RoomService roomService;
    private final BoardingOrderService boardingOrderService;

    public RoomController(RoomTypeService roomTypeService, RoomService roomService, BoardingOrderService boardingOrderService) {
        this.roomTypeService = roomTypeService;
        this.roomService = roomService;
        this.boardingOrderService = boardingOrderService;
    }

    @GetMapping("/type/list")
    public Map<String, Object> typeList() {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", roomTypeService.listAll());
        return result;
    }

    @GetMapping("/type/available")
    public Map<String, Object> typeListWithAvailability() {
        Map<String, Object> result = new HashMap<>();
        List<RoomType> types = roomTypeService.listAll();
        List<Map<String, Object>> list = new java.util.ArrayList<>();
        for (RoomType rt : types) {
            int available = rt.getCapacity() != null ? rt.getCapacity() : 0;
            Map<String, Object> item = new HashMap<>();
            item.put("id", rt.getId());
            item.put("name", rt.getName());
            item.put("dailyPrice", rt.getDailyPrice());
            item.put("facilities", rt.getFacilities());
            item.put("image", rt.getImage());
            item.put("available", available);
            list.add(item);
        }
        result.put("code", 200);
        result.put("data", list);
        return result;
    }

    @GetMapping("/type/{id}")
    public Map<String, Object> typeById(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        RoomType rt = roomTypeService.getById(id);
        if (rt != null) { result.put("code", 200); result.put("data", rt); }
        else { result.put("code", 404); result.put("message", "房型不存在"); }
        return result;
    }

    @GetMapping("/list")
    public Map<String, Object> roomList(@RequestParam Long typeId) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", roomService.getByTypeId(typeId));
        return result;
    }

    @PostMapping("/boarding/create")
    public Map<String, Object> createBoarding(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        try {
            Long userId = (Long) request.getAttribute("userId");
            if (userId == null) { result.put("code", 401); result.put("message", "未登录"); return result; }
            Long petId = Long.valueOf(params.get("petId").toString());
            Long roomId = Long.valueOf(params.get("roomId").toString());
            String checkIn = params.get("checkIn").toString();
            String checkOut = params.get("checkOut").toString();
            Long couponId = params.get("couponId") != null ? Long.valueOf(params.get("couponId").toString()) : null;

            BoardingOrder order = boardingOrderService.create(userId, petId, roomId, checkIn, checkOut, couponId);
            result.put("code", 200);
            result.put("message", "寄宿下单成功");
            result.put("data", order);
        } catch (RuntimeException e) {
            result.put("code", 400);
            result.put("message", e.getMessage());
        }
        return result;
    }

    @GetMapping("/boarding/user/{userId}")
    public Map<String, Object> boardingByUser(@PathVariable Long userId) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", boardingOrderService.getByUserId(userId));
        return result;
    }

    @PutMapping("/boarding/pay/{id}")
    public Map<String, Object> payBoarding(@PathVariable Long id, HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        try {
            Long userId = (Long) request.getAttribute("userId");
            if (userId == null) { result.put("code", 401); result.put("message", "未登录"); return result; }
            int rows = boardingOrderService.pay(id, userId);
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

    @PutMapping("/boarding/cancel/{id}")
    public Map<String, Object> cancelBoarding(@PathVariable Long id, HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        try {
            Long userId = (Long) request.getAttribute("userId");
            if (userId == null) { result.put("code", 401); result.put("message", "未登录"); return result; }
            int rows = boardingOrderService.cancel(id, userId);
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
