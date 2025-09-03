/**
 * 文件路径: src/main/java/com/myproject/mapper/UserMapper.java
 * 说明: users 表的 Mapper，提供按 username 查询用户的方法（服务层用于解析 username -> userId）。
 */

package com.myproject.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.myproject.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    @Select("SELECT * FROM users WHERE username = #{username}")
    User selectByUsername(@Param("username") String username);
}

