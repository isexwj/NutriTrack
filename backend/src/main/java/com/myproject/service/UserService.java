package com.myproject.service;

import com.myproject.dto.UserLoginDTO;
import com.myproject.dto.UserRegisterDTO;
import com.myproject.dto.ForgetPasswordDTO;
import com.myproject.vo.UserLoginVO;
import com.myproject.vo.UserInfoVO;
import com.myproject.result.ResponseResult;
import com.myproject.vo.CaptchaVO;
import com.myproject.dto.RegisterStep1DTO;
import com.myproject.dto.RegisterEmailSendDTO;
import com.myproject.dto.RegisterStep2DTO;
import com.myproject.dto.ForgetPasswordSendDTO;
import com.myproject.dto.ForgetPasswordResetDTO;

public interface UserService {
    
    /**
     * 用户注册
     */
    ResponseResult<String> register(UserRegisterDTO registerDTO);
    
    /**
     * 用户登录
     */
    ResponseResult<UserLoginVO> login(UserLoginDTO loginDTO);
    
    /**
     * 忘记密码
     */
    ResponseResult<String> forgetPassword(ForgetPasswordDTO forgetPasswordDTO);
    
    /**
     * 根据用户名获取用户信息
     */
    ResponseResult<UserInfoVO> getUserInfo(String username);
    
    /**
     * 根据用户名查找用户（内部使用）
     */
    com.myproject.entity.User findByUsername(String username);
    
    /**
     * 重置密码（内部使用）
     */
    boolean resetPassword(String username, String newPassword);

    ResponseResult<String> updateUserInfo(String username, UserInfoVO userInfoVO);

    ResponseResult<String> deactivateAccount(String username);

    // 注册两步与忘记密码（新增）
    ResponseResult<String> registerStep1(RegisterStep1DTO dto);

    ResponseResult<String> registerEmailSend(RegisterEmailSendDTO dto);

    ResponseResult<String> registerStep2(RegisterStep2DTO dto);

    ResponseResult<String> forgetPasswordSend(ForgetPasswordSendDTO dto);

    ResponseResult<String> forgetPasswordReset(ForgetPasswordResetDTO dto);

    // 图形验证码
    ResponseResult<CaptchaVO> generateCaptcha(String scene);
}