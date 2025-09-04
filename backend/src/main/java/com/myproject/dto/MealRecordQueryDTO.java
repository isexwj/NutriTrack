package com.myproject.dto;

import com.myproject.enums.MealType;
import lombok.Data;
import java.time.LocalDate;

@Data
public class MealRecordQueryDTO {
    private MealType mealType;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean isShared;
    private String keyword; // 用于描述搜索
}