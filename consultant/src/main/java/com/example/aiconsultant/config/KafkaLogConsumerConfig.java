package com.example.aiconsultant.config;

import com.example.aiconsultant.pojo.LogEvent;
import com.example.aiconsultant.service.LogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class KafkaLogConsumerConfig {

    @Autowired
    private LogService logService;

    /**
     * Kafka消费者，接收日志消息并存储到MySQL
     * @param logEvent 日志事件
     */
    @KafkaListener(topics = "travel-plan-logs", groupId = "travel-plan-log-group")
    public void consumeLog(LogEvent logEvent) {
        try {
            // 保存日志到MySQL数据库
            logService.saveLog(logEvent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}