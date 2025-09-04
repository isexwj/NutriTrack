package com.myproject.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.myproject.entity.*;
import com.myproject.mapper.*;
import com.myproject.service.CommunityService;
import com.myproject.service.UserService;
import com.myproject.vo.*;
import com.myproject.result.ResponseResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class CommunityServiceImpl implements CommunityService {

    private final MealRecordMapper mealRecordMapper;
    private final RecordLikeMapper recordLikeMapper;
    private final RecordCommentMapper recordCommentMapper;
    private final MealImageMapper mealImageMapper;
    private final UserMapper userMapper;
    private final UserService userService;
    private final NotificationMapper notificationMapper;


    @Override
    public ResponseResult<List<CommunityRecordVO>> getCommunityRecords(String mealType,String username) {
        try {
            Long currentUserId = userService.getUserInfo(username).getData().getId();

            List<MealRecord> records = mealRecordMapper.selectSharedRecords(mealType);

            List<CommunityRecordVO> recordVOs = records.stream()
                    .map(record -> {
                        CommunityRecordVO vo = convertToVO(record);
                        // 关键：设置点赞状态
                        vo.setIsLiked(isRecordLikedByUser(record.getId(), currentUserId));
                        return vo;
                    })
                    .collect(Collectors.toList());

            return ResponseResult.success(recordVOs);
        } catch (Exception e) {
            return ResponseResult.fail("获取社区记录失败: " + e.getMessage());
        }
    }

    @Override
    public ResponseResult<CommunityRecordVO> getRecordDetail(Long recordId) {
        try {
            MealRecord record = mealRecordMapper.selectById(recordId);
            if (record == null) {
                return ResponseResult.fail("记录不存在");
            }
            return ResponseResult.success(convertToVO(record));
        } catch (Exception e) {
            return ResponseResult.fail("获取记录详情失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public ResponseResult<String> likeRecord(Long recordId, String username) {

        long userId=userService.getUserInfo(username).getData().getId();
        try {
            if (isRecordLikedByUser(recordId, userId)) {
                return ResponseResult.fail("已点赞过该记录");
            }

            RecordLike like = new RecordLike();
            like.setRecordId(recordId);
            like.setUserId(userId);
            recordLikeMapper.insert(like);

            // 通知：被点赞
            MealRecord record = mealRecordMapper.selectById(recordId);
            if (record != null && !record.getUserId().equals(userId)) {
                Notification n = new Notification();
                n.setUserId(record.getUserId());
                n.setSenderUserId(userId);
                n.setNotificationType(com.myproject.enums.NotificationType.LIKE);
                n.setTargetRecordId(recordId);
                n.setContent("有人赞了你的记录");
                n.setIsRead(0);
                n.setCreatedAt(LocalDateTime.now());
                notificationMapper.insert(n);
            }
            return ResponseResult.success("点赞成功");
        } catch (Exception e) {
            return ResponseResult.fail("点赞失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public ResponseResult<String> unlikeRecord(Long recordId, String username) {
        long userId=userService.getUserInfo(username).getData().getId();
        try {
            QueryWrapper<RecordLike> wrapper = new QueryWrapper<>();
            wrapper.eq("record_id", recordId)
                    .eq("user_id", userId);

            int result = recordLikeMapper.delete(wrapper);
            return result > 0
                    ? ResponseResult.success("取消点赞成功")
                    : ResponseResult.fail("取消点赞失败");
        } catch (Exception e) {
            return ResponseResult.fail("取消点赞失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public ResponseResult<CommentVO> createComment(Long recordId, String username, String content, Long parentCommentId) {
        try {
            long userId=userService.getUserInfo(username).getData().getId();
            RecordComment comment = new RecordComment();
            comment.setRecordId(recordId);
            comment.setUserId(userId);
            comment.setContent(content);
            comment.setParentCommentId(parentCommentId);

            // 父评论校验，避免外键错误
            if (parentCommentId != null) {
                RecordComment parent = recordCommentMapper.selectById(parentCommentId);
                if (parent == null || !recordId.equals(parent.getRecordId())) {
                    return ResponseResult.fail("父评论不存在或已被删除");
                }
            }

            recordCommentMapper.insert(comment);

            // 通知：记录作者
            MealRecord record = mealRecordMapper.selectById(recordId);
            if (record != null && !record.getUserId().equals(userId)) {
                Notification n1 = new Notification();
                n1.setUserId(record.getUserId());
                n1.setSenderUserId(userId);
                n1.setNotificationType(com.myproject.enums.NotificationType.COMMENT);
                n1.setTargetRecordId(recordId);
                n1.setContent("有人评论了你的记录");
                n1.setIsRead(0);
                n1.setCreatedAt(LocalDateTime.now());
                notificationMapper.insert(n1);
            }

            // 通知：被回复的评论作者（若有且不同人）
            if (parentCommentId != null) {
                RecordComment parent = recordCommentMapper.selectById(parentCommentId);
                if (parent != null && !parent.getUserId().equals(userId)) {
                    Notification n2 = new Notification();
                    n2.setUserId(parent.getUserId());
                    n2.setSenderUserId(userId);
                    n2.setNotificationType(com.myproject.enums.NotificationType.COMMENT);
                    n2.setTargetRecordId(recordId);
                    n2.setContent("有人回复了你的评论");
                    n2.setIsRead(0);
                    n2.setCreatedAt(LocalDateTime.now());
                    notificationMapper.insert(n2);
                }
            }

            return ResponseResult.success(convertCommentToVO(comment));
        } catch (Exception e) {
            return ResponseResult.fail("发表评论失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public ResponseResult<String> deleteComment(Long commentId, Long userId) {
        try {
            RecordComment comment = recordCommentMapper.selectById(commentId);
            if (comment == null) {
                return ResponseResult.fail("评论不存在");
            }

            if (!comment.getUserId().equals(userId)) {
                return ResponseResult.fail("无权删除该评论");
            }

            int result = recordCommentMapper.deleteById(commentId);
            return result > 0
                    ? ResponseResult.success("删除评论成功")
                    : ResponseResult.fail("删除评论失败");
        } catch (Exception e) {
            return ResponseResult.fail("删除评论失败: " + e.getMessage());
        }
    }

    @Override
    public ResponseResult<List<CommentVO>> getRecordComments(Long recordId) {
        try {
            List<RecordComment> comments = recordCommentMapper.selectByRecordId(recordId);
            List<CommentVO> commentVOs = comments.stream()
                    .map(this::convertCommentToVO)
                    .collect(Collectors.toList());
            return ResponseResult.success(commentVOs);
        } catch (Exception e) {
            return ResponseResult.fail("获取评论失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public ResponseResult<String> shareRecord(Long recordId, Long userId) {
        try {
            MealRecord record = mealRecordMapper.selectById(recordId);
            if (record == null) {
                return ResponseResult.fail("记录不存在");
            }

            if (!record.getUserId().equals(userId)) {
                return ResponseResult.fail("无权操作极地记录");
            }

            UpdateWrapper<MealRecord> wrapper = new UpdateWrapper<>();
            wrapper.eq("id", recordId)
                    .set("is_shared", true);

            int result = mealRecordMapper.update(null, wrapper);
            return result > 0
                    ? ResponseResult.success("分享成功")
                    : ResponseResult.fail("分享失败");
        } catch (Exception e) {
            return ResponseResult.fail("分享失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public ResponseResult<String> unshareRecord(Long recordId, Long userId) {
        try {
            MealRecord record = mealRecordMapper.selectById(recordId);
            if (record == null) {
                return ResponseResult.fail("记录不存在");
            }

            if (!record.getUserId().equals(userId)) {
                return ResponseResult.fail("无权操作该记录");
            }

            UpdateWrapper<MealRecord> wrapper = new UpdateWrapper<>();
            wrapper.eq("id", recordId)
                    .set("is_shared", false);

            int result = mealRecordMapper.update(null, wrapper);
            return result > 0
                    ? ResponseResult.success("取消分享成功")
                    : ResponseResult.fail("取消分享失败");
        } catch (Exception e) {
            return ResponseResult.fail("取消分享失败: " + e.getMessage());
        }
    }

    // ============ 修复：添加缺失的方法 ============
    @Override
    public boolean isRecordLikedByUser(Long recordId, Long userId) {
        QueryWrapper<RecordLike> wrapper = new QueryWrapper<>();
        wrapper.eq("record_id", recordId)
                .eq("user_id", userId);
        return recordLikeMapper.selectCount(wrapper) > 0;
    }

    @Override
    public Integer getRecordLikeCount(Long recordId) {
        QueryWrapper<RecordLike> wrapper = new QueryWrapper<>();
        wrapper.eq("record_id", recordId);
        return Math.toIntExact(recordLikeMapper.selectCount(wrapper));
    }

    @Override
    public Integer getRecordCommentCount(Long recordId) {
        QueryWrapper<RecordComment> wrapper = new QueryWrapper<>();
        wrapper.eq("record_id", recordId);
        return Math.toIntExact(recordCommentMapper.selectCount(wrapper));
    }

    // ============ 私有转换方法 ============
    private CommunityRecordVO convertToVO(MealRecord record) {
        User user = userMapper.selectById(record.getUserId());

        return CommunityRecordVO.builder()
                .id(record.getId())
                .type(record.getMealType())
                .description(record.getDescription())
                .calories(record.getCalories())
                .rating(record.getRating())
                .images(getRecordImages(record.getId()))
                .likes(getRecordLikeCount(record.getId()))
                .isLiked(false)
                .comments(getRecordCommentsVO(record.getId()))
                .showComments(false)
                .newComment("")
                .user(convertUserToVO(user))
                .createdAt(record.getCreatedAt())
                .build();
    }

    private List<String> getRecordImages(Long recordId) {
        QueryWrapper<MealImage> wrapper = new QueryWrapper<>();
        wrapper.eq("record_id", recordId)
                .orderByAsc("upload_order");
        return mealImageMapper.selectList(wrapper).stream()
                .map(MealImage::getImageUrl)
                .collect(Collectors.toList());
    }

    private List<CommentVO> getRecordCommentsVO(Long recordId) {
        List<RecordComment> comments = recordCommentMapper.selectByRecordId(recordId);
        // 构建父子映射
        java.util.Map<Long, CommentVO> idToVo = new java.util.HashMap<>();
        java.util.List<CommentVO> roots = new java.util.ArrayList<>();
        for (RecordComment c : comments) {
            CommentVO vo = convertCommentToVO(c);
            vo.setReplies(new java.util.ArrayList<>());
            idToVo.put(c.getId(), vo);
        }
        for (RecordComment c : comments) {
            if (c.getParentCommentId() == null) {
                roots.add(idToVo.get(c.getId()));
            } else {
                CommentVO parent = idToVo.get(c.getParentCommentId());
                if (parent != null) {
                    parent.getReplies().add(idToVo.get(c.getId()));
                } else {
                    roots.add(idToVo.get(c.getId()));
                }
            }
        }
        return roots;
    }

    private CommentVO convertCommentToVO(RecordComment comment) {
        User user = userMapper.selectById(comment.getUserId());
        return CommentVO.builder()
                .id(comment.getId())
                .user(convertUserToVO(user))
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .parentCommentId(comment.getParentCommentId())
                .replies(null)
                .build();
    }

    private UserInfoVO convertUserToVO(User user) {
        return UserInfoVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role("USER")
                .createTime(user.getCreatedAt())
                .updateTime(user.getUpdatedAt())
                .build();
    }
}