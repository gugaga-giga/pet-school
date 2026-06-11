package com.petschool.controller;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradePagePayModel;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.petschool.entity.Wallet;
import com.petschool.entity.WalletOrder;
import com.petschool.entity.WalletRecord;
import com.petschool.mapper.WalletOrderMapper;
import com.petschool.service.WalletService;
import com.petschool.util.JwtUtil;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/wallet")
public class WalletController {

    private final WalletService walletService;
    private final WalletOrderMapper walletOrderMapper;


    public WalletController(WalletService walletService, WalletOrderMapper walletOrderMapper) {
        this.walletService = walletService;

        this.walletOrderMapper = walletOrderMapper;
    }

    // 支付宝沙箱配置
    private static final String ALIPAY_GATEWAY = "https://openapi-sandbox.dl.alipaydev.com/gateway.do";
    private static final String APP_ID = "9021000132694291";
    private static final String PRIVATE_KEY = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQDGz/mhIwbBHge3wl4LyMOkh9FKonKwdw0dEQdqhWwtv04lehB8FqnCgCu5XQWnMlGq9aMswWAidcFXLLys6wd+3AkUpev/zOt8pabMVtTJfaXVTZgsEhpDbtWpwMrbijOapVQyHOBM0aTTDyDw9UMEBZtFFk/zRSHGnPeEzeS8+6dds832OBHZrD/XrDrYA1wmmqSoC6z5cJukfFdkiNRZhzE3kqjYeXr1CS3NcPl4PFfGlwE30DcA5+WNXjM+8Xpg1XHorQpJn+qi2uSWT9acdbLu79N7TjqW0gRuIp7/XJeSu7vGgHx7oN+lt1Unk63Avpn+A6IaeuWZgkp2a5N7AgMBAAECggEAZKnjTFpuzhNFdx6b/b1+ie0W5IJSLdVpt2Dq7111A8jMaZ9ff2T+OXQqL2XROfH5deV/5GZZJbmX1mrZcXhjSDFjC6hjURbR2UAKr4X7obXT5KewOAhGcgRJfPUYAYb9T/0MH3+Ndnfjy9XqKPcWIpAwKQ/+FT/YtTa/AOg7PBAPHWr/Y4NMuOl27XRZ4X8D8fBAqW7zMrC8pIWUUbHJazZ14XIhMTS6Chg3Thkc6//QpFR0OBBuYNhpxMbPpUjuNyvWG9S4nQswH6fgNP6lboQB6WIQgyPXyYzCHYWBiNKy99oY3veIL9M4yf5c9PTxU8Tx+Idfqfh+PgJzViKPaQKBgQDiUwFCeqvSkRLUUlleSDTYmZwSl5Cv1uGa5GBBRGguIdlOGE3fCLum/Hr3UnjXTFt7f89d+FxBi5Am4eh31SH7csy7hnfMi1+RKHk70f7ZoSRZF9OnYFxM5o9EM+30bL8Jecxq64VXTw/U6rYAVoBgpvRSv6S9O3zeL82MtbqQ1QKBgQDg4Xvw57I7p+/xUa+40f4iRnjSgDGR6OwAkDL6OghydX25F7k7AhDUX5uWUZ1gYfQQum57NjiXDtpzqkJ2yhwffUQ44cX9jNjseB79MtT4OG5VBfJd2zvIYmwHEfXn8P0cs3wa8aTcnRtZFoZNyzSIyNwf5vmPCJrMtQkfeqw7DwKBgQCdD01IfSjQL60JezxZmXJS/ga5QEVAQba+bs39fSNvID5VW7hulvjioUQUbj896ddylPerFrevJNMcqJlSz62NedVqLZ7la7MeYSAAy3I4RQOccFSJSu3C0B2MDx2LCgxo3wVlDZyzQkKSV2+GPIFKkZHr+uKwD6BP8IyMoXCfIQKBgD+t2ijnk0LtVTaiiP3vCOWvyvrgrijBKkMmkijlUgsHViiZnSwmc5mP0UEJr2Thl/k8sZhWTEQKQrXKGrQEm14XXMA2vpgD0FF12mCcik4XvLZ6zpzhPdaKeEiwP1AEn8gG0zfWAXcvTdU2WaWhyyiIqDyobTahmyQoh4LQfADlAoGAVv9o5Sd3RqoAa/oLIxzSxsPwEomFZ0fw1H9l1ZmVoonpkT9MU32K84sMHE8yQA6UaNo5g+5of0Whe+BPRTdmzoCx7OjRhcR8toaHlWVr8kxSg67HbolrpXi0Y03SesHPpRazRTNVEN5Ev1tfBD5DeoFl43sQM1TeH67qBGmlSng=";
    private static final String ALIPAY_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEArOao9PgBVmlTBKDtTNo8PL0SlLEx/NE3EcucGpGBI2/eKJIueDL1Icg2VUB6IHDzXyedf02jfEjQH1z7fUVT4sUFnz9WPX2o9xTkOz9oHvee6rTfXU6qxbrBrOQcnNomx68ISMWyIOfBHQg3HvvWs58+Ptnr3FRfhDjGzZghIePV+V0rRUFKrpt3I3muko5gXJGjzHw/BVHY0bdRCqTzBI2cRi4pIqViw7x250Y32Dre+jMbwe+E8cPl9Yod2/2sGuyDnBokSwuhF5SWKliJt+GGkbdQCWy4yqBFsOg5SyWFFkRsqCLae8fpsJyHsYl4qFYoqtQgNidqrAdYde1Z0QIDAQAB";
    //    private static final String RETURN_URL = "http://localhost:5173/client/alipayReturn";
    private static final String RETURN_URL =
            "http://localhost:8080/wallet/alipayReturn";
    private static final String NOTIFY_URL = "http://localhost:8080/wallet/alipayNotify";

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

//    @PostMapping("/recharge")
//    public Map<String, Object> recharge(@RequestBody Map<String, Object> body, HttpServletRequest request) {
//        Long userId = getUserId(request);
//        BigDecimal amount = new BigDecimal(body.get("amount").toString());
//        String remark = body.get("remark") != null ? body.get("remark").toString() : null;
//        try {
//            walletService.recharge(userId, amount, remark);
//            Wallet wallet = walletService.getByUserId(userId);
//            Map<String, Object> result = new HashMap<>();
//            result.put("code", 200);
//            result.put("message", "充值成功");
//            result.put("data", wallet);
//            return result;
//        } catch (Exception e) {
//            Map<String, Object> result = new HashMap<>();
//            result.put("code", 500);
//            result.put("message", e.getMessage());
//            return result;
//        }
//    }

//    @PostMapping("/recharge")
//    public Map<String, Object> recharge(@RequestBody Map<String, Object> body, HttpServletRequest request) {
//        Long userId = getUserId(request);
//        BigDecimal amount = new BigDecimal(body.get("amount").toString());
//        String remark = body.get("remark") != null ? body.get("remark").toString() : null;
//        try {
//            // 生成支付订单，并返回支付宝支付页面信息
//            String payHtml = generateAlipayOrder(userId, amount, remark);
//            Map<String, Object> result = new HashMap<>();
//            result.put("code", 200);
//            result.put("payHtml", payHtml);
//            return result;
//        } catch (Exception e) {
//            Map<String, Object> result = new HashMap<>();
//            result.put("code", 500);
//            result.put("message", e.getMessage());
//            return result;
//        }
//    }

