package com.myproject.controller;

import com.myproject.dto.MealRecordCreateDTO;
import com.myproject.result.ResponseResult;
import com.myproject.service.MealRecordService;
import com.myproject.vo.MealRecordVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/meal-records")
public class MealRecordController {

    private final MealRecordService mealRecordService;

    @Autowired
    public MealRecordController(MealRecordService mealRecordService) {
        this.mealRecordService = mealRecordService;
    }

    @PostMapping("/create")
    public ResponseResult<MealRecordVO> createMealRecord(@Validated @ModelAttribute MealRecordCreateDTO createDTO) {
        try {
            // 从安全上下文中获取当前用户ID ?
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Long userId = Long.parseLong(authentication.getName());

            MealRecordVO result = mealRecordService.createMealRecord(createDTO, userId);
            return ResponseResult.success(result);
        } catch (Exception e) {
            return ResponseResult.fail("添加饮食记录失败: " + e.getMessage());
        }
    }
}