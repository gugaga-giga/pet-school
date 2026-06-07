package com.petschool.controller;

import com.petschool.ai.service.RagSyncService;
import com.petschool.entity.*;
import com.petschool.service.*;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/course")
public class CourseController {

    private final CourseService courseService;
    private final CourseCategoryService courseCategoryService;
    private final CoursePackageService coursePackageService;
    private final PackageItemService packageItemService;
    private final RagSyncService ragSyncService;

    public CourseController(CourseService courseService, CourseCategoryService courseCategoryService,
                            CoursePackageService coursePackageService, PackageItemService packageItemService,
                            RagSyncService ragSyncService) {
        this.courseService = courseService;
        this.courseCategoryService = courseCategoryService;
        this.coursePackageService = coursePackageService;
        this.packageItemService = packageItemService;
        this.ragSyncService = ragSyncService;
    }

    @GetMapping("/list")
    public Map<String, Object> listAll() {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", courseService.listAll());
        return result;
    }

    @GetMapping("/{id}")
    public Map<String, Object> getById(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        Course course = courseService.getById(id);
        if (course != null) {
            result.put("code", 200);
            result.put("data", course);
        } else {
            result.put("code", 404);
            result.put("message", "课程不存在");
        }
        return result;
    }

    @GetMapping("/category/list")
    public Map<String, Object> categoryList() {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", courseCategoryService.listAll());
        return result;
    }

    @GetMapping("/category/{categoryId}")
    public Map<String, Object> getByCategory(@PathVariable Long categoryId) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", courseService.getByCategoryId(categoryId));
        return result;
    }

    @GetMapping("/package/list")
    public Map<String, Object> packageList(@RequestParam Long courseId) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", coursePackageService.getByCourseId(courseId));
        return result;
    }

    @GetMapping("/package/{id}")
    public Map<String, Object> packageById(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        CoursePackage pkg = coursePackageService.getById(id);
        if (pkg != null) {
            result.put("code", 200);
            result.put("data", pkg);
        } else {
            result.put("code", 404);
            result.put("message", "套餐不存在");
        }
        return result;
    }

    @GetMapping("/package/item/list")
    public Map<String, Object> packageItems(@RequestParam Long packageId) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", packageItemService.getByPackageId(packageId));
        return result;
    }

    /**
     * 同步课程到RAG知识库（管理员）
     */
    @PostMapping("/admin/sync-rag/{id}")
    public Map<String, Object> syncToRag(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        boolean success = ragSyncService.syncCourse(id);
        if (success) {
            result.put("code", 200);
            result.put("message", "同步成功");
        } else {
            result.put("code", 500);
            result.put("message", "同步失败");
        }
        return result;
    }

    /**
     * 同步所有课程到RAG知识库（管理员）
     */
    @PostMapping("/admin/sync-rag-all")
    public Map<String, Object> syncAllToRag() {
        Map<String, Object> result = new HashMap<>();
        try {
            List<Course> courses = courseService.listAll();
            int successCount = 0;
            for (Course course : courses) {
                if (ragSyncService.syncCourse(course.getId())) {
                    successCount++;
                }
            }
            result.put("code", 200);
            result.put("message", "同步完成: " + successCount + "/" + courses.size() + " 成功");
            result.put("data", Map.of("total", courses.size(), "success", successCount));
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "同步失败: " + e.getMessage());
        }
        return result;
    }
}