    @PostMapping("/recharge")
    public Map<String, Object> recharge(@RequestBody Map<String, Object> body, HttpServletRequest request) throws Exception {
        Long userId = getUserId(request);
        BigDecimal amount = new BigDecimal(body.get("amount").toString());
        String remark = body.get("remark") != null ? body.get("remark").toString() : null;

        // 生成订单号
        String orderNo = "wallet_" + userId + "_" + System.currentTimeMillis();

        // 存入 WalletOrder
        WalletOrder order = new WalletOrder();
        order.setUserId(userId);
        order.setOrderNo(orderNo);
        order.setAmount(amount);
        order.setStatus(0);
        order.setCreateTime(LocalDateTime.now());
        order.setRemark(remark);
        walletOrderMapper.insert(order);

        // 生成支付宝表单
        String payHtml = generateAlipayOrder(orderNo, amount, remark);

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("payHtml", payHtml);
        return result;
    }



    /**
     * 生成支付宝支付表单
     */
//    private String generateAlipayOrder(Long userId, BigDecimal amount, String remark) throws AlipayApiException {
//        AlipayClient alipayClient = new DefaultAlipayClient(
//                ALIPAY_GATEWAY,
//                APP_ID,
//                PRIVATE_KEY,
//                "json",
//                "UTF-8",
//                ALIPAY_PUBLIC_KEY,
//                "RSA2"
//        );
//
//        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
//        request.setReturnUrl(RETURN_URL);
//        request.setNotifyUrl(NOTIFY_URL);
//
//        AlipayTradePagePayModel model = new AlipayTradePagePayModel();
//        model.setOutTradeNo("wallet" + System.currentTimeMillis() + userId); // 唯一订单号
//        model.setProductCode("FAST_INSTANT_TRADE_PAY");
//        model.setTotalAmount(amount.toPlainString());
//        model.setSubject("钱包充值");
//        model.setBody(remark != null ? remark : "钱包充值");
//
//        request.setBizModel(model);
//
//        // 返回支付表单 HTML
//        return alipayClient.pageExecute(request).getBody();
//    }

