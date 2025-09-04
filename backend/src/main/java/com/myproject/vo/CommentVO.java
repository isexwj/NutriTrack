package com.myproject.vo;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommentVO {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private UserInfoVO user;
    private String content;
    private LocalDateTime createdAt;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long parentCommentId;
    private java.util.List<CommentVO> replies;
}