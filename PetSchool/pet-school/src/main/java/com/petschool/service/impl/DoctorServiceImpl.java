package com.petschool.service.impl;

import com.petschool.entity.Doctor;
import com.petschool.mapper.DoctorMapper;
import com.petschool.service.DoctorService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DoctorServiceImpl implements DoctorService {

    private final DoctorMapper doctorMapper;

    public DoctorServiceImpl(DoctorMapper doctorMapper) {
        this.doctorMapper = doctorMapper;
    }

    @Override
    public int add(Doctor doctor) {
        return doctorMapper.insert(doctor);
    }

    @Override
    public Doctor getById(Long id) {
        return doctorMapper.selectById(id);
    }

    @Override
    public List<Doctor> listAll() {
        return doctorMapper.selectAll();
    }

    @Override
    public List<Doctor> listByDepartmentId(Long departmentId) {
        return doctorMapper.selectByDepartmentId(departmentId);
    }

    @Override
    public List<Doctor> listActive() {
        return doctorMapper.selectActive();
    }

    @Override
    public int update(Doctor doctor) {
        return doctorMapper.update(doctor);
    }

    @Override
    public int deleteById(Long id) {
        return doctorMapper.deleteById(id);
    }
}
