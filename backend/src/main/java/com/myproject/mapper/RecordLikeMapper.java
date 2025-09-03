package com.myproject.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.myproject.entity.RecordLike;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface RecordLikeMapper extends BaseMapper<RecordLike> {

    /**
     * 统计记录的点赞数量
     * @param recordId 记录ID
     * @return 点赞数量
     */
    @Select("SELECT COUNT(*) FROM record_likes WHERE record_id = #{recordId}")
    Integer countLikesByRecordId(@Param("recordId") Long recordId);

    /**
     * 检查用户是否已点赞某记录
     * @param recordId 记录ID
     * @param userId 用户ID
     * @return 是否存在点赞记录
     */
    @Select("SELECT COUNT(*) FROM record_likes WHERE record_id = #{recordId} AND user_id = #{userId}")
    boolean existsLike(@Param("recordId") Long recordId, @Param("userId") Long userId);
}