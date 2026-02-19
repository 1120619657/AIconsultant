package com.example.aiconsultant.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;

import java.util.Date;

public class JwtUtil {

    // 密钥，用于签名JWT
    private static final String SECRET = "your-secret-key"; // 实际项目中应使用更安全的密钥

    // 过期时间，7天
    private static final long EXPIRE_TIME = 7 * 24 * 60 * 60 * 1000;

    /**
     * 生成JWT token
     * @param userId 用户ID
     * @return token字符串
     */
    public static String generateToken(Long userId) {
        // 设置过期时间
        Date expireDate = new Date(System.currentTimeMillis() + EXPIRE_TIME);

        // 创建JWT token
        return JWT.create()
                .withClaim("userId", userId) // 存储用户ID
                .withExpiresAt(expireDate) // 设置过期时间
                .withIssuedAt(new Date()) // 设置签发时间
                .sign(Algorithm.HMAC256(SECRET)); // 使用HMAC256算法签名
    }

    /**
     * 验证JWT token
     * @param token token字符串
     * @return 是否有效
     */
    public static boolean verifyToken(String token) {
        try {
            JWT.require(Algorithm.HMAC256(SECRET))
                    .build()
                    .verify(token);
            return true;
        } catch (JWTVerificationException e) {
            return false;
        }
    }

    /**
     * 从JWT token中获取用户ID
     * @param token token字符串
     * @return 用户ID
     */
    public static Long getUserIdFromToken(String token) {
        try {
            DecodedJWT jwt = JWT.decode(token);
            return jwt.getClaim("userId").asLong();
        } catch (JWTDecodeException e) {
            return null;
        }
    }
}