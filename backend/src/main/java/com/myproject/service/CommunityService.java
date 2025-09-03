package com.myproject.service;

import com.myproject.vo.CommunityRecordVO;
import com.myproject.vo.CommentVO;
import com.myproject.result.ResponseResult;

import java.util.List;

public interface CommunityService {

    /**
     * 获取社区饮食记录列表
     */
    ResponseResult<List<CommunityRecordVO>> getCommunityRecords(String mealType,String username);

    /**
     * 获取单个饮食记录的详细信息
     */
    ResponseResult<CommunityRecordVO> getRecordDetail(Long recordId);

    /**
     * 点赞饮食记录
     */
    ResponseResult<String> likeRecord(Long recordId, String username);

    /**
     * 取消点赞饮食记录
     */
    ResponseResult<String> unlikeRecord(Long recordId, String username);

    /**
     * 发表评论
     */
    ResponseResult<CommentVO> createComment(Long recordId, String username, String content, Long parentCommentId);

    /**
     * 删除评论
     */
    ResponseResult<String> deleteComment(Long commentId, Long userId);

    /**
     * 获取饮食记录的所有评论
     */
    ResponseResult<List<CommentVO>> getRecordComments(Long recordId);

    /**
     * 分享饮食记录到社区
     */
    ResponseResult<String> shareRecord(Long recordId, Long userId);

    /**
     * 取消分享饮食记录
     */
    ResponseResult<String> unshareRecord(Long recordId, Long userId);

    /**
     * 获取用户点赞过的记录（内部使用）
     */
    boolean isRecordLikedByUser(Long recordId, Long userId);

    /**
     * 获取记录的点赞数量（内部使用）
     */
    Integer getRecordLikeCount(Long recordId);

    /**
     * 获取记录的评论数量（内部使用）
     */
    Integer getRecordCommentCount(Long recordId);
}