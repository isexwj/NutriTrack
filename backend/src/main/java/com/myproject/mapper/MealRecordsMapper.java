package com.myproject.mapper;

import com.myproject.entity.MealRecord;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 用户饮食记录表 Mapper 接口
 * </p>
 *
 * @author tian
 * @since 2025-09-03
 */
@Mapper
public interface MealRecordsMapper extends BaseMapper<MealRecord> {

}
