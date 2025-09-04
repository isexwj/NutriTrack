package com.myproject.controller;

import com.myproject.dto.UserLoginDTO;
import com.myproject.dto.UserRegisterDTO;
import com.myproject.dto.ForgetPasswordDTO;
import com.myproject.vo.UserLoginVO;
import com.myproject.vo.UserInfoVO;
import com.myproject.service.UserService;
import com.myproject.result.ResponseResult;
import com.myproject.dto.RegisterStep1DTO;
import com.myproject.dto.RegisterEmailSendDTO;
import com.myproject.dto.RegisterStep2DTO;
import com.myproject.dto.ForgetPasswordSendDTO;
import com.myproject.dto.ForgetPasswordResetDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    @Autowired(required = false)
    private StringRedisTemplate stringRedisTemplate;

//    public UserController(UserService userService) {
//        this.userService = userService;
//    }

    @PostMapping("/register")
    public ResponseResult<String> register(@RequestBody UserRegisterDTO registerDTO) {
        return userService.register(registerDTO);
    }

    // 新增：注册步骤1（缓存注册数据，不立即入库）
    @PostMapping("/register/step1")
    public ResponseResult<String> registerStep1(@RequestBody RegisterStep1DTO dto) {
        return userService.registerStep1(dto);
    }

    // 新增：注册-发送邮箱验证码
    @PostMapping("/register/email/send")
    public ResponseResult<String> registerEmailSend(@RequestBody RegisterEmailSendDTO dto) {
        return userService.registerEmailSend(dto);
    }

    // 新增：注册步骤2（校验邮箱验证码并入库）
    @PostMapping("/register/step2")
    public ResponseResult<String> registerStep2(@RequestBody RegisterStep2DTO dto) {
        return userService.registerStep2(dto);
    }

    @PostMapping("/login")
    public ResponseResult<UserLoginVO> login(@RequestBody UserLoginDTO loginDTO) {
        return userService.login(loginDTO);
    }

    // 获取登录/注册图形验证码（简单Base64，验证码存入Redis，TTL=3分钟）
    @GetMapping("/captcha")
    public ResponseResult<com.myproject.vo.CaptchaVO> getCaptcha(@RequestParam(defaultValue = "login") String scene) {
        return userService.generateCaptcha(scene);
    }

    @PostMapping("/forget-password")
    public ResponseResult<String> forgetPassword(@RequestBody ForgetPasswordDTO forgetPasswordDTO) {
        return userService.forgetPassword(forgetPasswordDTO);
    }

    // 新增：忘记密码-发送邮箱验证码
    @PostMapping("/forget-password/send")
    public ResponseResult<String> forgetPasswordSend(@RequestBody ForgetPasswordSendDTO dto) {
        return userService.forgetPasswordSend(dto);
    }

    // 新增：忘记密码-校验验证码并重置
    @PostMapping("/forget-password/reset")
    public ResponseResult<String> forgetPasswordReset(@RequestBody ForgetPasswordResetDTO dto) {
        return userService.forgetPasswordReset(dto);
    }

    @GetMapping("/{username}")
    public ResponseResult<UserInfoVO> getUserInfo(@PathVariable String username) {
        return userService.getUserInfo(username);
    }

    // controller/UserController.java

    @PutMapping("/{username}")
    public ResponseResult<String> updateUserInfo(@PathVariable String username, @RequestBody UserInfoVO userInfoVO) {
        // 验证用户权限（只能修改自己的信息）
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!username.equals(authentication.getName())) {
            return ResponseResult.fail("无权限修改他人信息");
        }

        return userService.updateUserInfo(username, userInfoVO);
    }

    @DeleteMapping("/{username}")
    public ResponseResult<String> deactivateAccount(@PathVariable String username) {
        // 验证用户权限（只能注销自己的账户）
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!username.equals(authentication.getName())) {
            return ResponseResult.fail("无权限注销他人账户");
        }

        return userService.deactivateAccount(username);
    }

}
