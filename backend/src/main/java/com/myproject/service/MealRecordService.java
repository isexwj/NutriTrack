package com.myproject.service;

import com.myproject.dto.MealRecordCreateDTO;
import com.myproject.dto.MealRecordQueryDTO;
import com.myproject.dto.MealRecordUpdateDTO;
import com.myproject.entity.MealRecord;
import com.myproject.vo.MealRecordVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface MealRecordService {
    MealRecordVO createMealRecord(MealRecordCreateDTO createDTO, Long userId);
    List<MealRecordVO> getMealRecordsByUserId(Long userId);
    List<MealRecordVO> getMealRecordsByQuery(Long userId, MealRecordQueryDTO queryDTO);
    MealRecordVO getMealRecordById(Long id, Long userId);
    MealRecordVO updateMealRecord(Long id, MealRecordUpdateDTO updateDTO, Long userId);
    void deleteMealRecord(Long id, Long userId);
    void toggleShareStatus(Long id, Boolean isShared, Long userId);
}