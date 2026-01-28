package com.heritage.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_user")
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("account")
    private String account;

    @TableField("password")
    @JsonIgnore
    private String password;

    @TableField("name")
    private String name;

    @TableField("role")
    private String role;

    @TableField("avatar")
    private String avatar;

    @TableField("certificate_number")
    private String certificateNumber;

    @TableField(value = "register_time", fill = FieldFill.INSERT)
    private LocalDateTime registerTime;
}
