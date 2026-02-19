package com.example.aiconsultant.controller;

import com.example.aiconsultant.mapper.ConversationMapper;
import com.example.aiconsultant.mapper.MessageMapper;
import com.example.aiconsultant.pojo.Conversation;
import com.example.aiconsultant.pojo.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 会话控制器，处理会话相关请求
 */
@RestController
@RequestMapping("/conversation")
public class ConversationController {

    @Autowired
    private ConversationMapper conversationMapper;

    @Autowired
    private MessageMapper messageMapper;

    /**
     * 获取用户会话列表
     */
    @GetMapping("/list")
    public Map<String, Object> getConversationList(HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 从request中获取用户ID
            Long userId = (Long) request.getAttribute("userId");

            // 查询会话列表
            List<Conversation> conversationList = conversationMapper.findByUserId(userId);
            result.put("success", true);
            result.put("data", conversationList);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取会话列表失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 创建新会话
     */
    @PostMapping("/create")
    public Map<String, Object> createConversation(HttpServletRequest request, @RequestBody Map<String, String> params) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 从request中获取用户ID
            Long userId = (Long) request.getAttribute("userId");

            // 创建会话
            Conversation conversation = new Conversation();
            conversation.setUserId(userId);
            conversation.setTitle(params.getOrDefault("title", "新会话"));
            conversation.setMemoryId(UUID.randomUUID().toString());
            conversation.setCreateTime(LocalDateTime.now());
            conversation.setUpdateTime(LocalDateTime.now());
            conversation.setIsDeleted(0);

            conversationMapper.insert(conversation);
            result.put("success", true);
            result.put("data", conversation);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "创建会话失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 获取会话详情
     */
    @GetMapping("/detail/{id}")
    public Map<String, Object> getConversationDetail(HttpServletRequest request, @PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 从request中获取用户ID
            Long userId = (Long) request.getAttribute("userId");

            // 查询会话
            Conversation conversation = conversationMapper.findById(id);
            if (conversation == null || !conversation.getUserId().equals(userId)) {
                result.put("success", false);
                result.put("message", "会话不存在或无权限访问");
                return result;
            }

            // 查询会话消息
            List<Message> messages = messageMapper.findByConversationId(id);

            Map<String, Object> data = new HashMap<>();
            data.put("conversation", conversation);
            data.put("messages", messages);

            result.put("success", true);
            result.put("data", data);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取会话详情失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 更新会话标题
     */
    @PostMapping("/update")
    public Map<String, Object> updateConversation(HttpServletRequest request, @RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 从request中获取用户ID
            Long userId = (Long) request.getAttribute("userId");

            Long id = Long.parseLong(params.get("id").toString());
            String title = params.get("title").toString();

            // 查询会话
            Conversation conversation = conversationMapper.findById(id);
            if (conversation == null || !conversation.getUserId().equals(userId)) {
                result.put("success", false);
                result.put("message", "会话不存在或无权限访问");
                return result;
            }

            // 更新会话
            conversation.setTitle(title);
            conversation.setUpdateTime(LocalDateTime.now());
            conversationMapper.update(conversation);

            result.put("success", true);
            result.put("data", conversation);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "更新会话失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 删除会话
     */
    @PostMapping("/delete/{id}")
    public Map<String, Object> deleteConversation(HttpServletRequest request, @PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 从request中获取用户ID
            Long userId = (Long) request.getAttribute("userId");

            // 查询会话
            Conversation conversation = conversationMapper.findById(id);
            if (conversation == null || !conversation.getUserId().equals(userId)) {
                result.put("success", false);
                result.put("message", "会话不存在或无权限访问");
                return result;
            }

            // 删除会话（软删除）
            conversationMapper.delete(id);
            // 删除会话消息（软删除）
            messageMapper.deleteByConversationId(id);

            result.put("success", true);
            result.put("message", "删除会话成功");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "删除会话失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 获取会话消息列表
     */
    @GetMapping("/messages/{conversationId}")
    public Map<String, Object> getMessages(HttpServletRequest request, @PathVariable Long conversationId) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 从request中获取用户ID
            Long userId = (Long) request.getAttribute("userId");

            // 查询会话，验证权限
            Conversation conversation = conversationMapper.findById(conversationId);
            if (conversation == null || !conversation.getUserId().equals(userId)) {
                result.put("success", false);
                result.put("message", "会话不存在或无权限访问");
                return result;
            }

            // 查询消息列表
            List<Message> messages = messageMapper.findByConversationId(conversationId);
            result.put("success", true);
            result.put("data", messages);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取消息列表失败: " + e.getMessage());
        }
        return result;
    }
}