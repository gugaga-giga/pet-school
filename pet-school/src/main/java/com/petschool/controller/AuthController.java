package com.petschool.controller;

import com.petschool.entity.User;
import com.petschool.mapper.UserMapper;
import com.petschool.service.UserService;
import com.petschool.util.JwtUtil;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;
    private final UserMapper userMapper;

    public AuthController(UserService userService, UserMapper userMapper) {
        this.userService = userService;
        this.userMapper = userMapper;
    }

    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody User user) {
        Map<String, Object> result = new HashMap<>();
        if (user.getPhone() == null || user.getPassword() == null || user.getUsername() == null) {
            result.put("code", 400);
            result.put("message", "手机号、密码和用户名不能为空");
            return result;
        }
        User existing = userMapper.selectByPhone(user.getPhone());
        if (existing != null) {
            result.put("code", 400);
            result.put("message", "该手机号已注册");
            return result;
        }
        int rows = userService.register(user);
        if (rows > 0) {
            String token = JwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
            result.put("code", 200);
            result.put("message", "注册成功");
            result.put("data", buildUserInfo(user, token));
        } else {
            result.put("code", 500);
            result.put("message", "注册失败");
        }
        return result;
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> params) {
        Map<String, Object> result = new HashMap<>();
        String phone = params.get("phone");
        String password = params.get("password");
        if (phone == null || password == null) {
            result.put("code", 400);
            result.put("message", "手机号和密码不能为空");
            return result;
        }
        User user = userService.login(phone, password);
        if (user != null) {
            String token = JwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
            result.put("code", 200);
            result.put("message", "登录成功");
            result.put("data", buildUserInfo(user, token));
        } else {
            result.put("code", 401);
            result.put("message", "手机号或密码错误");
        }
        return result;
    }

    @GetMapping("/info")
    public Map<String, Object> info(@RequestAttribute("userId") Long userId) {
        Map<String, Object> result = new HashMap<>();
        User user = userService.getById(userId);
        if (user != null) {
            result.put("code", 200);
            result.put("data", buildUserInfo(user, null));
        } else {
            result.put("code", 404);
            result.put("message", "用户不存在");
        }
        return result;
    }

    private Map<String, Object> buildUserInfo(User user, String token) {
        Map<String, Object> info = new HashMap<>();
        info.put("id", user.getId());
        info.put("username", user.getUsername());
        info.put("phone", user.getPhone());
        info.put("role", user.getRole());
        info.put("roleName", user.getRole() == 0 ? "客户" : user.getRole() == 1 ? "训练师" : "管理员");
        if (token != null) {
            info.put("token", token);
        }
        return info;
    }
}
