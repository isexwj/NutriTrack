package com.myproject.dto;

import lombok.Data;

@Data
public class ForgetPasswordDTO {
    
    private String username;
    private String newPassword;
}
