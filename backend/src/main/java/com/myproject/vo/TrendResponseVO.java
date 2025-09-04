package com.myproject.vo;

import lombok.Data;

import java.util.List;

/**
 * 趋势数据返回：dates 与两组数值（calories 与 healthScore）
 */
@Data
public class TrendResponseVO {
    // 如 ["2025-09-01", "2025-09-02", ...]
    private List<String> dates;
    // 与 dates 对应的每天总卡路里（可能为 0）
    private List<Integer> calories;
    // 与 dates 对应的每天健康指数（可能为 null）
    private List<Double> healthScores;
}
