package com.petschool.ai.context;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 用户上下文，从JWT认证信息中构建
 */
public class UserContext {

    private Long userId;
    private String username;
    private Integer role; // 0客户 1训练师 2管理员
    private String roleName;
    private String phone;

    public UserContext() {}

    public UserContext(Long userId, String username, Integer role) {
        this.userId = userId;
        this.username = username;
        this.role = role;
        this.roleName = getRoleNameByCode(role);
    }

    /**
     * 从request属性构建用户上下文（JWT Filter已将userId/username/role放入request属性）
     */
    public static UserContext fromRequest(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String username = (String) request.getAttribute("username");
        Integer role = (Integer) request.getAttribute("role");
        return new UserContext(userId, username, role);
    }

    private static String getRoleNameByCode(Integer role) {
        if (role == null) return "未知";
        return switch (role) {
            case 0 -> "客户";
            case 1 -> "训练师";
            case 2 -> "管理员";
            default -> "未知";
        };
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public Integer getRole() { return role; }
    public void setRole(Integer role) {
        this.role = role;
        this.roleName = getRoleNameByCode(role);
    }
    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
}
