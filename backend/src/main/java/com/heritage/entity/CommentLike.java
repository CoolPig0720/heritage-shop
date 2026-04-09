package com.heritage.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("comment_like")
public class CommentLike {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("comment_id")
    private Long commentId;

    @TableField("user_id")
    private Long userId;

    @TableField("create_time")
    private LocalDateTime createTime;
}
