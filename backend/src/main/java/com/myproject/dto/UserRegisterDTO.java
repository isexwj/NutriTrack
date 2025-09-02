package com.myproject.dto;

import lombok.Data;

@Data
public class UserRegisterDTO {
    
    private String username;
    private String password;
    private String email;
    private String role;
}
