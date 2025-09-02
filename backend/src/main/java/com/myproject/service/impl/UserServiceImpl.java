package com.myproject.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.myproject.dto.UserLoginDTO;
import com.myproject.dto.UserRegisterDTO;
import com.myproject.dto.ForgetPasswordDTO;
import com.myproject.vo.UserLoginVO;
import com.myproject.vo.UserInfoVO;
import com.myproject.entity.User;
import com.myproject.enums.UserStatus; // 引入用户状态枚举
import com.myproject.mapper.UserMapper;
import com.myproject.service.UserService;
import com.myproject.result.ResponseResult;
import com.myproject.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    @Transactional
    public ResponseResult<String> register(UserRegisterDTO registerDTO) {
        try {
            // 检查用户名是否已存在 (无论状态如何)
            if (findByUsername(registerDTO.getUsername()) != null) {
                return ResponseResult.fail("用户名已存在");
            }

            // 创建User实体，适配新的表结构
            User user = new User();
            user.setUsername(registerDTO.getUsername());
            user.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
            user.setEmail(registerDTO.getEmail());
            // 核心改动：不再设置role，而是设置默认状态为 ACTIVE
            user.setStatus(UserStatus.ACTIVE);
            user.setNickname(registerDTO.getUsername()); // 默认昵称为用户名

            userMapper.insert(user);

            return ResponseResult.success("注册成功");
        } catch (Exception e) {
            // 建议使用全局异常处理器，但此处保留原结构
            return ResponseResult.fail("注册失败：" + e.getMessage());
        }
    }

    @Override
    public ResponseResult<UserLoginVO> login(UserLoginDTO loginDTO) {
        try {
            User dbUser = findByUsername(loginDTO.getUsername());
            if (dbUser == null) {
                return ResponseResult.fail("用户名或密码错误");
            }

            if (!passwordEncoder.matches(loginDTO.getPassword(), dbUser.getPassword())) {
                return ResponseResult.fail("用户名或密码错误");
            }

            // 关键：检查用户状态
            if (dbUser.getStatus() != UserStatus.ACTIVE) {
                return ResponseResult.fail("账号状态异常，无法登录");
            }

            String token = jwtUtil.generateToken(dbUser.getId(), dbUser.getUsername());

            // 构建登录响应VO，保持旧结构
            UserLoginVO loginVO = UserLoginVO.builder()
                    .token(token)
                    .username(dbUser.getUsername())
                    // 核心适配：根据status模拟旧的role字段
                    .role("USER") // 假设所有活跃用户角色都是"USER"
                    .userId(dbUser.getId())
                    .build();

            return ResponseResult.success(loginVO);
        } catch (Exception e) {
            return ResponseResult.fail("登录失败：" + e.getMessage());
        }
    }

    @Override
    public ResponseResult<String> forgetPassword(ForgetPasswordDTO forgetPasswordDTO) {
        try {
            boolean success = resetPassword(forgetPasswordDTO.getUsername(), forgetPasswordDTO.getNewPassword());
            if (success) {
                return ResponseResult.success("密码重置成功");
            } else {
                // 更新错误信息，因为失败原因可能是状态问题
                return ResponseResult.fail("重置失败，用户不存在或状态异常");
            }
        } catch (Exception e) {
            return ResponseResult.fail("密码重置失败：" + e.getMessage());
        }
    }

    @Override
    public ResponseResult<UserInfoVO> getUserInfo(String username) {
        try {
            User user = findByUsername(username);
            if (user == null) {
                return ResponseResult.fail("用户不存在");
            }

            // 构建用户信息VO，保持旧结构
            UserInfoVO userInfoVO = UserInfoVO.builder()
                    .id(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    // 核心适配：根据status模拟旧的role字段
                    .role("USER") // 假设所有用户角色都是"USER"
                    // 核心适配：将新的createdAt字段值赋给旧的createTime字段
                    .createTime(user.getCreatedAt())
                    .build();

            return ResponseResult.success(userInfoVO);
        } catch (Exception e) {
            return ResponseResult.fail("获取用户信息失败：" + e.getMessage());
        }
    }

    @Override
    public User findByUsername(String username) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        return userMapper.selectOne(queryWrapper);
    }

    @Override
    @Transactional
    public boolean resetPassword(String username, String newPassword) {
        User user = findByUsername(username);
        // 关键：只允许活跃用户重置密码
        if (user == null || user.getStatus() != UserStatus.ACTIVE) {
            return false;
        }

        UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("username", username);

        User updateUser = new User();
        updateUser.setPassword(passwordEncoder.encode(newPassword));

        return userMapper.update(updateUser, updateWrapper) > 0;
    }
}

