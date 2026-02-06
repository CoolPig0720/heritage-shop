package com.heritage.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("heritage_project_media")
public class HeritageProjectMedia {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("project_id")
    private Long projectId;

    @TableField("media_type")
    private String mediaType;

    @TableField("media_url")
    private String mediaUrl;

    @TableField("sort_order")
    private Integer sortOrder;
}

