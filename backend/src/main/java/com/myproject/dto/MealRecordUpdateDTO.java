package com.myproject.dto;

import com.myproject.enums.MealType;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class MealRecordUpdateDTO {
    private MealType mealType;
    private String description;
    private Integer calories;
    private BigDecimal rating;
    private LocalDate recordDate;
    private Boolean isShared;
    private List<MultipartFile> newImages; // 新上传的图片
    private List<Long> deletedImageIds; // 要删除的图片ID
}