package com.petschool.mapper;

import com.petschool.entity.Doctor;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DoctorMapper {
    int insert(Doctor doctor);
    Doctor selectById(@Param("id") Long id);
    List<Doctor> selectAll();
    List<Doctor> selectByDepartmentId(@Param("departmentId") Long departmentId);
    List<Doctor> selectActive();
    int update(Doctor doctor);
    int deleteById(@Param("id") Long id);
}
