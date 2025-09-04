package com.myproject.dto;

import lombok.Data;

@Data
public class RegisterStep1DTO {

    private String username;

    private String password;

    private String confirmPassword;

    private String nickname;

    private String captchaId;

    private String captchaCode;
}


