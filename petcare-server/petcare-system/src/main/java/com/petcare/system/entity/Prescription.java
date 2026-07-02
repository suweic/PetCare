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
@TableName("prescription")
public class Prescription {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long consultationId;

    private Long userId;

    private Long doctorId;

    private Long petId;

    private String diagnosis;

    private String advice;

    private Integer status;

    @TableLogic
    private Integer deleted;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
