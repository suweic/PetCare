package com.petcare.system.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class PreConsultationResultDTO {

    /** 预问诊记录ID */
    private Long preConsultationId;

    /** 推荐科室ID */
    private Long departmentId;

    /** 推荐科室名称 */
    private String departmentName;

    /** 推荐科室描述 */
    private String departmentDescription;

    /** AI对症状的分析总结 */
    private String aiAnalysis;

    /** 推荐的医生列表 */
    private List<RecommendedDoctor> recommendedDoctors;

    /** AI通用护理建议 */
    private String generalAdvice;

    /** 使用的LLM模型 */
    private String llmModel;

    // ===== 嵌套DTO =====

    @Getter
    @Setter
    public static class RecommendedDoctor {
        /** 医生ID */
        private Long doctorId;

        /** 医生姓名 */
        private String doctorName;

        /** 职称 */
        private String title;

        /** 所属医院 */
        private String hospital;

        /** 专长领域 */
        private String specialty;

        /** 评分 */
        private BigDecimal rating;

        /** 问诊费用 */
        private BigDecimal consultationFee;

        /** 推荐理由 */
        private String matchReason;
    }
}
