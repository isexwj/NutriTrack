package com.myproject.mapper;

import com.myproject.entity.DailyDietAnalysis;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 用户的每日饮食汇总分析报告 Mapper 接口
 * </p>
 *
 * @author tian
 * @since 2025-09-03
 */
public interface DailyDietAnalysisMapper extends BaseMapper<DailyDietAnalysis> {

    List<Map<String, Object>> getTopHealthScores(int topN);
}
