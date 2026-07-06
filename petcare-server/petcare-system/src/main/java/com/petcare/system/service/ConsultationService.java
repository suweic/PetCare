package com.petcare.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.petcare.system.entity.Consultation;

/**
 * 问诊服务接口。
 *
 * 管理问诊的创建、查询、取消和接诊操作。
 * 权限模型：宠物主可创建/取消自己的问诊；医生可查看/接诊分配给自己的问诊。
 */
public interface ConsultationService {

    /**
     * 创建问诊（仅普通用户）。
     *
     * @param userId        当前用户ID
     * @param petId         宠物ID
     * @param departmentId  科室ID
     * @param type          问诊类型
     * @param chiefComplaint 主诉
     * @param symptoms      症状描述
     * @return 创建的 Consultation 实体
     */
    Consultation create(Long userId, Long petId, Long departmentId, Integer type,
                        String chiefComplaint, String symptoms);

    /**
     * 分页查询用户的问诊列表。
     *
     * @param userId 用户ID
     * @param status 问诊状态筛选（可选，null 表示全部）
     * @param page   页码（从1开始）
     * @param size   每页条数
     * @return 分页结果
     */
    Page<Consultation> listByUser(Long userId, Integer status, int page, int size);

    /**
     * 分页查询医生的接诊列表。
     * <p>
     * 注意：此处的 doctorUserId 是 JWT 中的 userId（对应 user 表主键），
     * 方法内部会自动查询 doctor 表映射到 doctor.id 后筛选问诊记录。
     *
     * @param doctorUserId 医生对应的 user 表 ID（来自 JWT）
     * @param status       问诊状态筛选（可选）
     * @param page         页码
     * @param size         每页条数
     * @return 分页结果
     */
    Page<Consultation> listByDoctor(Long doctorUserId, Integer status, int page, int size);

    /**
     * 查询问诊详情（需校验访问权限）。
     * <p>
     * 宠物主和接诊医生均可查看问诊详情。
     * 如果当前用户既不是宠物主也不是接诊医生，抛出 403 异常。
     *
     * @param consultationId 问诊ID
     * @param userId         当前用户ID（可能是宠物主或医生）
     * @return Consultation 实体
     */
    Consultation getDetail(Long consultationId, Long userId);

    /**
     * 用户取消问诊。
     * <p>
     * 仅问诊创建者（宠物主）可取消，且仅允许在"待接单"或"进行中"状态下取消。
     *
     * @param consultationId 问诊ID
     * @param userId         当前用户ID
     */
    void cancel(Long consultationId, Long userId);
}
