package com.petschool.controller;

import com.petschool.entity.Wallet;
import com.petschool.entity.WalletRecord;
import com.petschool.service.WalletService;
import com.petschool.util.JwtUtil;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/wallet")
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    private Long getUserId(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return JwtUtil.getUserId(token);
    }

    @GetMapping("/info")
    public Map<String, Object> getInfo(HttpServletRequest request) {
        Long userId = getUserId(request);
        Wallet wallet = walletService.ensureWallet(userId);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", wallet);
        return result;
    }

    @PostMapping("/recharge")
    public Map<String, Object> recharge(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        Long userId = getUserId(request);
        BigDecimal amount = new BigDecimal(body.get("amount").toString());
        String remark = body.get("remark") != null ? body.get("remark").toString() : null;
        try {
            walletService.recharge(userId, amount, remark);
            Wallet wallet = walletService.getByUserId(userId);
            Map<String, Object> result = new HashMap<>();
            result.put("code", 200);
            result.put("message", "充值成功");
            result.put("data", wallet);
            return result;
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("code", 500);
            result.put("message", e.getMessage());
            return result;
        }
    }

    @GetMapping("/records")
    public Map<String, Object> records(
            @RequestParam(required = false) Integer type,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            HttpServletRequest request) {
        Long userId = getUserId(request);
        List<WalletRecord> list = walletService.getRecords(userId, type, page, pageSize);
        int total = walletService.getRecordCount(userId, type);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", new HashMap<String, Object>() {{
            put("list", list);
            put("total", total);
        }});
        return result;
    }

    @GetMapping("/admin/page")
    public Map<String, Object> adminPage(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        Map<String, Object> data = walletService.getWalletPage(keyword, page, pageSize);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", data);
        return result;
    }

    @GetMapping("/admin/detail/{id}")
    public Map<String, Object> adminDetail(@PathVariable Long id) {
        try {
            var wallets = walletService.getWalletPage(null, 1, 10000);
            @SuppressWarnings("unchecked")
            List<Wallet> list = (List<Wallet>) wallets.get("list");
            Wallet wallet = list.stream().filter(w -> w.getId().equals(id)).findFirst().orElse(null);
            if (wallet == null) {
                Map<String, Object> result = new HashMap<>();
                result.put("code", 404);
                result.put("message", "钱包不存在");
                return result;
            }
            Map<String, Object> result = new HashMap<>();
            result.put("code", 200);
            result.put("data", wallet);
            return result;
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("code", 500);
            result.put("message", e.getMessage());
            return result;
        }
    }

    @PostMapping("/admin/adjust")
    public Map<String, Object> adminAdjust(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        try {
            Long walletId = Long.valueOf(body.get("walletId").toString());
            BigDecimal amount = new BigDecimal(body.get("amount").toString());
            String remark = body.get("remark") != null ? body.get("remark").toString() : null;
            String operatorName = "管理员";
            try {
                Long opId = getUserId(request);
                operatorName = "管理员#" + opId;
            } catch (Exception ignored) {}
            walletService.adjust(walletId, amount, remark, operatorName);
            Map<String, Object> result = new HashMap<>();
            result.put("code", 200);
            result.put("message", "调整成功");
            return result;
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("code", 500);
            result.put("message", e.getMessage());
            return result;
        }
    }

    @GetMapping("/admin/records")
    public Map<String, Object> adminRecords(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) String businessType,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        Map<String, Object> data = walletService.getRecordPage(keyword, type, businessType, page, pageSize);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", data);
        return result;
    }

    @PutMapping("/admin/status")
    public Map<String, Object> adminStatus(@RequestBody Map<String, Object> body) {
        try {
            Long walletId = Long.valueOf(body.get("walletId").toString());
            Integer status = Integer.valueOf(body.get("status").toString());
            walletService.updateStatus(walletId, status);
            Map<String, Object> result = new HashMap<>();
            result.put("code", 200);
            result.put("message", status == 0 ? "冻结成功" : "启用成功");
            return result;
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("code", 500);
            result.put("message", e.getMessage());
            return result;
        }
    }
}
