package com.example.aiconsultant.mapper;

import com.example.aiconsultant.pojo.LogEvent;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface LogMapper {

    /**
     * 插入日志记录
     * @param logEvent 日志事件
     */
    @Insert("INSERT INTO log_event (timestamp, level, logger, message, thread, class_name, method_name, line_number, request_url, request_method, ip_address, user_id, exception, trace_id) " +
            "VALUES (#{timestamp}, #{level}, #{logger}, #{message}, #{thread}, #{className}, #{methodName}, #{lineNumber}, #{requestUrl}, #{requestMethod}, #{ipAddress}, #{userId}, #{exception}, #{traceId})")
    void insertLog(LogEvent logEvent);
}