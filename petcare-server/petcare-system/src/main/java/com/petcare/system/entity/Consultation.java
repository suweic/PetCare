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
@TableName("consultation")
public class Consultation {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long doctorId;

    private Long petId;

    private Long departmentId;

    private Integer type;

    private Integer status;

    private String chiefComplaint;

    private String symptoms;

    private LocalDateTime scheduledTime;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    @TableLogic
    private Integer deleted;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
