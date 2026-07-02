package com.petcare.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.petcare.system.dto.DoctorDetailDTO;
import com.petcare.system.dto.DoctorListItemDTO;
import com.petcare.system.dto.DoctorPendingDTO;
import com.petcare.system.entity.Doctor;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface DoctorMapper extends BaseMapper<Doctor> {

    /**
     * 分页查询已启用的医生列表（联表user+department），支持科室筛选和关键词搜索
     */
    Page<DoctorListItemDTO> selectDoctorPage(Page<?> page,
                                              @Param("deptId") Long deptId,
                                              @Param("keyword") String keyword);

    /**
     * 查询医生详情（联表user+department），含最近5条评价
     */
    DoctorDetailDTO selectDoctorDetail(@Param("doctorId") Long doctorId);

    /**
     * 分页查询待审核的医生列表
     */
    Page<DoctorPendingDTO> selectPendingPage(Page<?> page);
}
