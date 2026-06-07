package com.petschool.mapper;

import com.petschool.entity.CourseCategory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CourseCategoryMapper {
    List<CourseCategory> selectAll();
    int insert(CourseCategory category);
    int update(CourseCategory category);
    int deleteById(@Param("id") Long id);
}
