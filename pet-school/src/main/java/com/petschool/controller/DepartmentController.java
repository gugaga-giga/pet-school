package com.petschool.controller;

import com.petschool.entity.Department;
import com.petschool.service.DepartmentService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/medical/department")
public class DepartmentController {

    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    private Map<String, Object> ok(Object data) { Map<String, Object> r = new HashMap<>(); r.put("code", 200); r.put("data", data); return r; }
    private Map<String, Object> ok(String msg, Object data) { Map<String, Object> r = new HashMap<>(); r.put("code", 200); r.put("message", msg); r.put("data", data); return r; }
    private Map<String, Object> fail(String msg) { Map<String, Object> r = new HashMap<>(); r.put("code", 500); r.put("message", msg); return r; }

    @GetMapping("/list")
    public Map<String, Object> list() {
        return ok(departmentService.listAll());
    }

    @GetMapping("/active")
    public Map<String, Object> active() {
        return ok(departmentService.listActive());
    }

    @PostMapping("/add")
    public Map<String, Object> add(@RequestBody Department department) {
        try {
            int rows = departmentService.add(department);
            return rows > 0 ? ok("添加成功", department) : fail("添加失败");
        } catch (RuntimeException e) {
            return fail(e.getMessage());
        }
    }

    @PutMapping("/update")
    public Map<String, Object> update(@RequestBody Department department) {
        try {
            int rows = departmentService.update(department);
            return rows > 0 ? ok("更新成功", null) : fail("更新失败");
        } catch (RuntimeException e) {
            return fail(e.getMessage());
        }
    }

    @DeleteMapping("/delete/{id}")
    public Map<String, Object> delete(@PathVariable Long id) {
        int rows = departmentService.deleteById(id);
        return rows > 0 ? ok("删除成功", null) : fail("删除失败");
    }
}
