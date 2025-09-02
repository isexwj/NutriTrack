package com.myproject.vo;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserLoginVO {
    
    private String token;
    private String username;
    private String role;
    private Long userId;
}
