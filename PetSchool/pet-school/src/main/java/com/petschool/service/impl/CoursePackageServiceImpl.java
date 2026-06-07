package com.petschool.service.impl;

import com.petschool.entity.CoursePackage;
import com.petschool.mapper.CoursePackageMapper;
import com.petschool.mapper.PackageItemMapper;
import com.petschool.service.CoursePackageService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CoursePackageServiceImpl implements CoursePackageService {

    private final CoursePackageMapper coursePackageMapper;
    private final PackageItemMapper packageItemMapper;

    public CoursePackageServiceImpl(CoursePackageMapper coursePackageMapper, PackageItemMapper packageItemMapper) {
        this.coursePackageMapper = coursePackageMapper;
        this.packageItemMapper = packageItemMapper;
    }

    @Override
    public List<CoursePackage> getByCourseId(Long courseId) {
        return coursePackageMapper.selectByCourseId(courseId);
    }

    @Override
    public CoursePackage getById(Long id) {
        return coursePackageMapper.selectById(id);
    }

    @Override
    public int add(CoursePackage coursePackage) {
        return coursePackageMapper.insert(coursePackage);
    }

    @Override
    public int update(CoursePackage coursePackage) {
        return coursePackageMapper.update(coursePackage);
    }

    @Override
    public int deleteById(Long id) {
        // 先删除套餐项
        packageItemMapper.deleteByPackageId(id);
        return coursePackageMapper.deleteById(id);
    }
}
