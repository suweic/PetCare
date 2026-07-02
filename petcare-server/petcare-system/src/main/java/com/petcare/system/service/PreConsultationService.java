package com.petcare.system.service;

import com.petcare.system.dto.PreConsultationRequestDTO;
import com.petcare.system.dto.PreConsultationResultDTO;

public interface PreConsultationService {

    /**
     * AI预问诊分析：根据用户描述的症状，调用LLM自动匹配推荐科室和医生
     *
     * @param userId 当前用户ID
     * @param dto    症状描述请求
     * @return AI分析结果（科室+医生推荐）
     */
    PreConsultationResultDTO analyze(Long userId, PreConsultationRequestDTO dto);
}
