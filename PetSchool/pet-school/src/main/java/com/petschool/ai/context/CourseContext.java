package com.petschool.ai.context;

import java.math.BigDecimal;

/**
 * 课程上下文，包含课程信息及用户关联状态
 */
public class CourseContext {

    private Long courseId;
    private String courseName;
    private String description;
    private BigDecimal price;
    private String categoryName;
    private String status; // purchased/learning/favorite

    public CourseContext() {}

    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }
    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    /**
     * 获取状态描述
     */
    public String getStatusDesc() {
        if (status == null) return "未知";
        return switch (status) {
            case "purchased" -> "已购买";
            case "learning" -> "学习中";
            case "favorite" -> "已收藏";
            case "available" -> "可报名";
            case "completed" -> "已完成";
            default -> status;
        };
    }
}
