package com.myproject.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import com.myproject.entity.RecordComment;
import java.util.List;

@Mapper
public interface RecordCommentMapper extends BaseMapper<RecordComment> {

    /**
     * 根据记录ID查询评论
     * @param recordId 记录ID
     * @return 评论列表
     */
    @Select("SELECT * FROM record_comments WHERE record_id = #{recordId} ORDER BY created_at ASC")
    List<RecordComment> selectByRecordId(@Param("recordId") Long recordId);

    /**
     * 统计记录的评论数量
     * @极地 recordId 记录ID
     * @return 评论数量
     */
    @Select("SELECT COUNT(*) FROM record_comments WHERE record_id = #{recordId}")
    Integer countCommentsByRecordId(@Param("recordId") Long recordId);
}