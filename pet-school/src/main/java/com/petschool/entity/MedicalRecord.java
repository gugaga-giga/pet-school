package com.petschool.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

public class MedicalRecord implements Serializable {
    private Long id;
    private Long medicalOrderId;
    private Long petId;
    private String doctorName;
    private String chiefComplaint;
    private String physicalExam;
    private String diagnosis;
    private String medicalAdvice;
    private String medication;
    private String remark;
    private LocalDateTime visitTime;
    private LocalDateTime createTime;
    private String petName;
    private String ownerName;
    private String departmentName;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getMedicalOrderId() { return medicalOrderId; }
    public void setMedicalOrderId(Long medicalOrderId) { this.medicalOrderId = medicalOrderId; }
    public Long getPetId() { return petId; }
    public void setPetId(Long petId) { this.petId = petId; }
    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }
    public String getChiefComplaint() { return chiefComplaint; }
    public void setChiefComplaint(String chiefComplaint) { this.chiefComplaint = chiefComplaint; }
    public String getPhysicalExam() { return physicalExam; }
    public void setPhysicalExam(String physicalExam) { this.physicalExam = physicalExam; }
    public String getDiagnosis() { return diagnosis; }
    public void setDiagnosis(String diagnosis) { this.diagnosis = diagnosis; }
    public String getMedicalAdvice() { return medicalAdvice; }
    public void setMedicalAdvice(String medicalAdvice) { this.medicalAdvice = medicalAdvice; }
    public String getMedication() { return medication; }
    public void setMedication(String medication) { this.medication = medication; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public LocalDateTime getVisitTime() { return visitTime; }
    public void setVisitTime(LocalDateTime visitTime) { this.visitTime = visitTime; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public String getPetName() { return petName; }
    public void setPetName(String petName) { this.petName = petName; }
    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }
    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }
}
