package com.heritage.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("certificate")
public class Certificate {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("certificate_number")
    private String certificateNumber;

    @TableField("name")
    private String name;

    @TableField("role")
    private String role;
}
