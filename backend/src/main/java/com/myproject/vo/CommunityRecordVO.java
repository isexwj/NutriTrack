package com.myproject.vo;

import com.myproject.enums.MealType;
import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommunityRecordVO {
    private Long id;
    private MealType type;
    private String description;
    private Integer calories;
    private BigDecimal rating;
    private List<String> images;
    private Integer likes;
    private Boolean isLiked;
    private List<CommentVO> comments;
    private Boolean showComments;
    private String newComment;
    private UserInfoVO user;
    private LocalDateTime createdAt;
}