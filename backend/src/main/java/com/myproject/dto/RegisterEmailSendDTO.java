package com.myproject.dto;

import lombok.Data;

@Data
public class RegisterEmailSendDTO {

    private String regToken;

    private String email;
}


