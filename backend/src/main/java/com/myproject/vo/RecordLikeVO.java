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
public class RecordLikeVO {
    private Long id;
    private Long recordId;
    private Long userId;
    private LocalDateTime createdAt;
}