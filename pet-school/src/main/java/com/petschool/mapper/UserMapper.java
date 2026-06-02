package com.petschool.mapper;

import com.petschool.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserMapper {
    int insert(User user);
    User selectById(@Param("id") Long id);
    List<User> selectAll();
    User selectByPhone(@Param("phone") String phone);
}
