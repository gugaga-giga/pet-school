package com.petschool.entity;

import java.io.Serializable;
import java.time.LocalDate;

public class SurgeryRecord implements Serializable {
    private Long id;
    private Long petId;
    private String surgeryName;
    private LocalDate surgeryDate;
    private Integer status;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getPetId() { return petId; }
    public void setPetId(Long petId) { this.petId = petId; }
    public String getSurgeryName() { return surgeryName; }
    public void setSurgeryName(String surgeryName) { this.surgeryName = surgeryName; }
    public LocalDate getSurgeryDate() { return surgeryDate; }
    public void setSurgeryDate(LocalDate surgeryDate) { this.surgeryDate = surgeryDate; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
}
