package com.example.aiconsultant.mapper;

import com.example.aiconsultant.pojo.Conversation;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 会话Mapper接口
 */
@Mapper
public interface ConversationMapper {
    /**
     * 根据用户ID查询会话列表
     * @param userId 用户ID
     * @return 会话列表
     */
    @Select("select * from conversation where user_id = #{userId} and is_deleted = 0 order by update_time desc")
    List<Conversation> findByUserId(Long userId);

    /**
     * 根据ID查询会话
     * @param id 会话ID
     * @return 会话信息
     */
    @Select("select * from conversation where id = #{id}")
    Conversation findById(Long id);

    /**
     * 根据记忆ID查询会话
     * @param memoryId 记忆ID
     * @return 会话信息
     */
    @Select("select * from conversation where memory_id = #{memoryId}")
    Conversation findByMemoryId(String memoryId);

    /**
     * 插入会话
     * @param conversation 会话信息
     */
    @Insert("insert into conversation(user_id, title, memory_id, create_time, update_time, is_deleted) values(#{userId}, #{title}, #{memoryId}, #{createTime}, #{updateTime}, #{isDeleted})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Conversation conversation);

    /**
     * 更新会话
     * @param conversation 会话信息
     */
    @Update("update conversation set title = #{title}, update_time = #{updateTime} where id = #{id}")
    void update(Conversation conversation);

    /**
     * 删除会话（软删除）
     * @param id 会话ID
     */
    @Update("update conversation set is_deleted = 1, update_time = now() where id = #{id}")
    void delete(Long id);
}