package com.petschool.entity;

import java.io.Serializable;

public class Room implements Serializable {
    private Long id;
    private Long typeId;
    private String roomNumber;
    private Integer status;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getTypeId() { return typeId; }
    public void setTypeId(Long typeId) { this.typeId = typeId; }
    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
}
