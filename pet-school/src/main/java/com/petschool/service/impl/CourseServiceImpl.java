package com.petschool.service.impl;

import com.petschool.entity.Course;
import com.petschool.mapper.CourseMapper;
import com.petschool.service.CourseService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CourseServiceImpl implements CourseService {

    private final CourseMapper courseMapper;

    public CourseServiceImpl(CourseMapper courseMapper) {
        this.courseMapper = courseMapper;
    }

    @Override
    public List<Course> listAll() {
        return courseMapper.selectAll();
    }

    @Override
    public Course getById(Long id) {
        return courseMapper.selectById(id);
    }

    @Override
    public List<Course> getByCategoryId(Long categoryId) {
        return courseMapper.selectByCategoryId(categoryId);
    }

    @Override
    public int add(Course course) {
        return courseMapper.insert(course);
    }

    @Override
    public int update(Course course) {
        return courseMapper.update(course);
    }

    @Override
    public int deleteById(Long id) {
        return courseMapper.deleteById(id);
    }
}
