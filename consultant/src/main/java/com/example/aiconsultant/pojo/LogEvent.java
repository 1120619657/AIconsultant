package com.example.aiconsultant.pojo;

import java.time.LocalDateTime;

public class LogEvent {
    private Long id;
    private LocalDateTime timestamp;
    private String level;
    private String logger;
    private String message;
    private String thread;
    private String className;
    private String methodName;
    private Integer lineNumber;
    private String requestUrl;
    private String requestMethod;
    private String ipAddress;
    private Long userId;
    private String exception;
    private String traceId;

    // 默认构造函数
    public LogEvent() {
    }

    // 全参构造函数
    public LogEvent(Long id, LocalDateTime timestamp, String level, String logger, String message, String thread, 
                   String className, String methodName, Integer lineNumber, String requestUrl, String requestMethod, 
                   String ipAddress, Long userId, String exception, String traceId) {
        this.id = id;
        this.timestamp = timestamp;
        this.level = level;
        this.logger = logger;
        this.message = message;
        this.thread = thread;
        this.className = className;
        this.methodName = methodName;
        this.lineNumber = lineNumber;
        this.requestUrl = requestUrl;
        this.requestMethod = requestMethod;
        this.ipAddress = ipAddress;
        this.userId = userId;
        this.exception = exception;
        this.traceId = traceId;
    }

    // getter方法
    public Long getId() {
        return id;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getLevel() {
        return level;
    }

    public String getLogger() {
        return logger;
    }

    public String getMessage() {
        return message;
    }

    public String getThread() {
        return thread;
    }

    public String getClassName() {
        return className;
    }

    public String getMethodName() {
        return methodName;
    }

    public Integer getLineNumber() {
        return lineNumber;
    }

    public String getRequestUrl() {
        return requestUrl;
    }

    public String getRequestMethod() {
        return requestMethod;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public Long getUserId() {
        return userId;
    }

    public String getException() {
        return exception;
    }

    public String getTraceId() {
        return traceId;
    }

    // setter方法
    public void setId(Long id) {
        this.id = id;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public void setLogger(String logger) {
        this.logger = logger;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setThread(String thread) {
        this.thread = thread;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public void setLineNumber(Integer lineNumber) {
        this.lineNumber = lineNumber;
    }

    public void setRequestUrl(String requestUrl) {
        this.requestUrl = requestUrl;
    }

    public void setRequestMethod(String requestMethod) {
        this.requestMethod = requestMethod;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setException(String exception) {
        this.exception = exception;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }
}