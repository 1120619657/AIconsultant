package com.example.aiconsultant.service;

import com.example.aiconsultant.pojo.LogEvent;

public interface LogService {
    /**
     * 保存日志到数据库
     * @param logEvent 日志事件
     */
    void saveLog(LogEvent logEvent);
}