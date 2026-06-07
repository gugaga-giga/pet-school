package com.petschool.entity;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class DewormingRecord implements Serializable {
    private Long id;
    private Long petId;
    private String dewormingName;
    private LocalDate dewormingDate;
    private LocalDate nextDueDate;
    private Integer status;
    private LocalDateTime createTime;
    private String petName;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getPetId() { return petId; }
    public void setPetId(Long petId) { this.petId = petId; }
    public String getDewormingName() { return dewormingName; }
    public void setDewormingName(String dewormingName) { this.dewormingName = dewormingName; }
    public LocalDate getDewormingDate() { return dewormingDate; }
    public void setDewormingDate(LocalDate dewormingDate) { this.dewormingDate = dewormingDate; }
    public LocalDate getNextDueDate() { return nextDueDate; }
    public void setNextDueDate(LocalDate nextDueDate) { this.nextDueDate = nextDueDate; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public String getPetName() { return petName; }
    public void setPetName(String petName) { this.petName = petName; }
}
