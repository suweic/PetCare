package com.petcare.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.petcare.common.BusinessException;
import com.petcare.system.dto.PreConsultationRequestDTO;
import com.petcare.system.dto.PreConsultationResultDTO;
import com.petcare.system.entity.Department;
import com.petcare.system.entity.Doctor;
import com.petcare.system.entity.PreConsultation;
import com.petcare.system.entity.User;
import com.petcare.system.enums.DoctorStatus;
import com.petcare.system.mapper.DepartmentMapper;
import com.petcare.system.mapper.DoctorMapper;
import com.petcare.system.mapper.PreConsultationMapper;
import com.petcare.system.mapper.UserMapper;
import com.petcare.system.service.PreConsultationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PreConsultationServiceImpl implements PreConsultationService {

    private final DepartmentMapper departmentMapper;
    private final DoctorMapper doctorMapper;
    private final UserMapper userMapper;
    private final PreConsultationMapper preConsultationMapper;
    private final ObjectMapper objectMapper;
    private final LlmApiClient llmApiClient;

    private static final String SPECIES_NAMES = "1-猫, 2-狗, 3-其他";

    @Override
    @Transactional
    public PreConsultationResultDTO analyze(Long userId, PreConsultationRequestDTO dto) {
        // 1. 校验LLM配置（已移至 LlmApiClient，此处保留冗余校验作为快速失败）

        // 2. 查询科室和医生数据作为LLM上下文
        List<Department> departments = departmentMapper.selectList(
                new LambdaQueryWrapper<Department>()
                        .eq(Department::getStatus, 1)
                        .orderByAsc(Department::getSort));

        if (departments.isEmpty()) {
            throw BusinessException.badRequest("系统中暂无可用科室");
        }

        List<Doctor> doctors = doctorMapper.selectList(
                new LambdaQueryWrapper<Doctor>()
                        .eq(Doctor::getStatus, DoctorStatus.ENABLED.getCode()));

        // 3. 批量预加载医生关联的用户姓名，避免N+1
        Map<Long, String> doctorNameMap = loadDoctorNames(doctors);

        // 4. 构建LLM提示词
        String prompt = buildPrompt(dto, departments, doctors, doctorNameMap);

        // 5. 调用LLM API（通过独立 Service 触发 @Retryable + RestClientCustomizer 超时）
        String llmResponse;
        try {
            llmResponse = llmApiClient.call(prompt);
        } catch (Exception e) {
            log.error("LLM API调用失败", e);
            throw BusinessException.badRequest("AI分析服务暂时不可用，请稍后重试");
        }

        // 构建医生Map用于O(1)查找（避免N+1）
        Map<Long, Doctor> doctorMap = doctors.stream()
                .collect(Collectors.toMap(Doctor::getId, d -> d, (a, b) -> a));

        // 6. 解析LLM返回的JSON
        PreConsultationResultDTO result;
        try {
            result = parseLLMResponse(llmResponse, departments, doctorMap);
        } catch (Exception e) {
            log.error("LLM响应解析失败: {}", llmResponse, e);
            throw BusinessException.badRequest("AI分析结果解析失败，请重试");
        }

        // 7. 保存预问诊记录
        PreConsultation record = new PreConsultation();
        record.setUserId(userId);
        record.setPetId(dto.getPetId());
        record.setSpecies(dto.getSpecies());
        record.setBreed(dto.getBreed());
        record.setAgeYears(dto.getAgeYears());
        record.setAgeMonths(dto.getAgeMonths());
        record.setSymptoms(dto.getSymptoms());
        record.setSymptomDuration(dto.getSymptomDuration());
        record.setAdditionalInfo(dto.getAdditionalInfo());
        record.setAiAnalysis(result.getAiAnalysis());
        record.setRecommendedDepartmentId(result.getDepartmentId());
        record.setRecommendedDepartmentName(result.getDepartmentName());
        record.setGeneralAdvice(result.getGeneralAdvice());
        String llmModel = llmApiClient.getLlmModel();
        record.setLlmModel(llmModel);
        try {
            record.setRecommendedDoctors(objectMapper.writeValueAsString(result.getRecommendedDoctors()));
        } catch (JsonProcessingException e) {
            log.error("推荐医生列表序列化失败, preConsultationId={}", record.getId(), e);
            record.setRecommendedDoctors("[]");
        }
        preConsultationMapper.insert(record);

        result.setPreConsultationId(record.getId());
        result.setLlmModel(llmModel);

        log.info("AI预问诊完成: userId={}, dept={}, doctors={}",
                userId, result.getDepartmentName(),
                result.getRecommendedDoctors() != null ? result.getRecommendedDoctors().size() : 0);
        return result;
    }

    // ===== 私有方法 =====

    /**
     * 批量加载医生姓名
     */
    private Map<Long, String> loadDoctorNames(List<Doctor> doctors) {
        List<Long> userIds = doctors.stream()
                .map(Doctor::getUserId)
                .distinct()
                .collect(Collectors.toList());
        if (userIds.isEmpty()) return Map.of();
        return userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u.getRealName() != null ? u.getRealName() : u.getNickname(), (a, b) -> a));
    }

    /**
     * 构建发送给LLM的提示词
     */
    private String buildPrompt(PreConsultationRequestDTO dto,
                               List<Department> departments,
                               List<Doctor> doctors,
                               Map<Long, String> doctorNameMap) {
        StringBuilder sb = new StringBuilder();
        sb.append("你是一位资深的宠物医疗分诊助手。请根据用户描述的宠物症状，分析并推荐最合适的科室和医生。\n\n");

        // 用户输入
        sb.append("## 用户输入\n");
        if (dto.getSpecies() != null) {
            String speciesName = switch (dto.getSpecies()) {
                case 1 -> "猫";
                case 2 -> "狗";
                default -> "其他";
            };
            sb.append("- 宠物物种: ").append(speciesName).append("\n");
        }
        if (dto.getBreed() != null && !dto.getBreed().isBlank()) {
            sb.append("- 品种: ").append(dto.getBreed()).append("\n");
        }
        if (dto.getAgeYears() != null || dto.getAgeMonths() != null) {
            sb.append("- 年龄: ");
            if (dto.getAgeYears() != null && dto.getAgeYears() > 0) sb.append(dto.getAgeYears()).append("岁");
            if (dto.getAgeMonths() != null && dto.getAgeMonths() > 0) sb.append(dto.getAgeMonths()).append("个月");
            sb.append("\n");
        }
        sb.append("- 症状描述: ").append(dto.getSymptoms()).append("\n");
        if (dto.getSymptomDuration() != null && !dto.getSymptomDuration().isBlank()) {
            sb.append("- 持续时间: ").append(dto.getSymptomDuration()).append("\n");
        }
        if (dto.getAdditionalInfo() != null && !dto.getAdditionalInfo().isBlank()) {
            sb.append("- 补充信息: ").append(dto.getAdditionalInfo()).append("\n");
        }
        sb.append("\n");

        // 科室列表
        sb.append("## 可用科室\n");
        for (Department dept : departments) {
            sb.append("- ID=").append(dept.getId())
                    .append(" 名称=").append(dept.getName());
            if (dept.getDescription() != null && !dept.getDescription().isBlank()) {
                sb.append(" 描述=").append(dept.getDescription());
            }
            sb.append("\n");
        }
        sb.append("\n");

        // 医生列表
        sb.append("## 可用医生\n");
        for (Doctor doc : doctors) {
            String name = doctorNameMap.getOrDefault(doc.getUserId(), "未知");
            String deptName = departments.stream()
                    .filter(d -> d.getId().equals(doc.getDepartmentId()))
                    .findFirst().map(Department::getName).orElse("未分配");
            sb.append("- ID=").append(doc.getId())
                    .append(" 姓名=").append(name)
                    .append(" 科室=").append(deptName)
                    .append(" 职称=").append(doc.getTitle() != null ? doc.getTitle() : "未填");
            if (doc.getSpecialty() != null && !doc.getSpecialty().isBlank()) {
                sb.append(" 专长=").append(doc.getSpecialty());
            }
            if (doc.getHospital() != null && !doc.getHospital().isBlank()) {
                sb.append(" 医院=").append(doc.getHospital());
            }
            sb.append(" 评分=").append(doc.getRating())
                    .append(" 问诊次数=").append(doc.getConsultationCount());
            if (doc.getConsultationFee() != null) {
                sb.append(" 费用=").append(doc.getConsultationFee()).append("元");
            }
            sb.append("\n");
        }
        sb.append("\n");

        // 指令
        sb.append("## 任务要求\n");
        sb.append("请根据症状分析，严格按以下JSON格式返回结果（不要包含markdown代码块标记）：\n\n");
        sb.append("{\n");
        sb.append("  \"departmentId\": 数字(推荐科室ID),\n");
        sb.append("  \"departmentName\": \"科室名称\",\n");
        sb.append("  \"aiAnalysis\": \"对症状的简要分析（100字以内），包括可能的病因和严重程度评估\",\n");
        sb.append("  \"recommendedDoctors\": [\n");
        sb.append("    {\n");
        sb.append("      \"doctorId\": 数字,\n");
        sb.append("      \"doctorName\": \"姓名\",\n");
        sb.append("      \"matchReason\": \"推荐理由（20字以内，说明为什么该医生适合此症状）\"\n");
        sb.append("    }\n");
        sb.append("  ],\n");
        sb.append("  \"generalAdvice\": \"给宠物主人的通用护理建议（50字以内，在就医前的注意事项）\"\n");
        sb.append("}\n\n");
        sb.append("重要规则：\n");
        sb.append("1. 推荐1-3名最匹配的医生\n");
        sb.append("2. 优先推荐对应科室的医生，如该科室无医生可从相关科室推荐\n");
        sb.append("3. 评分高、问诊次数多的医生优先\n");
        sb.append("4. 分析中应体现专业性，同时给出合理的紧急程度判断\n");
        sb.append("5. 如果是急症（如中毒、严重外伤、呼吸困难），应在分析和建议中明确标注【建议立即就医】\n");
        sb.append("6. 只返回JSON，不要包含```json```标记或任何其他文字");

        return sb.toString();
    }

    /**
     * 解析LLM返回的JSON响应，填充医生详细信息。
     *
     * @param llmResponse LLM原始响应
     * @param departments 科室列表（用于填充科室描述）
     * @param doctorMap   医生ID→实体映射（避免N+1查询）
     */
    private PreConsultationResultDTO parseLLMResponse(String llmResponse, List<Department> departments,
                                                       Map<Long, Doctor> doctorMap) {
        try {
            // 提取LLM返回内容中的JSON
            JsonNode root = objectMapper.readTree(llmResponse);

            // OpenAI格式: {"choices":[{"message":{"content":"..."}}]}
            String content;
            if (root.has("choices") && root.get("choices").isArray() && root.get("choices").size() > 0) {
                content = root.get("choices").get(0).get("message").get("content").asText();
            } else if (root.has("content")) {
                // 兼容其他格式
                content = root.get("content").asText();
            } else {
                // 直接就是内容
                content = llmResponse;
            }

            // 清理可能的markdown代码块标记
            content = content.trim();
            if (content.startsWith("```json")) {
                content = content.substring(7);
            } else if (content.startsWith("```")) {
                content = content.substring(3);
            }
            if (content.endsWith("```")) {
                content = content.substring(0, content.length() - 3);
            }
            content = content.trim();

            // 解析JSON内容
            JsonNode resultNode = objectMapper.readTree(content);

            PreConsultationResultDTO result = new PreConsultationResultDTO();

            // 防御性解析：每个字段先检查是否存在
            if (!resultNode.has("departmentId") || !resultNode.has("departmentName")
                    || !resultNode.has("aiAnalysis")) {
                throw new RuntimeException("LLM返回缺少必要字段: departmentId/ departmentName/ aiAnalysis. 原始内容: " + content);
            }

            result.setDepartmentId(resultNode.get("departmentId").asLong());
            result.setDepartmentName(resultNode.get("departmentName").asText());
            result.setAiAnalysis(resultNode.get("aiAnalysis").asText());
            result.setGeneralAdvice(resultNode.has("generalAdvice") && !resultNode.get("generalAdvice").isNull()
                    ? resultNode.get("generalAdvice").asText() : null);

            // 填充科室描述
            departments.stream()
                    .filter(d -> d.getId().equals(result.getDepartmentId()))
                    .findFirst()
                    .ifPresent(d -> result.setDepartmentDescription(d.getDescription()));

            // 解析推荐医生列表，从预加载的doctorMap中O(1)查找（消除N+1）
            List<PreConsultationResultDTO.RecommendedDoctor> recommendedDoctors = new ArrayList<>();
            JsonNode doctorsNode = resultNode.get("recommendedDoctors");
            if (doctorsNode != null && doctorsNode.isArray()) {
                for (JsonNode docNode : doctorsNode) {
                    if (!docNode.has("doctorId") || !docNode.has("doctorName")) {
                        log.warn("LLM返回的推荐医生缺少必要字段，跳过: {}", docNode);
                        continue;
                    }

                    PreConsultationResultDTO.RecommendedDoctor rd = new PreConsultationResultDTO.RecommendedDoctor();
                    rd.setDoctorId(docNode.get("doctorId").asLong());
                    rd.setDoctorName(docNode.get("doctorName").asText());
                    rd.setMatchReason(docNode.has("matchReason") && !docNode.get("matchReason").isNull()
                            ? docNode.get("matchReason").asText() : null);

                    // 从预加载Map中O(1)获取医生完整信息
                    Doctor doctor = doctorMap.get(rd.getDoctorId());
                    if (doctor != null) {
                        rd.setTitle(doctor.getTitle());
                        rd.setHospital(doctor.getHospital());
                        rd.setSpecialty(doctor.getSpecialty());
                        rd.setRating(doctor.getRating());
                        rd.setConsultationFee(doctor.getConsultationFee());
                    }
                    recommendedDoctors.add(rd);
                }
            }
            result.setRecommendedDoctors(recommendedDoctors);

            return result;
        } catch (JsonProcessingException e) {
            throw new RuntimeException("LLM响应JSON解析失败", e);
        }
    }
}
