package com.petcare.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.petcare.system.entity.DoctorAuditLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DoctorAuditLogMapper extends BaseMapper<DoctorAuditLog> {
}
