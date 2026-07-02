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
@TableName("evaluation")
public class Evaluation {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long consultationId;

    private Long userId;

    private Long doctorId;

    private Integer rating;

    private String content;

    private Integer isAnonymous;

    private String reply;

    private LocalDateTime replyTime;

    private Integer status;

    @TableLogic
    private Integer deleted;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
