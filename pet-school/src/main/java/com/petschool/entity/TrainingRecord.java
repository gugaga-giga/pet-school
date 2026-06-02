package com.petschool.entity;

import java.io.Serializable;
import java.time.LocalDate;

public class TrainingRecord implements Serializable {
    private Long id;
    private Long petId;
    private Long trainerId;
    private String itemName;
    private String content;
    private LocalDate recordDate;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getPetId() { return petId; }
    public void setPetId(Long petId) { this.petId = petId; }
    public Long getTrainerId() { return trainerId; }
    public void setTrainerId(Long trainerId) { this.trainerId = trainerId; }
    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public LocalDate getRecordDate() { return recordDate; }
    public void setRecordDate(LocalDate recordDate) { this.recordDate = recordDate; }
}
