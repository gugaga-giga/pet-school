package com.petschool.service;

import com.petschool.entity.User;
import java.util.List;

public interface UserService {
    int register(User user);
    User getById(Long id);
    List<User> listAll();
    User login(String phone, String password);
}
