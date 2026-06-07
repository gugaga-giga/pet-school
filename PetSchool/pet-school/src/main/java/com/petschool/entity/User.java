package com.petschool.entity;

import java.io.Serializable;

public class User implements Serializable {
    private Long id;
    private String username;
    private String password;
    private String phone;
    private Integer role;
    private String avatar;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public Integer getRole() { return role; }
    public void setRole(Integer role) { this.role = role; }
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
}
