package com.petschool.entity;

import java.io.Serializable;
import java.math.BigDecimal;

public class RoomType implements Serializable {
    private Long id;
    private String name;
    private BigDecimal dailyPrice;
    private String facilities;
    private String image;
    private Integer capacity;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public BigDecimal getDailyPrice() { return dailyPrice; }
    public void setDailyPrice(BigDecimal dailyPrice) { this.dailyPrice = dailyPrice; }
    public String getFacilities() { return facilities; }
    public void setFacilities(String facilities) { this.facilities = facilities; }
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }
}
