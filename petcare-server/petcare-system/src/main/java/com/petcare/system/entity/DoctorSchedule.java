package com.petcare.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
@TableName("doctor_schedule")
public class DoctorSchedule {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long doctorId;

    private LocalDate scheduleDate;

    private LocalTime startTime;

    private LocalTime endTime;

    private Integer status;

    @TableLogic
    private Integer deleted;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
