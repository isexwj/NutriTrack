package com.myproject.service;

import com.myproject.entity.MealRecord;
import com.baomidou.mybatisplus.extension.service.IService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 用户饮食记录表 服务类
 * </p>
 *
 * @author tian
 * @since 2025-09-03
 */
public interface IMealRecordsService extends IService<MealRecord> {

    Map<String, Boolean> checkMeals(String username, LocalDate now);

    Map<String, Object> getStats(String username, LocalDate now);

    List<MealRecord> getRecent(String username);
}
