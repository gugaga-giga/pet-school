package com.petschool.service.impl;

import com.petschool.entity.CourseCategory;
import com.petschool.mapper.CourseCategoryMapper;
import com.petschool.service.CourseCategoryService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CourseCategoryServiceImpl implements CourseCategoryService {

    private final CourseCategoryMapper courseCategoryMapper;

    public CourseCategoryServiceImpl(CourseCategoryMapper courseCategoryMapper) {
        this.courseCategoryMapper = courseCategoryMapper;
    }

    @Override
    public List<CourseCategory> listAll() {
        return courseCategoryMapper.selectAll();
    }

    @Override
    public int add(CourseCategory category) {
        return courseCategoryMapper.insert(category);
    }

    @Override
    public int update(CourseCategory category) {
        return courseCategoryMapper.update(category);
    }

    @Override
    public int deleteById(Long id) {
        return courseCategoryMapper.deleteById(id);
    }
}
