package com.petschool.service.impl;

import com.petschool.entity.*;
import com.petschool.mapper.*;
import com.petschool.service.MedicalOrderService;
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
public class MedicalOrderServiceImpl implements MedicalOrderService {

    private final MedicalOrderMapper medicalOrderMapper;
    private final PetMapper petMapper;
    private final UserMapper userMapper;
    private final DoctorMapper doctorMapper;
    private final DepartmentMapper departmentMapper;
    private final WalletService walletService;

    public MedicalOrderServiceImpl(MedicalOrderMapper medicalOrderMapper,
                                   PetMapper petMapper,
                                   UserMapper userMapper,
                                   DoctorMapper doctorMapper,
                                   DepartmentMapper departmentMapper,
                                   WalletService walletService) {
        this.medicalOrderMapper = medicalOrderMapper;
        this.petMapper = petMapper;
        this.userMapper = userMapper;
        this.doctorMapper = doctorMapper;
        this.departmentMapper = departmentMapper;
        this.walletService = walletService;
    }

    @Override
    public MedicalOrder create(Long userId, Long petId, Long doctorId, Long departmentId, LocalDateTime appointmentTime, String symptom) {
        Pet pet = petMapper.selectById(petId);
        User user = userMapper.selectById(userId);
        Doctor doctor = doctorMapper.selectById(doctorId);
        Department department = departmentMapper.selectById(departmentId);

        String orderNo = generateOrderNo();
        BigDecimal price = department != null && department.getPrice() != null
                ? BigDecimal.valueOf(department.getPrice())
                : BigDecimal.ZERO;

        MedicalOrder order = new MedicalOrder();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setPetId(petId);
        order.setDoctorId(doctorId);
        order.setDepartmentId(departmentId);
        order.setAppointmentTime(appointmentTime);
        order.setSymptom(symptom);
        order.setPrice(price);
        order.setStatus(0);
        order.setPetName(pet != null ? pet.getName() : "");
        order.setOwnerName(user != null ? user.getUsername() : "");
        order.setDoctorName(doctor != null ? doctor.getName() : "");
        order.setDepartmentName(department != null ? department.getName() : "");

        medicalOrderMapper.insert(order);
        return order;
    }

    @Override
    public List<MedicalOrder> getByUserId(Long userId) {
        return medicalOrderMapper.selectByUserId(userId);
    }

    @Override
    public Map<String, Object> page(String keyword, Integer status, int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        List<MedicalOrder> list = medicalOrderMapper.selectPage(keyword, status, offset, pageSize);
        int total = medicalOrderMapper.selectPageCount(keyword, status);
        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);
        result.put("page", page);
        result.put("pageSize", pageSize);
        result.put("totalPages", (total + pageSize - 1) / pageSize);
        return result;
    }

    @Override
    public int updateStatus(Long id, Integer status) {
        return medicalOrderMapper.updateStatus(id, status);
    }

    @Override
    @Transactional
    public int cancel(Long id, Long userId) {
        MedicalOrder order = medicalOrderMapper.selectById(id);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作此订单");
        }
        if (order.getStatus() != 0 && order.getStatus() != 1) {
            throw new RuntimeException("订单状态不允许取消");
        }
        if (order.getStatus() == 1 && order.getPrice() != null && order.getPrice().compareTo(BigDecimal.ZERO) > 0) {
            walletService.refund(userId, order.getPrice(), "medical", order.getId(), "医疗退款-" + order.getOrderNo());
        }
        return medicalOrderMapper.updateStatus(id, 4);
    }

    @Override
    public MedicalOrder getById(Long id) {
        return medicalOrderMapper.selectById(id);
    }

    private String generateOrderNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int random = ThreadLocalRandom.current().nextInt(1000, 9999);
        return "MED" + timestamp + random;
    }
}
