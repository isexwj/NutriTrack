package com.myproject.dto;

import lombok.Data;

@Data
public class RegisterStep2DTO {

    private String regToken;

    private String email;

    private String emailCode;
}


