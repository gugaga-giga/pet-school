package com.petschool.service;

import com.petschool.entity.Doctor;

import java.util.List;

public interface DoctorService {
    int add(Doctor doctor);
    Doctor getById(Long id);
    List<Doctor> listAll();
    List<Doctor> listByDepartmentId(Long departmentId);
    List<Doctor> listActive();
    int update(Doctor doctor);
    int deleteById(Long id);
}
