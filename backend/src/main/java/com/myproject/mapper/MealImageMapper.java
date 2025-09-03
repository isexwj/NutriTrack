/**
 * 文件路径: src/main/java/com/myproject/mapper/MealImageMapper.java
 * 说明: meal_images 表的 Mapper，提供按 recordId 查询图片列表的方法。
 */

package com.myproject.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.myproject.entity.MealImage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface MealImageMapper extends BaseMapper<MealImage> {
    @Select("SELECT * FROM meal_images WHERE record_id = #{recordId}")
    List<MealImage> selectByRecordId(@Param("recordId") Long recordId);
}
