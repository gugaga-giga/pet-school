package com.petschool.filter;

import com.petschool.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

public class JwtFilter extends OncePerRequestFilter {

    private static final Set<String> WHITE_LIST = Set.of(
            "/auth/login",
            "/auth/register",
            "/"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        String path = request.getRequestURI();
        String contextPath = request.getContextPath();
        if (contextPath != null && !contextPath.isEmpty()) {
            path = path.substring(contextPath.length());
        }

        if (WHITE_LIST.contains(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        if (path.startsWith("/error") || path.startsWith("/webjars") || path.startsWith("/webrtc") || path.startsWith("/video") ||
                path.startsWith("/certificate/verify") || path.startsWith("/uploads/") ||
                path.endsWith(".html") || path.endsWith(".css") || path.endsWith(".js") ||
                path.endsWith(".ico") || path.endsWith(".png") || path.endsWith(".jpg")) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");

        // SSE连接特殊处理：支持通过query参数传递token
        if ((authHeader == null || !authHeader.startsWith("Bearer ")) && path.startsWith("/ai/")) {
            String queryToken = request.getParameter("token");
            if (queryToken != null && !queryToken.isEmpty()) {
                authHeader = "Bearer " + queryToken;
            }
        }

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"未登录或token已过期\"}");
            return;
        }

        try {
            String token = authHeader.substring(7);
            Long userId = JwtUtil.getUserId(token);
            Integer role = JwtUtil.getRole(token);
            String username = JwtUtil.getUsername(token);
            request.setAttribute("userId", userId);
            request.setAttribute("role", role);
            request.setAttribute("username", username);

            if (path.startsWith("/admin") && role < 2) {
                response.setStatus(403);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":403,\"message\":\"权限不足，需要管理员权限\"}");
                return;
            }

            String method = request.getMethod();
            if (("POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method) || "DELETE".equalsIgnoreCase(method)) && role < 2) {
                boolean requiresAdmin = false;
                if (path.startsWith("/medical/department") || path.startsWith("/medical/doctor") ||
                        path.startsWith("/medical/order/status") || path.startsWith("/medical/record") ||
                        path.startsWith("/medical/vaccine") || path.startsWith("/medical/deworming")) {
                    requiresAdmin = true;
                }
                if (path.startsWith("/health/create") || path.startsWith("/health/update") ||
                        path.startsWith("/health/delete") || path.equals("/health/rules")) {
                    requiresAdmin = true;
                }
                if (path.startsWith("/wallet/admin")) {
                    requiresAdmin = true;
                }
                if (requiresAdmin) {
                    response.setStatus(403);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"code\":403,\"message\":\"权限不足，需要管理员权限\"}");
                    return;
                }
            }

            filterChain.doFilter(request, response);
        } catch (Exception e) {
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"token无效或已过期\"}");
        }
    }
}
