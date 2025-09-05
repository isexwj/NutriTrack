package com.myproject.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.myproject.entity.DailyDietAnalysis;
import com.myproject.entity.MealRecord;
import com.myproject.entity.User;
import com.myproject.mapper.DailyDietAnalysisMapper;
import com.myproject.mapper.MealRecordsMapper;
import com.myproject.service.IMealRecordsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.myproject.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * <p>
 * 用户饮食记录表 服务实现类
 * </p>
 *
 * @author tian
 * @since 2025-09-03
 */
@Service
@RequiredArgsConstructor
public class MealRecordsServiceImpl extends ServiceImpl<MealRecordsMapper, MealRecord> implements IMealRecordsService {


    private final MealRecordsMapper mealRecordMapper;
    private final UserService userService;
    private final DailyDietAnalysisMapper dailyDietAnalysisMapper;

//    public MealRecordsServiceImpl(MealRecordsMapper mealRecordMapper) {
//        this.mealRecordMapper = mealRecordMapper;
//    }

    @Override
    public Map<String, Boolean> checkMeals(String username, LocalDate now) {
        QueryWrapper<MealRecord> wrapper = new QueryWrapper<>();
        User user = userService.findByUsername(username);
        wrapper.select("meal_type", "COUNT(*) as cnt")
                .eq("user_id", user.getId())
                .eq("record_date", now)
                .groupBy("meal_type");

        List<Map<String, Object>> list = mealRecordMapper.selectMaps(wrapper);

        Map<String, Boolean> result = new HashMap<>();
        result.put("breakfast", false);
        result.put("lunch", false);
        result.put("dinner", false);

        for (Map<String, Object> row : list) {
            Object raw = row.get("meal_type");
            if (raw == null) {
                raw = row.get("mealType"); // 兼容 map-underscore-to-camel-case
            }
            String mealType = raw == null ? null : raw.toString();
            Long count = ((Number) row.get("cnt")).longValue();
            if (count <= 0 || mealType == null) {
                continue;
            }
            String key = normalizeMealType(mealType);
            if (result.containsKey(key)) {
                result.put(key, true);
            }
        }

        return result;
    }

    private String normalizeMealType(String mealType) {
        String s = mealType.trim().toLowerCase();
        // 统一一些可能的存储值
        switch (s) {
            case "breakfast":
            case "早餐":
            case "morning":
                return "breakfast";
            case "lunch":
            case "午餐":
            case "noon":
                return "lunch";
            case "dinner":
            case "晚餐":
            case "supper":
                return "dinner";
            default:
                return s;
        }
    }

    @Override
    public Map<String, Object> getStats(String username, LocalDate now) {
        User user = userService.findByUsername(username);

        Long userId = user.getId();

        // 2. 查询今日餐记录
        List<MealRecord> todayRecords = mealRecordMapper.selectList(
                new QueryWrapper<MealRecord>()
                        .eq("user_id", userId)
                        .eq("record_date", now)
        );

        // 3. 计算统计数据
        int totalCalories = todayRecords.stream()
                .filter(r -> r.getCalories() != null)
                .mapToInt(MealRecord::getCalories)
                .sum();

        int meals = todayRecords.size();

        double avgRating = todayRecords.stream()
                .filter(r -> r.getRating() != null)
                .mapToDouble(r -> r.getRating().doubleValue())
                .average()
                .orElse(0.0);

        // 简单健康评分：基础分 100，按热量偏差扣分 dailyDietAnalysisMapper
        QueryWrapper<DailyDietAnalysis> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId)
                .orderByDesc("created_at") // 按创建时间降序
                .last("LIMIT 2");          // 只取一条

        List<DailyDietAnalysis> latestRecords = dailyDietAnalysisMapper.selectList(wrapper);
        double healthScore=0;
        if(now.isEqual(LocalDate.now())&&latestRecords.size()>0) {
            healthScore = latestRecords.get(0).getHealthScore().doubleValue();
        }else if(now.isBefore(LocalDate.now())&&latestRecords.size()>1) {
            healthScore = latestRecords.get(1).getHealthScore().doubleValue();
        }

        // 4. 返回结果
        return Map.of(
                "calories", totalCalories,
                "meals", meals,
                "rating", Math.round(avgRating * 10) / 10.0, // 保留 1 位小数
                "healthScore", healthScore
        );
    }

    @Override
    public List<MealRecord> getRecent(String username) {
        QueryWrapper<MealRecord> queryWrapper = new QueryWrapper<>();
        User user = userService.findByUsername(username);
        queryWrapper.eq("user_id", user.getId())      // 根据 user_id 过滤
                .orderByDesc("created_at")   // 按创建时间倒序
                .last("LIMIT 5");             // 只取前五条

        return mealRecordMapper.selectList(queryWrapper);
    }
}
