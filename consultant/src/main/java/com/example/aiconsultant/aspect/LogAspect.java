package com.example.aiconsultant.aspect;

import com.example.aiconsultant.pojo.LogEvent;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Arrays;

@Aspect
@Component
public class LogAspect {
    
    private static final Logger log = LoggerFactory.getLogger(LogAspect.class);

    @Autowired
    private KafkaTemplate<String, LogEvent> kafkaTemplate;

    // 定义切入点，拦截所有controller和service层的方法
    @Pointcut("execution(* com.example.aiconsultant.controller..*.*(..)) || execution(* com.example.aiconsultant.aiservice..*.*(..))")
    public void logPointcut() {}

    // 环绕通知
    @Around("logPointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        Object result = null;
        String exceptionMessage = null;
        long startTime = System.currentTimeMillis();

        try {
            result = joinPoint.proceed();
        } catch (Exception e) {
            exceptionMessage = Arrays.toString(e.getStackTrace());
            throw e;
        } finally {
            long endTime = System.currentTimeMillis();
            // 发送日志到Kafka
            sendLogToKafka(joinPoint, startTime, endTime, exceptionMessage);
        }

        return result;
    }

    // 发送日志到Kafka
    private void sendLogToKafka(ProceedingJoinPoint joinPoint, long startTime, long endTime, String exceptionMessage) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        
        LogEvent logEvent = new LogEvent();
        logEvent.setTimestamp(LocalDateTime.now());
        logEvent.setLevel(exceptionMessage != null ? "ERROR" : "INFO");
        logEvent.setLogger(joinPoint.getTarget().getClass().getName());
        logEvent.setMessage("Method execution time: " + (endTime - startTime) + "ms");
        logEvent.setThread(Thread.currentThread().getName());
        logEvent.setClassName(joinPoint.getTarget().getClass().getName());
        logEvent.setMethodName(joinPoint.getSignature().getName());
        logEvent.setLineNumber(0); // 无法直接获取行号
        logEvent.setRequestUrl(request.getRequestURL().toString());
        logEvent.setRequestMethod(request.getMethod());
        logEvent.setIpAddress(request.getRemoteAddr());
        
        // 从request中获取用户ID（由AuthInterceptor设置）
        Object userIdObj = request.getAttribute("userId");
        if (userIdObj != null) {
            try {
                logEvent.setUserId((Long) userIdObj);
            } catch (ClassCastException e) {
                log.error("Failed to cast userId to Long: {}", e.getMessage());
            }
        }
        
        logEvent.setException(exceptionMessage);
        logEvent.setTraceId(request.getHeader("trace-id")); // 可从请求头获取

        // 异步发送到Kafka
        try {
            kafkaTemplate.send("travel-plan-logs", logEvent);
            log.info("Log sent to Kafka: {}", logEvent);
        } catch (Exception e) {
            // Kafka发送失败时，记录到本地日志
            log.error("Failed to send log to Kafka: {}", e.getMessage(), e);
        }
    }
}