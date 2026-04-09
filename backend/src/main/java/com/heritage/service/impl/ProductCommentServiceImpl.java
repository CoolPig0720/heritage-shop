package com.heritage.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heritage.common.BusinessException;
import com.heritage.dto.CommentCreateRequest;
import com.heritage.dto.CommentLikeVO;
import com.heritage.dto.CommentVO;
import com.heritage.entity.CommentLike;
import com.heritage.entity.Product;
import com.heritage.entity.ProductComment;
import com.heritage.entity.User;
import com.heritage.mapper.CommentLikeMapper;
import com.heritage.mapper.ProductCommentMapper;
import com.heritage.mapper.ProductMapper;
import com.heritage.mapper.UserMapper;
import com.heritage.service.ProductCommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductCommentServiceImpl extends ServiceImpl<ProductCommentMapper, ProductComment>
        implements ProductCommentService {

    private final ProductCommentMapper commentMapper;
    private final CommentLikeMapper likeMapper;
    private final ProductMapper productMapper;
    private final UserMapper userMapper;

    @Override
    public Page<CommentVO> listProductComments(Long productId, Long currentUserId, int page, int size) {
        Page<ProductComment> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<ProductComment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductComment::getProductId, productId)
                .eq(ProductComment::getStatus, 1)
                .isNull(ProductComment::getParentId)
                .orderByDesc(ProductComment::getCreateTime);

        Page<ProductComment> result = commentMapper.selectPage(pageParam, wrapper);
        return convertPage(result, currentUserId);
    }

    @Override
    public Page<CommentVO> listCommentReplies(Long commentId, Long currentUserId, int page, int size) {
        Page<ProductComment> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<ProductComment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductComment::getParentId, commentId)
                .eq(ProductComment::getStatus, 1)
                .orderByAsc(ProductComment::getCreateTime);

        Page<ProductComment> result = commentMapper.selectPage(pageParam, wrapper);
        return convertPage(result, currentUserId);
    }

    @Override
    @Transactional
    public Long createComment(Long userId, CommentCreateRequest request) {
        // 校验商品存在且上架
        Product product = productMapper.selectById(request.getProductId());
        if (product == null || product.getStatus() != 1) {
            throw new BusinessException("商品不存在或已下架");
        }

        ProductComment comment = new ProductComment();
        comment.setProductId(request.getProductId());
        comment.setUserId(userId);
        comment.setContent(request.getContent().trim());
        comment.setLikeCount(0);
        comment.setStatus(1);

        if (request.getParentId() != null) {
            // 回复逻辑
            ProductComment parentComment = commentMapper.selectById(request.getParentId());
            if (parentComment == null || parentComment.getStatus() != 1) {
                throw new BusinessException("回复的评论不存在或已删除");
            }
            if (!parentComment.getProductId().equals(request.getProductId())) {
                throw new BusinessException("回复的评论不属于该商品");
            }
            // 只支持二级回复：父评论必须是顶级评论
            if (parentComment.getParentId() != null) {
                throw new BusinessException("不支持多级嵌套回复，请直接回复顶级评论");
            }
            comment.setParentId(request.getParentId());
            comment.setReplyToUserId(request.getReplyToUserId());
        }

        commentMapper.insert(comment);
        return comment.getId();
    }

    @Override
    @Transactional
    public void deleteComment(Long userId, boolean isAdmin, Long commentId) {
        ProductComment comment = commentMapper.selectById(commentId);
        if (comment == null || comment.getStatus() != 1) {
            throw new BusinessException("评论不存在或已删除");
        }
        if (!comment.getUserId().equals(userId) && !isAdmin) {
            throw new BusinessException("无权删除此评论");
        }

        // 逻辑删除
        comment.setStatus(0);
        commentMapper.updateById(comment);

        // 如果是顶级评论，同时逻辑删除其下所有回复
        if (comment.getParentId() == null) {
            LambdaUpdateWrapper<ProductComment> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(ProductComment::getParentId, commentId)
                    .eq(ProductComment::getStatus, 1)
                    .set(ProductComment::getStatus, 0);
            commentMapper.update(null, updateWrapper);
        }

        // 清理点赞记录
        LambdaQueryWrapper<CommentLike> likeWrapper = new LambdaQueryWrapper<>();
        likeWrapper.eq(CommentLike::getCommentId, commentId);
        likeMapper.delete(likeWrapper);

        // 如果是顶级评论，也清理回复的点赞记录
        if (comment.getParentId() == null) {
            // 先查出所有回复的ID
            LambdaQueryWrapper<ProductComment> replyWrapper = new LambdaQueryWrapper<>();
            replyWrapper.eq(ProductComment::getParentId, commentId)
                    .select(ProductComment::getId);
            List<ProductComment> replies = commentMapper.selectList(replyWrapper);
            if (!replies.isEmpty()) {
                List<Long> replyIds = replies.stream().map(ProductComment::getId).collect(Collectors.toList());
                LambdaQueryWrapper<CommentLike> replyLikeWrapper = new LambdaQueryWrapper<>();
                replyLikeWrapper.in(CommentLike::getCommentId, replyIds);
                likeMapper.delete(replyLikeWrapper);
            }
        }
    }

    @Override
    @Transactional
    public CommentLikeVO toggleLike(Long userId, Long commentId) {
        ProductComment comment = commentMapper.selectById(commentId);
        if (comment == null || comment.getStatus() != 1) {
            throw new BusinessException("评论不存在或已删除");
        }

        LambdaQueryWrapper<CommentLike> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CommentLike::getCommentId, commentId)
                .eq(CommentLike::getUserId, userId);
        CommentLike existingLike = likeMapper.selectOne(wrapper);

        CommentLikeVO vo = new CommentLikeVO();

        if (existingLike != null) {
            // 取消点赞
            likeMapper.deleteById(existingLike.getId());
            comment.setLikeCount(Math.max(0, comment.getLikeCount() - 1));
            commentMapper.updateById(comment);
            vo.setLiked(false);
        } else {
            // 点赞
            CommentLike like = new CommentLike();
            like.setCommentId(commentId);
            like.setUserId(userId);
            likeMapper.insert(like);
            comment.setLikeCount(comment.getLikeCount() + 1);
            commentMapper.updateById(comment);
            vo.setLiked(true);
        }

        vo.setLikeCount(comment.getLikeCount());
        return vo;
    }

    /**
     * 将分页的ProductComment转换为CommentVO，填充用户信息和点赞状态
     */
    private Page<CommentVO> convertPage(Page<ProductComment> result, Long currentUserId) {
        List<ProductComment> records = result.getRecords();
        if (records.isEmpty()) {
            Page<CommentVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
            voPage.setRecords(Collections.emptyList());
            return voPage;
        }

        // 批量查询用户信息
        Set<Long> userIds = new HashSet<>();
        Set<Long> replyToUserIds = new HashSet<>();
        for (ProductComment c : records) {
            userIds.add(c.getUserId());
            if (c.getReplyToUserId() != null) {
                replyToUserIds.add(c.getReplyToUserId());
            }
        }
        userIds.addAll(replyToUserIds);

        Map<Long, User> userMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            List<User> users = userMapper.selectBatchIds(userIds);
            for (User u : users) {
                userMap.put(u.getId(), u);
            }
        }

        // 查询当前用户已点赞的评论
        Set<Long> likedCommentIds = new HashSet<>();
        if (currentUserId != null) {
            List<Long> commentIds = records.stream().map(ProductComment::getId).collect(Collectors.toList());
            if (!commentIds.isEmpty()) {
                LambdaQueryWrapper<CommentLike> likeWrapper = new LambdaQueryWrapper<>();
                likeWrapper.in(CommentLike::getCommentId, commentIds)
                        .eq(CommentLike::getUserId, currentUserId);
                List<CommentLike> likes = likeMapper.selectList(likeWrapper);
                for (CommentLike like : likes) {
                    likedCommentIds.add(like.getCommentId());
                }
            }
        }

        // 查询每条评论的回复数（只对顶级评论查）
        Map<Long, Integer> replyCountMap = new HashMap<>();
        List<Long> topLevelIds = records.stream()
                .filter(c -> c.getParentId() == null)
                .map(ProductComment::getId)
                .collect(Collectors.toList());
        if (!topLevelIds.isEmpty()) {
            // 批量查询回复数
            for (Long topId : topLevelIds) {
                LambdaQueryWrapper<ProductComment> replyWrapper = new LambdaQueryWrapper<>();
                replyWrapper.eq(ProductComment::getParentId, topId)
                        .eq(ProductComment::getStatus, 1);
                Long count = commentMapper.selectCount(replyWrapper);
                replyCountMap.put(topId, count.intValue());
            }
        }

        // 转换
        List<CommentVO> voList = records.stream().map(c -> {
            CommentVO vo = new CommentVO();
            BeanUtils.copyProperties(c, vo);

            User user = userMap.get(c.getUserId());
            if (user != null) {
                vo.setUserName(user.getName());
                vo.setUserAvatar(user.getAvatar());
            }

            if (c.getReplyToUserId() != null) {
                User replyToUser = userMap.get(c.getReplyToUserId());
                if (replyToUser != null) {
                    vo.setReplyToUserName(replyToUser.getName());
                }
            }

            vo.setLiked(likedCommentIds.contains(c.getId()));
            vo.setReplyCount(replyCountMap.getOrDefault(c.getId(), 0));

            return vo;
        }).collect(Collectors.toList());

        Page<CommentVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        voPage.setRecords(voList);
        return voPage;
    }
}
