package com.myproject.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.myproject.entity.MealImage;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MealImageMapper extends BaseMapper<MealImage> {
    // 继承BaseMapper提供基本的CRUD操作
    // 可根据需要添加自定义查询方法
}