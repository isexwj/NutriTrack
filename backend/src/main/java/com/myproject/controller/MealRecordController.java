package com.myproject.controller;

import com.myproject.dto.MealRecordCreateDTO;
import com.myproject.dto.MealRecordQueryDTO;
import com.myproject.dto.MealRecordUpdateDTO;
import com.myproject.entity.User;
import com.myproject.result.ResponseResult;
import com.myproject.service.MealRecordService;
import com.myproject.service.UserService;
import com.myproject.vo.MealRecordVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/meal-records")

public class MealRecordController {

    private final MealRecordService mealRecordService;
    private final UserService userService;

    @Autowired
    public MealRecordController(MealRecordService mealRecordService, UserService userService) {
        this.mealRecordService = mealRecordService;
        this.userService = userService;
    }

    /**
     * 获取当前用户的所有饮食记录
     */
    @GetMapping("/query_me")
    public ResponseResult<List<MealRecordVO>> getMyMealRecords() {
        try {
            // 从安全上下文中获取当前用户名
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();

            // 通过用户名查询用户信息
            User user = userService.findByUsername(username);
            if (user == null) {
                return ResponseResult.fail("用户不存在");
            }

            Long userId = user.getId();
            List<MealRecordVO> result = mealRecordService.getMealRecordsByUserId(userId);
            System.out.println(result);
            return ResponseResult.success(result);
        } catch (Exception e) {
            return ResponseResult.fail("获取饮食记录失败: " + e.getMessage());
        }
    }

    /**
     * 根据条件查询饮食记录
     */
    @GetMapping("/query")
    public ResponseResult<List<MealRecordVO>> getMealRecordsByQuery(@ModelAttribute MealRecordQueryDTO queryDTO) {
        try {
            // 从安全上下文中获取当前用户名
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();

            // 通过用户名查询用户信息
            User user = userService.findByUsername(username);
            if (user == null) {
                return ResponseResult.fail("用户不存在");
            }

            Long userId = user.getId();
            List<MealRecordVO> result = mealRecordService.getMealRecordsByQuery(userId, queryDTO);
            return ResponseResult.success(result);
        } catch (Exception e) {
            return ResponseResult.fail("查询饮食记录失败: " + e.getMessage());
        }
    }

    /**
     * 获取特定饮食记录的详情
     */
    @GetMapping("/{id}")
    public ResponseResult<MealRecordVO> getMealRecordById(@PathVariable Long id) {
        try {
            // 从安全上下文中获取当前用户名
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();

            // 通过用户名查询用户信息
            User user = userService.findByUsername(username);
            if (user == null) {
                return ResponseResult.fail("用户不存在");
            }

            Long userId = user.getId();
            MealRecordVO result = mealRecordService.getMealRecordById(id, userId);
            return ResponseResult.success(result);
        } catch (Exception e) {
            return ResponseResult.fail("获取饮食记录详情失败: " + e.getMessage());
        }
    }

    /**
     * 添加饮食记录
     */
    @PostMapping("/create")
    public ResponseResult<MealRecordVO> createMealRecord(@Validated @ModelAttribute MealRecordCreateDTO createDTO) {
        try {
            // 从安全上下文中获取当前用户名
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();

            // 通过用户名查询用户信息
            User user = userService.findByUsername(username);
            if (user == null) {
                return ResponseResult.fail("用户不存在");
            }

            Long userId = user.getId();
            MealRecordVO result = mealRecordService.createMealRecord(createDTO, userId);
            return ResponseResult.success(result);
        } catch (Exception e) {
            return ResponseResult.fail("添加饮食记录失败: " + e.getMessage());
        }
    }

    /**
     * 更新饮食记录
     */
    @PutMapping("/update/{id}")
    public ResponseResult<MealRecordVO> updateMealRecord(
            @PathVariable Long id,
            @Validated @ModelAttribute MealRecordUpdateDTO updateDTO) {
        try {
            // 从安全上下文中获取当前用户名
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();

            // 通过用户名查询用户信息
            User user = userService.findByUsername(username);
            if (user == null) {
                return ResponseResult.fail("用户不存在");
            }

            Long userId = user.getId();
            MealRecordVO result = mealRecordService.updateMealRecord(id, updateDTO, userId);
            return ResponseResult.success(result);
        } catch (Exception e) {
            return ResponseResult.fail("更新饮食记录失败: " + e.getMessage());
        }
    }

    /**
     * 删除饮食记录
     */
    @DeleteMapping("/delete/{id}")
    public ResponseResult<String> deleteMealRecord(@PathVariable Long id) {
        try {
            // 从安全上下文中获取当前用户名
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();

            // 通过用户名查询用户信息
            User user = userService.findByUsername(username);
            if (user == null) {
                return ResponseResult.fail("用户不存在");
            }

            Long userId = user.getId();
            mealRecordService.deleteMealRecord(id, userId);
            return ResponseResult.success("删除成功");
        } catch (Exception e) {
            return ResponseResult.fail("删除饮食记录失败: " + e.getMessage());
        }
    }

    /**
     * 切换分享状态
     */
    @PatchMapping("/share/{id}")
    public ResponseResult<String> toggleShareStatus(@PathVariable Long id, @RequestBody Map<String, Boolean> request) {
        try {
            // 从安全上下文中获取当前用户名
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();

            // 通过用户名查询用户信息
            User user = userService.findByUsername(username);
            if (user == null) {
                return ResponseResult.fail("用户不存在");
            }

            Long userId = user.getId();
            Boolean isShared = request.get("isShared");
            mealRecordService.toggleShareStatus(id, isShared, userId);

            String message = isShared ? "已分享到社区" : "已取消分享";
            return ResponseResult.success(message);
        } catch (Exception e) {
            return ResponseResult.fail("切换分享状态失败: " + e.getMessage());
        }
    }
}