package com.petschool.ai.recommendation;

import com.petschool.ai.context.CourseContext;
import com.petschool.ai.context.PetContext;
import com.petschool.entity.Course;
import com.petschool.entity.CourseCategory;
import com.petschool.mapper.CourseCategoryMapper;
import com.petschool.mapper.CourseMapper;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 课程推荐器：根据宠物信息和意图推荐课程
 */
@Component
public class CourseRecommender {

    private final CourseMapper courseMapper;
    private final CourseCategoryMapper courseCategoryMapper;

    public CourseRecommender(CourseMapper courseMapper, CourseCategoryMapper courseCategoryMapper) {
        this.courseMapper = courseMapper;
        this.courseCategoryMapper = courseCategoryMapper;
    }

    /**
     * 根据宠物信息推荐课程
     * @param petContext 宠物上下文
     * @param purchasedCourseNames 已购买课程名称列表
     * @return 推荐课程列表
     */
    public List<Map<String, Object>> recommend(PetContext petContext, List<String> purchasedCourseNames) {
        List<Course> allCourses = courseMapper.selectAll();
        if (allCourses == null || allCourses.isEmpty()) {
            return Collections.emptyList();
        }

        // 预加载分类
        List<CourseCategory> categories = courseCategoryMapper.selectAll();
        Map<Long, String> categoryMap = categories.stream()
                .collect(Collectors.toMap(CourseCategory::getId, CourseCategory::getName, (a, b) -> a));

        // 过滤已购买的课程
        Set<String> purchased = purchasedCourseNames != null ? new HashSet<>(purchasedCourseNames) : new HashSet<>();
        List<Course> availableCourses = allCourses.stream()
                .filter(c -> !purchased.contains(c.getName()))
                .collect(Collectors.toList());

        // 根据宠物特征评分
        List<Map<String, Object>> recommendations = new ArrayList<>();
        for (Course course : availableCourses) {
            int score = calculateScore(petContext, course, categoryMap);
            if (score > 0) {
                Map<String, Object> rec = new HashMap<>();
                rec.put("courseId", course.getId());
                rec.put("courseName", course.getName());
                rec.put("description", course.getDescription());
                rec.put("price", course.getMonthlyPrice());
                rec.put("categoryName", categoryMap.getOrDefault(course.getCategoryId(), "未分类"));
                rec.put("score", score);
                rec.put("reason", buildReason(petContext, course, categoryMap));
                recommendations.add(rec);
            }
        }

        // 按评分排序，取前5个
        recommendations.sort((a, b) -> (int) b.get("score") - (int) a.get("score"));
        return recommendations.stream().limit(5).collect(Collectors.toList());
    }

    /**
     * 计算课程匹配评分
     */
    private int calculateScore(PetContext pet, Course course, Map<Long, String> categoryMap) {
        int score = 50; // 基础分

        String categoryName = categoryMap.getOrDefault(course.getCategoryId(), "").toLowerCase();
        String courseName = course.getName() != null ? course.getName().toLowerCase() : "";
        String breed = pet.getBreed() != null ? pet.getBreed().toLowerCase() : "";

        // 年龄匹配
        if (pet.getAge() != null) {
            if (pet.getAge() < 1 && (courseName.contains("幼犬") || courseName.contains("幼猫") || courseName.contains("基础"))) {
                score += 30;
            } else if (pet.getAge() >= 1 && pet.getAge() < 3 && (courseName.contains("基础") || courseName.contains("入门"))) {
                score += 20;
            } else if (pet.getAge() >= 3 && (courseName.contains("进阶") || courseName.contains("高级"))) {
                score += 20;
            }
        }

        // 品种匹配
        if (!breed.isEmpty() && (courseName.contains(breed) || course.getDescription() != null && course.getDescription().toLowerCase().contains(breed))) {
            score += 25;
        }

        // 健康状态匹配
        if (pet.getHealthStatus() != null && !"健康".equals(pet.getHealthStatus()) && !"暂无记录".equals(pet.getHealthStatus())) {
            if (courseName.contains("康复") || courseName.contains("护理") || categoryName.contains("护理")) {
                score += 20;
            }
        }

        // 行为训练匹配
        if (courseName.contains("行为") || courseName.contains("纠正") || categoryName.contains("训练")) {
            score += 15;
        }

        return score;
    }

    /**
     * 构建推荐理由
     */
    private String buildReason(PetContext pet, Course course, Map<Long, String> categoryMap) {
        StringBuilder reason = new StringBuilder();
        reason.append("适合").append(pet.getPetName() != null ? pet.getPetName() : "您的宠物");

        if (pet.getAge() != null) {
            reason.append("（").append(pet.getAge()).append("岁");
            if (pet.getBreed() != null) reason.append("，").append(pet.getBreed());
            reason.append("）");
        }

        return reason.toString();
    }
}
