package com.myproject.controller;

import com.myproject.dto.UserLoginDTO;
import com.myproject.dto.UserRegisterDTO;
import com.myproject.dto.ForgetPasswordDTO;
import com.myproject.vo.UserLoginVO;
import com.myproject.vo.UserInfoVO;
import com.myproject.service.UserService;
import com.myproject.result.ResponseResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

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
}
