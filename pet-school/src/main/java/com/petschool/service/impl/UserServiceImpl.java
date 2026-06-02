package com.petschool.service.impl;

import com.petschool.entity.User;
import com.petschool.mapper.UserMapper;
import com.petschool.service.UserService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;

    public UserServiceImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public int register(User user) {
        user.setRole(0);
        return userMapper.insert(user);
    }

    @Override
    public User getById(Long id) {
        return userMapper.selectById(id);
    }

    @Override
    public List<User> listAll() {
        return userMapper.selectAll();
    }

    @Override
    public User login(String phone, String password) {
        User user = userMapper.selectByPhone(phone);
        if (user != null && user.getPassword().equals(password)) {
            return user;
        }
        return null;
    }
}
