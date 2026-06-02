package com.petschool.service;

import com.petschool.entity.CoursePackage;
import java.util.List;

public interface CoursePackageService {
    List<CoursePackage> getByCourseId(Long courseId);
    CoursePackage getById(Long id);
}
