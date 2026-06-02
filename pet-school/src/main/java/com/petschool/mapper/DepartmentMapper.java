package com.petschool.mapper;

import com.petschool.entity.Department;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DepartmentMapper {
    int insert(Department department);
    Department selectById(@Param("id") Long id);
    List<Department> selectAll();
    List<Department> selectActive();
    int update(Department department);
    int deleteById(@Param("id") Long id);
}
