package com.myproject.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.myproject.entity.MealRecord;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MealRecordMapper extends BaseMapper<MealRecord> {
}