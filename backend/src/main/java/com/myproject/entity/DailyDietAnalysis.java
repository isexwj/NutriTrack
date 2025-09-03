package com.myproject.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 文件路径: src/main/java/com/myproject/entity/DailyDietAnalysis.java
 * 说明: 对应数据库表 daily_diet_analysis（字段一一对应）
 */
@Data
@TableName("daily_diet_analysis")
public class DailyDietAnalysis {
    @TableId
    @TableField("id")
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("analysis_date")
    private LocalDate analysisDate;

    @TableField("health_score")
    private BigDecimal healthScore;

    @TableField("carb_grams")
    private BigDecimal carbGrams;

    @TableField("protein_grams")
    private BigDecimal proteinGrams;

    @TableField("fat_grams")
    private BigDecimal fatGrams;

    @TableField("nutrition_summary")
    private String nutritionSummary;

    @TableField("health_suggestions")
    private String healthSuggestions;

    @TableField("improvement_suggestions")
    private String improvementSuggestions;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
