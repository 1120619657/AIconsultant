package com.example.aiconsultant.mapper;

import com.example.aiconsultant.pojo.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;

/**
 * 用户Mapper接口
 */
@Mapper
public interface UserMapper {

    /**
     * 根据用户名查询用户
     * @param username 用户名
     * @return 用户信息
     */
    @Select("select * from user where username = #{username}")
    User findByUsername(String username);

    /**
     * 根据邮箱查询用户
     * @param email 邮箱
     * @return 用户信息
     */
    @Select("select * from user where email = #{email}")
    User findByEmail(String email);

    /**
     * 根据手机号查询用户
     * @param phone 手机号
     * @return 用户信息
     */
    @Select("select * from user where phone = #{phone}")
    User findByPhone(String phone);
    
    /**
     * 根据ID查询用户
     * @param id 用户ID
     * @return 用户信息
     */
    @Select("select * from user where id = #{id}")
    User selectById(Long id);

    /**
     * 插入用户
     * @param user 用户信息
     */
    @Insert("insert into user(username, password, email, phone, create_time, update_time, status) values(#{username}, #{password}, #{email}, #{phone}, #{createTime}, #{updateTime}, #{status})")
    void insert(User user);

    /**
     * 更新用户基本信息
     * @param user 用户信息
     */
    @Update("update user set password = #{password}, email = #{email}, phone = #{phone}, update_time = #{updateTime}, status = #{status} where id = #{id}")
    void update(User user);
    
    /**
     * 更新用户头像
     * @param id 用户ID
     * @param avatar 头像URL
     * @param updateTime 更新时间
     */
    @Update("update user set avatar = #{avatar}, update_time = #{updateTime} where id = #{id}")
    void updateAvatar(@Param("id") Long id, @Param("avatar") String avatar, @Param("updateTime") LocalDateTime updateTime);
}