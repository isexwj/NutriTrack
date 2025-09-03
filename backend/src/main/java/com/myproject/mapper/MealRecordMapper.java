package com.myproject.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.myproject.entity.MealRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface MealRecordMapper extends BaseMapper<MealRecord> {

    /**
     * 查询已分享的饮食记录
     * @param mealType 餐别类型，可为null表示查询所有
     * @return 饮食记录列表
     */
    @Select("SELECT * FROM meal_records WHERE is_shared = 1 " +
            "AND (#{mealType} IS NULL OR meal_type = #{mealType}) " +
            "ORDER BY created_at DESC")
    List<MealRecord> selectSharedRecords(@Param("mealType") String mealType);

    /**
     * 获取记录的图片URL列表
     * @param recordId 记录ID
     * @return 图片URL列表
     */
    @Select("SELECT image_url FROM meal_images WHERE record_id = #{recordId} ORDER BY upload_order")
    List<String> selectRecordImages(@Param("recordId") Long recordId);
}