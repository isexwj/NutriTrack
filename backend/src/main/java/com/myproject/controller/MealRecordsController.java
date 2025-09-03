package com.myproject.controller;


import com.myproject.entity.MealRecord;
import com.myproject.result.ResponseResult;
import com.myproject.service.IMealRecordsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 用户饮食记录表 前端控制器
 * </p>
 *
 * @author tian
 * @since 2025-09-03
 */
@RestController
@RequestMapping("/meal")
@RequiredArgsConstructor
public class MealRecordsController {

    private final IMealRecordsService mealRecordsService;

    @GetMapping("/threeStatus")
    public ResponseResult threeStatus(@AuthenticationPrincipal UserDetails userDetails) {
        Map<String, Boolean> threeStatus = mealRecordsService.checkMeals(userDetails.getUsername(), LocalDate.now());
        return ResponseResult.success(threeStatus);
    }

    @GetMapping("/todayData")
    public ResponseResult Data(@AuthenticationPrincipal UserDetails userDetails,@RequestParam("date") LocalDate date) {
        Map<String, Object> stats = mealRecordsService.getStats(userDetails.getUsername(),date);
        return ResponseResult.success(stats);
    }

    @GetMapping("/recent")
    public ResponseResult recent(@AuthenticationPrincipal UserDetails userDetails) {
        List<MealRecord> recent = mealRecordsService.getRecent(userDetails.getUsername());
        return ResponseResult.success(recent);
    }




}
