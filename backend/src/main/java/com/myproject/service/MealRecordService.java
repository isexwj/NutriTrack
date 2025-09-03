package com.myproject.service;

import com.myproject.dto.MealRecordCreateDTO;
import com.myproject.entity.MealRecord;
import com.myproject.vo.MealRecordVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface MealRecordService {
    MealRecordVO createMealRecord(MealRecordCreateDTO createDTO, Long userId);
    List<MealRecordVO> getMyRecords(Long userId);
}