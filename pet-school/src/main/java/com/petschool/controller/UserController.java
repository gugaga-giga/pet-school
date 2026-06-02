package com.petschool.controller;

import com.petschool.entity.User;
import com.petschool.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody User user) {
        Map<String, Object> result = new HashMap<>();
        int rows = userService.register(user);
        if (rows > 0) {
            result.put("code", 200);
            result.put("message", "注册成功");
            result.put("data", user);
        } else {
            result.put("code", 500);
            result.put("message", "注册失败");
        }
        return result;
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> params) {
        Map<String, Object> result = new HashMap<>();
        User user = userService.login(params.get("phone"), params.get("password"));
        if (user != null) {
            result.put("code", 200);
            result.put("message", "登录成功");
            result.put("data", user);
        } else {
            result.put("code", 401);
            result.put("message", "手机号或密码错误");
        }
        return result;
    }

    @GetMapping("/{id}")
    public Map<String, Object> getById(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        User user = userService.getById(id);
        if (user != null) {
            result.put("code", 200);
            result.put("data", user);
        } else {
            result.put("code", 404);
            result.put("message", "用户不存在");
        }
        return result;
    }

    @GetMapping("/list")
    public Map<String, Object> listAll() {
        Map<String, Object> result = new HashMap<>();
        List<User> users = userService.listAll();
        result.put("code", 200);
        result.put("data", users);
        return result;
    }
}
