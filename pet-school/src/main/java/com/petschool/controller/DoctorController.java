package com.petschool.controller;

import com.petschool.entity.Doctor;
import com.petschool.service.DoctorService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/medical/doctor")
public class DoctorController {

    private final DoctorService doctorService;

    public DoctorController(DoctorService doctorService) {
        this.doctorService = doctorService;
    }

    private Map<String, Object> ok(Object data) { Map<String, Object> r = new HashMap<>(); r.put("code", 200); r.put("data", data); return r; }
    private Map<String, Object> ok(String msg, Object data) { Map<String, Object> r = new HashMap<>(); r.put("code", 200); r.put("message", msg); r.put("data", data); return r; }
    private Map<String, Object> fail(String msg) { Map<String, Object> r = new HashMap<>(); r.put("code", 500); r.put("message", msg); return r; }

    @GetMapping("/list")
    public Map<String, Object> list() {
        return ok(doctorService.listAll());
    }

    @GetMapping("/active")
    public Map<String, Object> active() {
        return ok(doctorService.listActive());
    }

    @GetMapping("/by-department/{departmentId}")
    public Map<String, Object> byDepartment(@PathVariable Long departmentId) {
        return ok(doctorService.listByDepartmentId(departmentId));
    }

    @PostMapping("/add")
    public Map<String, Object> add(@RequestBody Doctor doctor) {
        try {
            int rows = doctorService.add(doctor);
            return rows > 0 ? ok("添加成功", doctor) : fail("添加失败");
        } catch (RuntimeException e) {
            return fail(e.getMessage());
        }
    }

    @PutMapping("/update")
    public Map<String, Object> update(@RequestBody Doctor doctor) {
        try {
            int rows = doctorService.update(doctor);
            return rows > 0 ? ok("更新成功", null) : fail("更新失败");
        } catch (RuntimeException e) {
            return fail(e.getMessage());
        }
    }

    @DeleteMapping("/delete/{id}")
    public Map<String, Object> delete(@PathVariable Long id) {
        int rows = doctorService.deleteById(id);
        return rows > 0 ? ok("删除成功", null) : fail("删除失败");
    }
}
