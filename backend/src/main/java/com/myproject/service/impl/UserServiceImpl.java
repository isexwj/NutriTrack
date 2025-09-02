package com.myproject.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.myproject.dto.UserLoginDTO;
import com.myproject.dto.UserRegisterDTO;
import com.myproject.dto.ForgetPasswordDTO;
import com.myproject.vo.UserLoginVO;
import com.myproject.vo.UserInfoVO;
import com.myproject.entity.User;
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
            // 检查用户名是否已存在
            if (findByUsername(registerDTO.getUsername()) != null) {
                return ResponseResult.fail("用户名已存在");
            }
            
            // 创建User实体
            User user = new User();
            user.setUsername(registerDTO.getUsername());
            user.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
            user.setEmail(registerDTO.getEmail());
            user.setRole(registerDTO.getRole() != null ? registerDTO.getRole() : "USER");
            
            userMapper.insert(user);
            
            return ResponseResult.success("注册成功");
        } catch (Exception e) {
            return ResponseResult.fail("注册失败：" + e.getMessage());
        }
    }

    @Override
    public ResponseResult<UserLoginVO> login(UserLoginDTO loginDTO) {
        try {
            // 直接验证用户名和密码
            User dbUser = findByUsername(loginDTO.getUsername());
            if (dbUser == null) {
                return ResponseResult.fail("用户名或密码错误");
            }
            
            // 验证密码
            if (!passwordEncoder.matches(loginDTO.getPassword(), dbUser.getPassword())) {
                return ResponseResult.fail("用户名或密码错误");
            }
            
            // 生成JWT token
            String token = jwtUtil.generateToken(dbUser.getId(), dbUser.getUsername());
            
            // 构建登录响应VO
            UserLoginVO loginVO = UserLoginVO.builder()
                .token(token)
                .username(dbUser.getUsername())
                .role(dbUser.getRole())
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
                return ResponseResult.fail("重置失败，用户名不存在");
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
            
            // 构建用户信息VO（不包含敏感信息）
            UserInfoVO userInfoVO = UserInfoVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .createTime(user.getCreateTime())
                .build();
            
            return ResponseResult.success(userInfoVO);
        } catch (Exception e) {
            return ResponseResult.fail("获取用户信息失败：" + e.getMessage());
        }
    }

    @Override
    public User findByUsername(String username) {
        // 使用MyBatis-Plus的QueryWrapper
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        return userMapper.selectOne(queryWrapper);
    }

    @Override
    @Transactional
    public boolean resetPassword(String username, String newPassword) {
        User user = findByUsername(username);
        if (user == null) {
            return false;
        }
        
        // 使用MyBatis-Plus的UpdateWrapper
        UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("username", username);
        
        User updateUser = new User();
        updateUser.setPassword(passwordEncoder.encode(newPassword));
        
        return userMapper.update(updateUser, updateWrapper) > 0;
    }
}
