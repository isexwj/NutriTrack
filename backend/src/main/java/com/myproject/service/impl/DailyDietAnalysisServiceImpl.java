package com.myproject.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.myproject.entity.DailyDietAnalysis;
import com.myproject.entity.User;
import com.myproject.mapper.DailyDietAnalysisMapper;
import com.myproject.mapper.UserMapper;
import com.myproject.service.IDailyDietAnalysisService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 用户的每日饮食汇总分析报告 服务实现类
 * </p>
 *
 * @author tian
 * @since 2025-09-03
 */
@Service
@RequiredArgsConstructor
public class DailyDietAnalysisServiceImpl extends ServiceImpl<DailyDietAnalysisMapper, DailyDietAnalysis> implements IDailyDietAnalysisService {
    private final DailyDietAnalysisMapper dailyDietAnalysisMapper;
    private final UserServiceImpl userService;

    @Override
    public List<String> getAISuggestions(String username) {
        User user = userService.findByUsername(username);
        if (user == null) throw new RuntimeException("用户不存在");

        DailyDietAnalysis latestAnalysis = dailyDietAnalysisMapper.selectOne(
                new QueryWrapper<DailyDietAnalysis>()
                        .eq("user_id", user.getId())
                        .orderByDesc("created_at")
                        .last("LIMIT 1")  // 只取一条
        );

        List<String> suggestions = new ArrayList<>();
        if (latestAnalysis != null) {
            if (latestAnalysis.getHealthSuggestions() != null && !latestAnalysis.getHealthSuggestions().isEmpty()) {
                suggestions.add(latestAnalysis.getHealthSuggestions());
            }
            if (latestAnalysis.getImprovementSuggestions() != null && !latestAnalysis.getImprovementSuggestions().isEmpty()) {
                suggestions.add(latestAnalysis.getImprovementSuggestions());
            }
            if (latestAnalysis.getNutritionSummary() != null && !latestAnalysis.getNutritionSummary().isEmpty()) {
                suggestions.add(latestAnalysis.getNutritionSummary());
            }
        }
        return suggestions;
    }

    @Override
    public List<Map<String, Object>> getCommunityRanking() {
        return this.baseMapper.getTopHealthScores(5);
    }
}
