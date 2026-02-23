package com.example.aiconsultant.controller;

import com.example.aiconsultant.aiservice.ConsultantService;
import com.example.aiconsultant.mapper.ConversationMapper;
import com.example.aiconsultant.mapper.MessageMapper;
import com.example.aiconsultant.pojo.Conversation;
import com.example.aiconsultant.pojo.Message;
import com.example.aiconsultant.pojo.MessageEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
@RestController
public class ChatController {
    private static final Logger log = LoggerFactory.getLogger(ChatController.class);
    
    @Autowired
    private ConsultantService consultantService;

    @Autowired
    private ConversationMapper conversationMapper;

    @Autowired
    private MessageMapper messageMapper;
    
    @Autowired
    private KafkaTemplate<String, MessageEvent> kafkaTemplate;

    @RequestMapping(value = "/chat",produces = "text/html;charset=utf-8")
    public Flux<String> chat(HttpServletRequest request, @RequestParam String memoryId, @RequestParam String message){
        // 异步保存用户消息到Kafka
        saveMessageToKafka(request, memoryId, message, "user");
        
        Flux<String> result = consultantService.chat(memoryId, message);
        return result;
    }
    
    /**
     * 保存AI模型的响应到数据库
     */
    @RequestMapping(value = "/chat/save-ai-response",produces = "application/json")
    public String saveAiResponse(HttpServletRequest request, @RequestParam String memoryId, @RequestParam String content){
        // 异步保存AI响应到Kafka
        saveMessageToKafka(request, memoryId, content, "assistant");
        return "{\"success\": true}";
    }

    /**
     * 保存消息到Kafka
     */
    private void saveMessageToKafka(HttpServletRequest request, String memoryId, String content, String role) {
        try {
            // 获取用户ID
            Long userId = (Long) request.getAttribute("userId");
            
            // 创建消息事件
            MessageEvent event = new MessageEvent();
            event.setMemoryId(memoryId);
            event.setContent(content);
            event.setRole(role);
            event.setUserId(userId);
            event.setCreateTime(LocalDateTime.now());
            
            // 发送到Kafka
            kafkaTemplate.send("chat-messages", event);
            log.info("Message sent to Kafka: memoryId={}, role={}", memoryId, role);
        } catch (Exception e) {
            log.error("Failed to send message to Kafka: {}", e.getMessage(), e);
            // 降级处理：直接保存到数据库
            saveMessageToDatabase(request, memoryId, content, role);
        }
    }
    
    /**
     * 保存消息到数据库（降级处理）
     */
    private void saveMessageToDatabase(HttpServletRequest request, String memoryId, String content, String role) {
        try {
            // 根据memoryId查询会话
            Conversation conversation = conversationMapper.findByMemoryId(memoryId);
            
            // 如果会话不存在，自动创建新会话（隐式创建）
            if (conversation == null) {
                Long userId = (Long) request.getAttribute("userId");
                if (userId != null) {
                    conversation = new Conversation();
                    conversation.setUserId(userId);
                    conversation.setTitle("新会话");
                    conversation.setMemoryId(memoryId);
                    conversation.setCreateTime(LocalDateTime.now());
                    conversation.setUpdateTime(LocalDateTime.now());
                    conversation.setIsDeleted(0);
                    conversationMapper.insert(conversation);
                }
            }
            
            // 保存消息
            if (conversation != null) {
                Message message = new Message();
                message.setConversationId(conversation.getId());
                message.setRole(role);
                message.setContent(content);
                message.setCreateTime(LocalDateTime.now());
                message.setIsDeleted(0);
                messageMapper.insert(message);
            }
        } catch (Exception e) {
            log.error("Failed to save message to database: {}", e.getMessage(), e);
        }
    }


}
