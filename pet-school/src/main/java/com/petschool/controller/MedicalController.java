package com.petschool.controller;

import com.petschool.entity.*;
import com.petschool.service.*;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/medical")
public class MedicalController {

    private final MedicalOrderService medicalOrderService;
    private final MedicalRecordService medicalRecordService;
    private final DepartmentService departmentService;
    private final DoctorService doctorService;
    private final VaccineRecordService vaccineRecordService;
    private final DewormingRecordService dewormingRecordService;
    private final WalletService walletService;

    public MedicalController(MedicalOrderService medicalOrderService, MedicalRecordService medicalRecordService, DepartmentService departmentService, DoctorService doctorService, VaccineRecordService vaccineRecordService, DewormingRecordService dewormingRecordService, WalletService walletService) {
        this.medicalOrderService = medicalOrderService;
        this.medicalRecordService = medicalRecordService;
        this.departmentService = departmentService;
        this.doctorService = doctorService;
        this.vaccineRecordService = vaccineRecordService;
        this.dewormingRecordService = dewormingRecordService;
        this.walletService = walletService;
    }

    private Map<String, Object> ok(Object data) { Map<String, Object> r = new HashMap<>(); r.put("code", 200); r.put("data", data); return r; }
    private Map<String, Object> ok(String msg, Object data) { Map<String, Object> r = new HashMap<>(); r.put("code", 200); r.put("message", msg); r.put("data", data); return r; }
    private Map<String, Object> fail(String msg) { Map<String, Object> r = new HashMap<>(); r.put("code", 500); r.put("message", msg); return r; }

    @PostMapping("/order/create")
    public Map<String, Object> createOrder(@RequestBody Map<String, Object> params) {
        try {
            Long userId = Long.valueOf(params.get("userId").toString());
            Long petId = Long.valueOf(params.get("petId").toString());
            Long doctorId = Long.valueOf(params.get("doctorId").toString());
            Long departmentId = Long.valueOf(params.get("departmentId").toString());
            LocalDateTime appointmentTime = LocalDateTime.parse(params.get("appointmentTime").toString());
            String symptom = params.get("symptom") != null ? params.get("symptom").toString() : null;
            MedicalOrder order = medicalOrderService.create(userId, petId, doctorId, departmentId, appointmentTime, symptom);
            return ok("预约成功", order);
        } catch (RuntimeException e) {
            return fail(e.getMessage());
        }
    }

    @GetMapping("/order/my")
    public Map<String, Object> myOrders(@RequestParam Long userId) {
        return ok(medicalOrderService.getByUserId(userId));
    }

    @PutMapping("/order/cancel/{id}")
    public Map<String, Object> cancelOrder(@PathVariable Long id, @RequestParam Long userId) {
        try {
            int rows = medicalOrderService.cancel(id, userId);
            return rows > 0 ? ok("取消成功", null) : fail("取消失败");
        } catch (RuntimeException e) {
            return fail(e.getMessage());
        }
    }

    @GetMapping("/order/detail/{id}")
    public Map<String, Object> orderDetail(@PathVariable Long id) {
        MedicalOrder order = medicalOrderService.getById(id);
        return order != null ? ok(order) : fail("订单不存在");
    }

    @GetMapping("/record/by-order/{orderId}")
    public Map<String, Object> recordByOrder(@PathVariable Long orderId) {
        return ok(medicalRecordService.getByMedicalOrderId(orderId));
    }

    @GetMapping("/record/by-pet/{petId}")
    public Map<String, Object> recordByPet(@PathVariable Long petId) {
        return ok(medicalRecordService.getByPetId(petId));
    }

    @GetMapping("/vaccine/upcoming")
    public Map<String, Object> vaccineUpcoming(@RequestParam Long userId, @RequestParam(defaultValue = "7") int days) {
        return ok(vaccineRecordService.getUpcoming(userId, days));
    }

    @GetMapping("/vaccine/expired")
    public Map<String, Object> vaccineExpired(@RequestParam Long userId) {
        return ok(vaccineRecordService.getExpired(userId));
    }

    @GetMapping("/deworming/upcoming")
    public Map<String, Object> dewormingUpcoming(@RequestParam Long userId, @RequestParam(defaultValue = "3") int days) {
        return ok(dewormingRecordService.getUpcoming(userId, days));
    }

    @GetMapping("/deworming/expired")
    public Map<String, Object> dewormingExpired(@RequestParam Long userId) {
        return ok(dewormingRecordService.getExpired(userId));
    }

    @GetMapping("/order/page")
    public Map<String, Object> orderPage(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ok(medicalOrderService.page(keyword, status, page, pageSize));
    }

