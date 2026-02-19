package com.example.aiconsultant.mapper;

import com.example.aiconsultant.pojo.Message;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 消息Mapper接口
 */
@Mapper
public interface MessageMapper {
    /**
     * 根据会话ID查询消息列表
     * @param conversationId 会话ID
     * @return 消息列表
     */
    @Select("select * from message where conversation_id = #{conversationId} and is_deleted = 0 order by create_time asc")
    List<Message> findByConversationId(Long conversationId);

    /**
     * 插入消息
     * @param message 消息信息
     */
    @Insert("insert into message(conversation_id, role, content, create_time, is_deleted) values(#{conversationId}, #{role}, #{content}, #{createTime}, #{isDeleted})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Message message);

    /**
     * 根据会话ID删除消息（软删除）
     * @param conversationId 会话ID
     */
    @Update("update message set is_deleted = 1 where conversation_id = #{conversationId}")
    void deleteByConversationId(Long conversationId);

    /**
     * 查询会话的最新消息
     * @param conversationId 会话ID
     * @return 最新消息
     */
    @Select("select * from message where conversation_id = #{conversationId} and is_deleted = 0 order by create_time desc limit 1")
    Message findLatestMessageByConversationId(Long conversationId);
}