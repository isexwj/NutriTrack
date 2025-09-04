/**
 * 文件路径: src/main/java/com/myproject/mapper/DailyDietAnalysisMapper.java
 * 说明: daily_diet_analysis 表的 Mapper，提供按 user+date 查询的自定义方法。
 */

package com.myproject.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.myproject.entity.DailyDietAnalysis;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Mapper
public interface DailyDietAnalysisMapper extends BaseMapper<DailyDietAnalysis> {
    List<Map<String, Object>> getTopHealthScores(int topN);
    
    @Select("SELECT * FROM daily_diet_analysis WHERE user_id = #{userId} AND analysis_date = #{date}")
    DailyDietAnalysis selectByUserAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);
}
