package com.petcare.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@TableName("department")
public class Department {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String description;

    private String icon;

    private Integer sort;

    private Integer status;

    @TableLogic
    private Integer deleted;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}