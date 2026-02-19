package com.example.aiconsultant.service.impl;

import com.example.aiconsultant.pojo.LogEvent;
import com.example.aiconsultant.service.LogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.aiconsultant.mapper.LogMapper;

@Service
public class LogServiceImpl implements LogService {

    @Autowired
    private LogMapper logMapper;

    @Override
    public void saveLog(LogEvent logEvent) {
        logMapper.insertLog(logEvent);
    }
}