package com.petschool.service.impl;

import com.petschool.entity.CoursePackage;
import com.petschool.mapper.CoursePackageMapper;
import com.petschool.service.CoursePackageService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CoursePackageServiceImpl implements CoursePackageService {

    private final CoursePackageMapper coursePackageMapper;

    public CoursePackageServiceImpl(CoursePackageMapper coursePackageMapper) {
        this.coursePackageMapper = coursePackageMapper;
    }

    @Override
    public List<CoursePackage> getByCourseId(Long courseId) {
        return coursePackageMapper.selectByCourseId(courseId);
    }

    @Override
    public CoursePackage getById(Long id) {
        return coursePackageMapper.selectById(id);
    }
}
