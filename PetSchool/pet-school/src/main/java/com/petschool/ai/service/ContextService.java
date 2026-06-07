package com.petschool.ai.service;

import com.petschool.ai.context.CourseContext;
import com.petschool.ai.context.PetContext;
import com.petschool.ai.context.UserContext;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Map;

/**
 * 上下文构建服务接口
 */
public interface ContextService {

    /**
     * 构建用户上下文
     */
    UserContext buildUserContext(HttpServletRequest request);

    /**
     * 构建宠物上下文列表
     */
    List<PetContext> buildPetContext(Long userId);

    /**
     * 构建课程上下文列表
     */
    List<CourseContext> buildCourseContext(Long userId);

    /**
     * 构建完整上下文（合并所有上下文信息）
     */
    Map<String, Object> buildFullContext(HttpServletRequest request);

    /**
     * 构建课程目录（所有可用课程）
     */
    List<CourseContext> buildCourseCatalog();

    /**
     * 构建订单上下文
     */
    Map<String, Object> buildOrderContext(Long userId);
}
