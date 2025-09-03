// MealRecordCreateDTO.java
package com.myproject.dto;

import com.myproject.enums.MealType;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class MealRecordCreateDTO {
    private MealType mealType;

    private String description;

    private Integer calories;

    private BigDecimal rating;

    private LocalDate recordDate;

    private Boolean isShared = false;

    private List<MultipartFile> images;
}