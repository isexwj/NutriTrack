/**
 * 文件路径: src/main/java/com/myproject/vo/DailyAnalysisReportVO.java
 * 说明: 返回给前端的分析报告 VO（仅包含前端展示所需字段）
 */

package com.myproject.vo;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class DailyAnalysisReportVO {
    // 营养占比（百分比）：{ "carbs":45, "protein":25, "fat":30 }
    private Map<String, Integer> nutrition;

    // 营养文字摘要
    private String nutritionSummary;

    // 健康建议列表
    private List<String> suggestions;

    // 改进建议列表
    private List<String> improvements;

    // 健康评分（0-100）
    private Double healthScore;

    // 最后更新时间（字符串）
    private String lastUpdated;
}
