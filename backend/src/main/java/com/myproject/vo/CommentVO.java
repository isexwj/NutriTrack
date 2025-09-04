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
public class CommentVO {
    private Long id;
    private UserInfoVO user;
    private String content;
    private LocalDateTime createdAt;
}