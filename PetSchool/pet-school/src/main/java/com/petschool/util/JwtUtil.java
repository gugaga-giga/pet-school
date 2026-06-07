package com.petschool.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

public class JwtUtil {

    private static final String SECRET = "PetSchoolSecretKey2026ForJwtTokenGeneration!!";
    private static final SecretKey KEY = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    private static final long EXPIRE = 86400000L;

    public static String generateToken(Long userId, String username, Integer role) {
        return Jwts.builder()
                .subject(userId.toString())
                .claim("username", username)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRE))
                .signWith(KEY)
                .compact();
    }

    public static Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(KEY)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public static Long getUserId(String token) {
        return Long.parseLong(parseToken(token).getSubject());
    }

    public static Integer getRole(String token) {
        return parseToken(token).get("role", Integer.class);
    }

    public static String getUsername(String token) {
        return parseToken(token).get("username", String.class);
    }
}
