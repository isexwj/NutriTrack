// MealRecordVO.java
package com.myproject.vo;

import com.myproject.enums.MealType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class MealRecordVO {
    private Long id;
    private MealType mealType;
    private String description;
    private Integer calories;
    private BigDecimal rating;
    private LocalDate recordDate;
    private Boolean isShared;
    private LocalDateTime createdAt;
    private List<String> imageUrls; // 图片URL列表
}