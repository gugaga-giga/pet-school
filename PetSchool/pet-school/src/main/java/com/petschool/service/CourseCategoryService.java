package com.petschool.service;

import com.petschool.entity.CourseCategory;
import java.util.List;

public interface CourseCategoryService {
    List<CourseCategory> listAll();
    int add(CourseCategory category);
    int update(CourseCategory category);
    int deleteById(Long id);
}