    private String generateAlipayOrder(String orderNo, BigDecimal amount, String remark) throws AlipayApiException {
        AlipayClient alipayClient = new DefaultAlipayClient(
                ALIPAY_GATEWAY,
                APP_ID,
                PRIVATE_KEY,
                "json",
                "UTF-8",
                ALIPAY_PUBLIC_KEY,
                "RSA2"
        );

        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        request.setReturnUrl(RETURN_URL);
        request.setNotifyUrl(NOTIFY_URL);

        AlipayTradePagePayModel model = new AlipayTradePagePayModel();
        model.setOutTradeNo(orderNo);
        model.setProductCode("FAST_INSTANT_TRADE_PAY");
        model.setTotalAmount(amount.toPlainString());
        model.setSubject("钱包充值");
        model.setBody(remark != null ? remark : "钱包充值");

        request.setBizModel(model);

        return alipayClient.pageExecute(request).getBody();
    }



    /**
     * 支付宝同步回调（H5支付完成后跳转）
     */
//    @GetMapping("/alipayReturn")
//    public void alipayReturn(HttpServletRequest request, HttpServletResponse response) throws IOException {
//
//        response.getWriter().write("<h1>支付完成，请关闭窗口</h1>");
//    }
//    @GetMapping("/alipayReturn")
//    public void alipayReturn(
//            HttpServletRequest request,
//            HttpServletResponse response) throws Exception {
//
//        Map<String,String> params = new HashMap<>();
//
//        Map<String,String[]> requestParams =
//                request.getParameterMap();
//
//        for (String name : requestParams.keySet()) {
//
//            String[] values = requestParams.get(name);
//
//            StringBuilder valueStr = new StringBuilder();
//
//            for (int i = 0; i < values.length; i++) {
//
//                valueStr.append(values[i]);
//
//                if(i != values.length - 1){
//                    valueStr.append(",");
//                }
//            }
//
//            params.put(name,valueStr.toString());
//        }
//
//        boolean signVerified =
//                AlipaySignature.rsaCheckV1(
//                        params,
//                        ALIPAY_PUBLIC_KEY,
//                        "UTF-8",
//                        "RSA2"
//                );
//
//        if(!signVerified){
//
//            response.getWriter().write("验签失败");
//            return;
//        }
//
//        String orderNo =
//                request.getParameter("out_trade_no");
//
//        String[] arr =
//                orderNo.split("_");
//
//        Long userId =
//                Long.valueOf(arr[1]);
//
//        BigDecimal amount =
//                new BigDecimal(arr[2]);
//
//        walletService.recharge(
//                userId,
//                amount,
//                "支付宝充值"
//        );
//
//        response.sendRedirect(
//                "http://localhost:5173/client/wallet"
//        );
//    }

//    @GetMapping("/alipayReturn")
//    public void alipayReturn(HttpServletRequest request, HttpServletResponse response) throws IOException {
//        try {
//            // 1. 收集支付宝返回参数
//            Map<String, String> params = new HashMap<>();
//            request.getParameterMap().forEach((name, values) -> {
//                String valueStr = String.join(",", values);
//                params.put(name, valueStr);
//            });
//
//            // 2. 验签
//            boolean signVerified = AlipaySignature.rsaCheckV1(
//                    params,
//                    ALIPAY_PUBLIC_KEY,
//                    "UTF-8",
//                    "RSA2"
//            );
//            if (!signVerified) {
//                response.getWriter().write("验签失败");
//                return;
//            }
//
//            // 3. 解析订单号
//            String orderNo = request.getParameter("out_trade_no");
//            if (orderNo == null) {
//                response.getWriter().write("订单号为空");
//                return;
//            }
//
//            String[] arr = orderNo.split("_");
//            if (arr.length < 4) {
//                response.getWriter().write("订单格式错误：" + orderNo);
//                return;
//            }
//
//            Long userId;
//            BigDecimal amount;
//            try {
//                userId = Long.valueOf(arr[1]);
//                amount = new BigDecimal(arr[2]);
//
//            } catch (Exception e) {
//                response.getWriter().write("解析订单号失败：" + orderNo);
//                return;
//            }
//
//            // 4. 防重复充值（可选，如果没有订单表只能靠 session 或其他方式）
//            String tradeNo = request.getParameter("trade_no"); // 支付宝交易号，可记录在数据库防重复
//            // TODO: 这里可以增加 WalletOrder 表判断是否已经处理过
//
//            // 5. 充值到钱包
//            walletService.recharge(userId, amount, "支付宝充值");
//
//            // 6. 跳转回前端钱包页面
//            response.sendRedirect("http://localhost:5173/client/wallet?success=1");
//        } catch (Exception e) {
//            response.getWriter().write("回调处理异常：" + e.getMessage());
//        }
//    }

