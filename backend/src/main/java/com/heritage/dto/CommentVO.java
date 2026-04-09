package com.heritage.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommentVO {

    private Long id;

    private Long productId;

    private Long userId;

    private String userName;

    private String userAvatar;

    private Long parentId;

    private Long replyToUserId;

    private String replyToUserName;

    private String content;

    private Integer likeCount;

    private Boolean liked;

    private Integer replyCount;

    private LocalDateTime createTime;
}