    @PutMapping("/order/status")
    public Map<String, Object> updateOrderStatus(@RequestBody Map<String, Object> params) {
        try {
            if (params.get("id") == null || params.get("status") == null) {
                return fail("参数不完整");
            }
            Long id = Long.valueOf(params.get("id").toString());
            Integer status = Integer.valueOf(params.get("status").toString());
            MedicalOrder order = medicalOrderService.getById(id);
            if (order == null) return fail("订单不存在");
            if (status == 1 && order.getStatus() == 0) {
                walletService.deduct(order.getUserId(), order.getPrice(), "medical", order.getId(), "医疗支付-" + order.getOrderNo());
            }
            if (status == 4 && (order.getStatus() == 0 || order.getStatus() == 1)) {
                if (order.getStatus() == 1 && order.getPrice() != null && order.getPrice().compareTo(java.math.BigDecimal.ZERO) > 0) {
                    walletService.refund(order.getUserId(), order.getPrice(), "medical", order.getId(), "医疗退款-" + order.getOrderNo());
                }
            }
            int rows = medicalOrderService.updateStatus(id, status);
            return rows > 0 ? ok("状态更新成功", null) : fail("状态更新失败");
        } catch (RuntimeException e) {
            return fail(e.getMessage());
        }
    }

    @PostMapping("/record/create")
    public Map<String, Object> createRecord(@RequestBody Map<String, Object> params) {
        try {
            Long medicalOrderId = Long.valueOf(params.get("medicalOrderId").toString());
            String chiefComplaint = params.get("chiefComplaint") != null ? params.get("chiefComplaint").toString() : null;
            String physicalExam = params.get("physicalExam") != null ? params.get("physicalExam").toString() : null;
            String diagnosis = params.get("diagnosis") != null ? params.get("diagnosis").toString() : null;
            String medicalAdvice = params.get("medicalAdvice") != null ? params.get("medicalAdvice").toString() : null;
            String medication = params.get("medication") != null ? params.get("medication").toString() : null;
            String remark = params.get("remark") != null ? params.get("remark").toString() : null;
            MedicalRecord record = medicalRecordService.create(medicalOrderId, chiefComplaint, physicalExam, diagnosis, medicalAdvice, medication, remark);
            return ok("录入成功", record);
        } catch (RuntimeException e) {
            return fail(e.getMessage());
        }
    }

    @GetMapping("/record/page")
    public Map<String, Object> recordPage(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ok(medicalRecordService.page(keyword, page, pageSize));
    }

    @PutMapping("/record/update")
    public Map<String, Object> updateRecord(@RequestBody MedicalRecord record) {
        try {
            int rows = medicalRecordService.update(record);
            return rows > 0 ? ok("更新成功", null) : fail("更新失败");
        } catch (RuntimeException e) {
            return fail(e.getMessage());
        }
    }

    @DeleteMapping("/record/delete/{id}")
    public Map<String, Object> deleteRecord(@PathVariable Long id) {
        try {
            int rows = medicalRecordService.deleteById(id);
            return rows > 0 ? ok("删除成功", null) : fail("删除失败");
        } catch (RuntimeException e) {
            return fail(e.getMessage());
        }
    }

    @PostMapping("/vaccine/add")
    public Map<String, Object> addVaccine(@RequestBody VaccineRecord record) {
        try {
            int rows = vaccineRecordService.add(record);
            return rows > 0 ? ok("添加成功", record) : fail("添加失败");
        } catch (RuntimeException e) {
            return fail(e.getMessage());
        }
    }

    @PutMapping("/vaccine/update")
    public Map<String, Object> updateVaccine(@RequestBody VaccineRecord record) {
        try {
            int rows = vaccineRecordService.update(record);
            return rows > 0 ? ok("更新成功", null) : fail("更新失败");
        } catch (RuntimeException e) {
            return fail(e.getMessage());
        }
    }

    @DeleteMapping("/vaccine/delete/{id}")
    public Map<String, Object> deleteVaccine(@PathVariable Long id) {
        try {
            int rows = vaccineRecordService.deleteById(id);
            return rows > 0 ? ok("删除成功", null) : fail("删除失败");
        } catch (RuntimeException e) {
            return fail(e.getMessage());
        }
    }

    @PostMapping("/deworming/add")
    public Map<String, Object> addDeworming(@RequestBody DewormingRecord record) {
        try {
            int rows = dewormingRecordService.add(record);
            return rows > 0 ? ok("添加成功", record) : fail("添加失败");
        } catch (RuntimeException e) {
            return fail(e.getMessage());
        }
    }

    @PutMapping("/deworming/update")
    public Map<String, Object> updateDeworming(@RequestBody DewormingRecord record) {
        try {
            int rows = dewormingRecordService.update(record);
            return rows > 0 ? ok("更新成功", null) : fail("更新失败");
        } catch (RuntimeException e) {
            return fail(e.getMessage());
        }
    }

    @DeleteMapping("/deworming/delete/{id}")
    public Map<String, Object> deleteDeworming(@PathVariable Long id) {
        try {
            int rows = dewormingRecordService.deleteById(id);
            return rows > 0 ? ok("删除成功", null) : fail("删除失败");
        } catch (RuntimeException e) {
            return fail(e.getMessage());
        }
    }

    @GetMapping("/vaccine/list")
    public Map<String, Object> vaccineList() {
        return ok(vaccineRecordService.listAll());
    }

    @GetMapping("/deworming/list")
    public Map<String, Object> dewormingList() {
        return ok(dewormingRecordService.listAll());
    }
}
