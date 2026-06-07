package com.petschool.entity;

import java.io.Serializable;
import java.math.BigDecimal;

public class CoursePackage implements Serializable {
    private Long id;
    private Long courseId;
    private String name;
    private BigDecimal price;
    private Integer level;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public Integer getLevel() { return level; }
    public void setLevel(Integer level) { this.level = level; }
}
