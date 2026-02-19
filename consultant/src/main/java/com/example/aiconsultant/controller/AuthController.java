package com.example.aiconsultant.controller;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.example.aiconsultant.mapper.UserMapper;
import com.example.aiconsultant.pojo.User;
import com.example.aiconsultant.util.JwtUtil;
import com.wf.captcha.SpecCaptcha;
import com.wf.captcha.base.Captcha;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 认证控制器，处理注册登录请求
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    
    @Autowired
    private OSS ossClient;
    
    @Value("${aliyun.oss.bucket-name}")
    private String bucketName;
    
    @Value("${aliyun.oss.domain}")
    private String ossDomain;

    /**
     * 生成验证码
     */
    @GetMapping("/captcha")
    public void generateCaptcha(HttpServletResponse response, @RequestParam String uuid) throws IOException {
        // 设置响应头
        response.setContentType("image/png");
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);

        // 生成验证码
        SpecCaptcha captcha = new SpecCaptcha(130, 48);
        captcha.setLen(4); // 验证码长度
        captcha.setCharType(Captcha.TYPE_ONLY_NUMBER); // 只生成数字

        // 保存验证码到Redis，有效期5分钟
        redisTemplate.opsForValue().set("captcha:" + uuid, captcha.text().toLowerCase(), 5, TimeUnit.MINUTES);

        // 输出验证码
        ServletOutputStream outputStream = response.getOutputStream();
        captcha.out(outputStream);
        outputStream.flush();
        outputStream.close();
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody Map<String, String> params) {
        Map<String, Object> result = new HashMap<>();
        String username = params.get("username");
        String password = params.get("password");
        String email = params.get("email");
        String phone = params.get("phone");
        String uuid = params.get("uuid");
        String captcha = params.get("captcha");

        // 验证验证码
        String redisCaptcha = redisTemplate.opsForValue().get("captcha:" + uuid);
        if (redisCaptcha == null || !redisCaptcha.equals(captcha.toLowerCase())) {
            result.put("success", false);
            result.put("message", "验证码错误");
            return result;
        }

        // 检查用户名是否已存在
        if (userMapper.findByUsername(username) != null) {
            result.put("success", false);
            result.put("message", "用户名已存在");
            return result;
        }

        // 检查邮箱是否已存在
        if (userMapper.findByEmail(email) != null) {
            result.put("success", false);
            result.put("message", "邮箱已存在");
            return result;
        }

        // 检查手机号是否已存在
        if (userMapper.findByPhone(phone) != null) {
            result.put("success", false);
            result.put("message", "手机号已存在");
            return result;
        }

        // 创建用户
        User user = new User();
        user.setUsername(username);
        user.setPassword(DigestUtils.md5DigestAsHex(password.getBytes())); // MD5加密密码
        user.setEmail(email);
        user.setPhone(phone);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        user.setStatus(1); // 启用状态

        userMapper.insert(user);

        result.put("success", true);
        result.put("message", "注册成功");
        return result;
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> params) {
        Map<String, Object> result = new HashMap<>();
        String username = params.get("username");
        String password = params.get("password");
        String uuid = params.get("uuid");
        String captcha = params.get("captcha");

        // 验证验证码
        String redisCaptcha = redisTemplate.opsForValue().get("captcha:" + uuid);
        if (redisCaptcha == null || !redisCaptcha.equals(captcha.toLowerCase())) {
            result.put("success", false);
            result.put("message", "验证码错误");
            return result;
        }

        // 验证用户
        User user = userMapper.findByUsername(username);
        if (user == null) {
            result.put("success", false);
            result.put("message", "用户名不存在");
            return result;
        }

        // 验证密码
        if (!DigestUtils.md5DigestAsHex(password.getBytes()).equals(user.getPassword())) {
            result.put("success", false);
            result.put("message", "密码错误");
            return result;
        }

        // 验证用户状态
        if (user.getStatus() == 0) {
            result.put("success", false);
            result.put("message", "账户已禁用");
            return result;
        }

        // 生成JWT token
        String token = JwtUtil.generateToken(user.getId());

        result.put("success", true);
        result.put("message", "登录成功");
        result.put("token", token);
        result.put("user", user);
        return result;
    }

    /**
     * 退出登录
     */
    @PostMapping("/logout")
    public Map<String, Object> logout() {
        Map<String, Object> result = new HashMap<>();
        // JWT是无状态的，客户端删除token即可
        result.put("success", true);
        result.put("message", "退出成功");
        return result;
    }

    /**
     * 验证token
     */
    @GetMapping("/verify")
    public Map<String, Object> verifyToken(@RequestHeader String token) {
        Map<String, Object> result = new HashMap<>();
        // 处理token格式，去掉Bearer前缀（如果有的话）
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        
        if (token == null || !JwtUtil.verifyToken(token)) {
            result.put("success", false);
            result.put("message", "token无效");
        } else {
            result.put("success", true);
            result.put("message", "token有效");
        }
        return result;
    }
    
    /**
     * 上传头像
     */
    @PostMapping("/upload-avatar")
    public Map<String, Object> uploadAvatar(@RequestParam("file") MultipartFile file, @RequestHeader String Authorization) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 处理token，获取用户ID
            String token = Authorization;
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            
            if (token == null || !JwtUtil.verifyToken(token)) {
                result.put("success", false);
                result.put("message", "token无效");
                return result;
            }
            
            Long userId = JwtUtil.getUserIdFromToken(token);
            if (userId == null) {
                result.put("success", false);
                result.put("message", "无效的用户信息");
                return result;
            }
            
            // 验证文件
            if (file.isEmpty()) {
                result.put("success", false);
                result.put("message", "请选择要上传的头像文件");
                return result;
            }
            
            // 验证文件类型
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                result.put("success", false);
                result.put("message", "请上传图片类型的文件");
                return result;
            }
            
            // 验证文件大小（限制10MB）
            long fileSize = file.getSize();
            if (fileSize > 10 * 1024 * 1024) {
                result.put("success", false);
                result.put("message", "头像文件大小不能超过10MB");
                return result;
            }
            
            // 生成文件名
            String originalFilename = file.getOriginalFilename();
            String fileExtension = StringUtils.getFilenameExtension(originalFilename);
            String filename = "avatars/" + userId + "/" + System.currentTimeMillis() + "." + fileExtension;
            
            // 上传文件到OSS
            ossClient.putObject(bucketName, filename, file.getInputStream());
            
            // 生成文件URL
            String fileUrl = ossDomain + "/" + filename;
            
            // 更新用户头像
            User user = userMapper.selectById(userId);
            if (user == null) {
                result.put("success", false);
                result.put("message", "用户不存在");
                return result;
            }
            
            user.setAvatar(fileUrl);
            LocalDateTime updateTime = LocalDateTime.now();
            user.setUpdateTime(updateTime);
            userMapper.updateAvatar(userId, fileUrl, updateTime);
            
            result.put("success", true);
            result.put("message", "头像上传成功");
            result.put("avatar", fileUrl);
            result.put("user", user);
            
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "头像上传失败：" + e.getMessage());
        }
        
        return result;
    }
}