package com.petschool.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Department implements Serializable {
    private Long id;
    private String name;
    private String description;
    private String icon;
    private Integer status;
    private Integer sort;
    private LocalDateTime createTime;
    private Double price;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public Integer getSort() { return sort; }
    public void setSort(Integer sort) { this.sort = sort; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
}
