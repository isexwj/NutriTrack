package com.myproject.dto;

import lombok.Data;

@Data
public class ForgetPasswordResetDTO {

    private String email;

    private String emailCode;

    private String newPassword;

    private String confirmPassword;
}


