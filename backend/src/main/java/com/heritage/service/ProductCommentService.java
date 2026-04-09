package com.heritage.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.heritage.dto.CommentCreateRequest;
import com.heritage.dto.CommentLikeVO;
import com.heritage.dto.CommentVO;
import com.heritage.entity.ProductComment;

public interface ProductCommentService extends IService<ProductComment> {

    Page<CommentVO> listProductComments(Long productId, Long currentUserId, int page, int size);

    Page<CommentVO> listCommentReplies(Long commentId, Long currentUserId, int page, int size);

    Long createComment(Long userId, CommentCreateRequest request);

    void deleteComment(Long userId, boolean isAdmin, Long commentId);

    CommentLikeVO toggleLike(Long userId, Long commentId);
}
