package com.petschool.service.impl;

import com.petschool.entity.Department;
import com.petschool.mapper.DepartmentMapper;
import com.petschool.service.DepartmentService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentMapper departmentMapper;

    public DepartmentServiceImpl(DepartmentMapper departmentMapper) {
        this.departmentMapper = departmentMapper;
    }

    @Override
    public int add(Department department) {
        return departmentMapper.insert(department);
    }

    @Override
    public Department getById(Long id) {
        return departmentMapper.selectById(id);
    }

    @Override
    public List<Department> listAll() {
        return departmentMapper.selectAll();
    }

    @Override
    public List<Department> listActive() {
        return departmentMapper.selectActive();
    }

    @Override
    public int update(Department department) {
        return departmentMapper.update(department);
    }

    @Override
    public int deleteById(Long id) {
        return departmentMapper.deleteById(id);
    }
}
