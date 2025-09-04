package com.myproject.vo;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserInfoVO {
    
    private Long id;
    private String username;
    private String nickname;
    private String email;
    private String phoneNumber;
    private String role;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    
    // 不包含敏感信息如密码
}
