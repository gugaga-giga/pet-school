package com.petschool.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class MedicalOrder implements Serializable {
    private Long id;
    private String orderNo;
    private Long userId;
    private Long petId;
    private Long doctorId;
    private Long departmentId;
    private LocalDateTime appointmentTime;
    private String symptom;
    private BigDecimal price;
    private Integer status;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private String petName;
    private String ownerName;
    private String doctorName;
    private String departmentName;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getPetId() { return petId; }
    public void setPetId(Long petId) { this.petId = petId; }
    public Long getDoctorId() { return doctorId; }
    public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }
    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }
    public LocalDateTime getAppointmentTime() { return appointmentTime; }
    public void setAppointmentTime(LocalDateTime appointmentTime) { this.appointmentTime = appointmentTime; }
    public String getSymptom() { return symptom; }
    public void setSymptom(String symptom) { this.symptom = symptom; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
    public String getPetName() { return petName; }
    public void setPetName(String petName) { this.petName = petName; }
    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }
    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }
    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }
}
