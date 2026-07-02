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
@TableName("doctor_audit_log")
public class DoctorAuditLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long doctorId;

    private Long auditUserId;

    private Integer auditStatus;

    private String auditComment;

    @TableLogic
    private Integer deleted;

    private LocalDateTime createTime;
}
