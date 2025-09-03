package com.myproject.controller;


import com.myproject.result.ResponseResult;
import com.myproject.service.IDailyDietAnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 用户的每日饮食汇总分析报告 前端控制器
 * </p>
 *
 * @author tian
 * @since 2025-09-03
 */
@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class DailyDietAnalysisController {

    private final IDailyDietAnalysisService DailyDietAnalysisService;

    @GetMapping("/suggestions")
    public ResponseResult<List<String>> getAISuggestions(@AuthenticationPrincipal UserDetails userDetails) {
        List<String> suggestions = DailyDietAnalysisService.getAISuggestions(userDetails.getUsername());
        return ResponseResult.success(suggestions);
    }

    @GetMapping("/ranking")
    public ResponseResult getRanking() {
        List<Map<String, Object>> ranking = DailyDietAnalysisService.getCommunityRanking();
        return ResponseResult.success(ranking);
    }

}
