package com.myproject.controller;

import com.myproject.service.CommunityService;
import com.myproject.vo.CommunityRecordVO;
import com.myproject.vo.CommentVO;
import com.myproject.result.ResponseResult;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/community")
@RequiredArgsConstructor
public class CommunityController {

    private final CommunityService communityService;

    /**
     * 获取社区饮食记录列表
     */
    @GetMapping("/records")
    public ResponseResult<List<CommunityRecordVO>> getCommunityRecords(
            @RequestParam(required = false) String mealType,@RequestParam String username) {
        return communityService.getCommunityRecords(mealType,username);
    }

    /**
     * 获取单个饮食记录的详细信息
     */
    @GetMapping("/records/{recordId}")
    public ResponseResult<CommunityRecordVO> getRecordDetail(@PathVariable Long recordId) {
        return communityService.getRecordDetail(recordId);
    }

    /**
     * 点赞饮食记录
     */
    @PostMapping("/records/{recordId}/like")
    public ResponseResult<String> likeRecord(@PathVariable Long recordId, @RequestParam String username) {
        return communityService.likeRecord(recordId, username);
    }

    /**
     * 取消点赞饮食记录
     */
    @DeleteMapping("/records/{recordId}/like")
    public ResponseResult<String> unlikeRecord(@PathVariable Long recordId, @RequestParam String username) {
        return communityService.unlikeRecord(recordId, username);
    }

    /**
     * 发表评论
     */
    @PostMapping("/records/{recordId}/comments")
    public ResponseResult<CommentVO> createComment(
            @PathVariable Long recordId,
            @RequestParam String username,
            @RequestParam String content,
            @RequestParam(required = false) Long parentCommentId) {
        return communityService.createComment(recordId, username, content, parentCommentId);
    }

    /**
     * 删除评论
     */
    @DeleteMapping("/comments/{commentId}")
    public ResponseResult<String> deleteComment(@PathVariable Long commentId, @RequestParam Long userId) {
        return communityService.deleteComment(commentId, userId);
    }

    /**
     * 获取饮食记录的所有评论
     */
    @GetMapping("/records/{recordId}/comments")
    public ResponseResult<List<CommentVO>> getRecordComments(@PathVariable Long recordId) {
        return communityService.getRecordComments(recordId);
    }

    /**
     * 分享饮食记录到社区
     */
    @PostMapping("/records/{recordId}/share")
    public ResponseResult<String> shareRecord(@PathVariable Long recordId, @RequestParam Long userId) {
        return communityService.shareRecord(recordId, userId);
    }

    /**
     * 取消分享饮食记录
     */
    @DeleteMapping("/records/{recordId}/share")
    public ResponseResult<String> unshareRecord(@PathVariable Long recordId, @RequestParam Long userId) {
        return communityService.unshareRecord(recordId, userId);
    }
}