package com.petcare.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.petcare.system.dto.ConsultationMessageDTO;

public interface ConsultationMessageService {

    /**
     * 分页查询问诊消息历史。
     *
     * @param consultationId 问诊ID
     * @param userId         请求用户ID（用于权限校验）
     * @param page           页码
     * @param size           每页大小
     * @return 消息分页
     */
    Page<ConsultationMessageDTO> getMessages(Long consultationId, Long userId, int page, int size);
}