    @GetMapping("/alipayReturn")
    public void alipayReturn(HttpServletRequest request, HttpServletResponse response) throws Exception {

        Map<String, String> params = new HashMap<>();
        Map<String, String[]> requestParams = request.getParameterMap();
        for (String name : requestParams.keySet()) {
            String[] values = requestParams.get(name);
            StringBuilder valueStr = new StringBuilder();
            for (int i = 0; i < values.length; i++) {
                valueStr.append(values[i]);
                if (i != values.length - 1) valueStr.append(",");
            }
            params.put(name, valueStr.toString());
        }

        boolean signVerified = AlipaySignature.rsaCheckV1(params, ALIPAY_PUBLIC_KEY, "UTF-8", "RSA2");
        if (!signVerified) {
            response.getWriter().write("验签失败");
            return;
        }

        String orderNo = request.getParameter("out_trade_no");
        if (orderNo == null) {
            response.getWriter().write("订单号为空");
            return;
        }

        WalletOrder order = walletOrderMapper.selectByOrderNo(orderNo);
        if (order == null) {
            response.getWriter().write("订单不存在");
            return;
        }

        // 防重复充值
        if (order.getStatus() == 1) {
            response.sendRedirect("http://localhost:5173/client/wallet");
            return;
        }

        int row = walletOrderMapper.updatePaySuccess(orderNo, LocalDateTime.now());
        if (row == 0) {
            response.sendRedirect("http://localhost:5173/client/wallet");
            return;
        }

        // 充值到账
        walletService.recharge(order.getUserId(), order.getAmount(), "支付宝充值");

        response.sendRedirect("http://localhost:5173/client/wallet");
    }


    /**
     * 支付宝异步回调
     */
    @PostMapping("/alipayNotify")
    public String alipayNotify(HttpServletRequest request) {
        // TODO: 验签 & 处理订单逻辑
        // 支付成功后调用 walletService.rechargeByAlipay(userId, amount, tradeNo)
        return "success";
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
