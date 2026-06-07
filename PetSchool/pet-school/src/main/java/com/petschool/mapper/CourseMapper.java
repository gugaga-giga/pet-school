package com.petschool.mapper;

import com.petschool.entity.Course;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CourseMapper {
    List<Course> selectAll();
    Course selectById(@Param("id") Long id);
    List<Course> selectByCategoryId(@Param("categoryId") Long categoryId);
    int insert(Course course);
    int update(Course course);
    int deleteById(@Param("id") Long id);
}
