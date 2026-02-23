package com.example.aiconsultant.config;

import com.example.aiconsultant.mapper.ConversationMapper;
import com.example.aiconsultant.mapper.MessageMapper;
import com.example.aiconsultant.pojo.Conversation;
import com.example.aiconsultant.pojo.Message;
import com.example.aiconsultant.pojo.MessageEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Kafka消息消费者，处理聊天消息的异步保存
 */
@Component
public class KafkaMessageConsumer {
    private static final Logger log = LoggerFactory.getLogger(KafkaMessageConsumer.class);

    @Autowired
    private ConversationMapper conversationMapper;

    @Autowired
    private MessageMapper messageMapper;

    /**
     * 消费聊天消息并保存到数据库
     */
    @KafkaListener(topics = "chat-messages", groupId = "chat-message-group")
    public void consumeChatMessage(MessageEvent event) {
        try {
            log.info("Received message from Kafka: memoryId={}, role={}", event.getMemoryId(), event.getRole());
            
            // 根据memoryId查询会话
            Conversation conversation = conversationMapper.findByMemoryId(event.getMemoryId());
            
            // 如果会话不存在，自动创建新会话
            if (conversation == null) {
                if (event.getUserId() != null) {
                    conversation = new Conversation();
                    conversation.setUserId(event.getUserId());
                    conversation.setTitle("新会话");
                    conversation.setMemoryId(event.getMemoryId());
                    conversation.setCreateTime(event.getCreateTime());
                    conversation.setUpdateTime(event.getCreateTime());
                    conversation.setIsDeleted(0);
                    conversationMapper.insert(conversation);
                    log.info("Created new conversation for memoryId: {}", event.getMemoryId());
                } else {
                    log.error("Cannot create conversation: userId is null");
                    return;
                }
            }
            
            // 保存消息到数据库
            Message message = new Message();
            message.setConversationId(conversation.getId());
            message.setRole(event.getRole());
            message.setContent(event.getContent());
            message.setCreateTime(event.getCreateTime());
            message.setIsDeleted(0);
            messageMapper.insert(message);
            
            log.info("Message saved to database: conversationId={}, role={}", conversation.getId(), event.getRole());
        } catch (Exception e) {
            log.error("Failed to process message from Kafka: {}", e.getMessage(), e);
        }
    }
}