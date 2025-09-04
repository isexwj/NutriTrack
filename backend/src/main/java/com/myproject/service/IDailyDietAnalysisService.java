package com.myproject.service;

import com.myproject.entity.DailyDietAnalysis;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 用户的每日饮食汇总分析报告 服务类
 * </p>
 *
 * @author tian
 * @since 2025-09-03
 */
public interface IDailyDietAnalysisService extends IService<DailyDietAnalysis> {

    List<String> getAISuggestions(String username);

    List<Map<String, Object>> getCommunityRanking();
}
