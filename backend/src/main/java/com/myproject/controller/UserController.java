package com.myproject.controller;

import com.myproject.dto.UserLoginDTO;
import com.myproject.dto.UserRegisterDTO;
import com.myproject.dto.ForgetPasswordDTO;
import com.myproject.vo.UserLoginVO;
import com.myproject.vo.UserInfoVO;
import com.myproject.service.UserService;
import com.myproject.result.ResponseResult;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

//    public UserController(UserService userService) {
//        this.userService = userService;
//    }

    @PostMapping("/register")
    public ResponseResult<String> register(@RequestBody UserRegisterDTO registerDTO) {
        return userService.register(registerDTO);
    }

    @PostMapping("/login")
    public ResponseResult<UserLoginVO> login(@RequestBody UserLoginDTO loginDTO) {
        return userService.login(loginDTO);
    }

    @PostMapping("/forget-password")
    public ResponseResult<String> forgetPassword(@RequestBody ForgetPasswordDTO forgetPasswordDTO) {
        return userService.forgetPassword(forgetPasswordDTO);
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
