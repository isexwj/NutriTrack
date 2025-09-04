package com.myproject.vo;

import lombok.Data;

@Data
public class MealImageVO {
    private Long id;
    private String imageUrl;
    private Integer uploadOrder;
}
