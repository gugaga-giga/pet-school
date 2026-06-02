package com.petschool.service;

import com.petschool.entity.Department;

import java.util.List;

public interface DepartmentService {
    int add(Department department);
    Department getById(Long id);
    List<Department> listAll();
    List<Department> listActive();
    int update(Department department);
    int deleteById(Long id);
}
