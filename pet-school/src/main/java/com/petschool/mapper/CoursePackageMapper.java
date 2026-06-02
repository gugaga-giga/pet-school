package com.petschool.mapper;

import com.petschool.entity.CoursePackage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CoursePackageMapper {
    List<CoursePackage> selectByCourseId(@Param("courseId") Long courseId);
    CoursePackage selectById(@Param("id") Long id);
}
