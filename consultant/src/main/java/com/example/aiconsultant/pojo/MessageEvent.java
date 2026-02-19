package com.example.aiconsultant.pojo;

import java.time.LocalDateTime;

/**
 * Kafka消息事件类，用于异步传输聊天消息
 */
public class MessageEvent {
    private Long conversationId;
    private String role;
    private String content;
    private LocalDateTime createTime;
    private String memoryId;
    private Long userId;

    // 构造方法
    public MessageEvent() {
        this.createTime = LocalDateTime.now();
    }

    // Getter和Setter方法
    public Long getConversationId() {
        return conversationId;
    }

    public void setConversationId(Long conversationId) {
        this.conversationId = conversationId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public String getMemoryId() {
        return memoryId;
    }

    public void setMemoryId(String memoryId) {
        this.memoryId = memoryId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}