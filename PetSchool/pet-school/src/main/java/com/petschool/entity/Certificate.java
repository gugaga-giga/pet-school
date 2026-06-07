package com.petschool.entity;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class Certificate implements Serializable {
    private Long id;
    private String certificateNo;
    private Long userId;
    private Long petId;
    private Long orderId;
    private Long courseId;
    private String courseName;
    private String petName;
    private String ownerName;
    private LocalDate graduateDate;
    private LocalDate issueDate;
    private Long templateId;
    private String certificateUrl;
    private Integer status;
    private LocalDateTime createTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCertificateNo() { return certificateNo; }
    public void setCertificateNo(String certificateNo) { this.certificateNo = certificateNo; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getPetId() { return petId; }
    public void setPetId(Long petId) { this.petId = petId; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }
    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }
    public String getPetName() { return petName; }
    public void setPetName(String petName) { this.petName = petName; }
    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }
    public LocalDate getGraduateDate() { return graduateDate; }
    public void setGraduateDate(LocalDate graduateDate) { this.graduateDate = graduateDate; }
    public LocalDate getIssueDate() { return issueDate; }
    public void setIssueDate(LocalDate issueDate) { this.issueDate = issueDate; }
    public Long getTemplateId() { return templateId; }
    public void setTemplateId(Long templateId) { this.templateId = templateId; }
    public String getCertificateUrl() { return certificateUrl; }
    public void setCertificateUrl(String certificateUrl) { this.certificateUrl = certificateUrl; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}
