package com.petschool.service;

import com.petschool.entity.Course;
import java.util.List;

public interface CourseService {
    List<Course> listAll();
    Course getById(Long id);
    List<Course> getByCategoryId(Long categoryId);
    int add(Course course);
    int update(Course course);
    int deleteById(Long id);
}
