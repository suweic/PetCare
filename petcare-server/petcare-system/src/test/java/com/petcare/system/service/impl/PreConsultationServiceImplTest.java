package com.petcare.system.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.petcare.common.BusinessException;
import com.petcare.system.dto.PreConsultationRequestDTO;
import com.petcare.system.dto.PreConsultationResultDTO;
import com.petcare.system.entity.Department;
import com.petcare.system.entity.Doctor;
import com.petcare.system.entity.PreConsultation;
import com.petcare.system.entity.User;
import com.petcare.system.mapper.DepartmentMapper;
import com.petcare.system.mapper.DoctorMapper;
import com.petcare.system.mapper.PreConsultationMapper;
import com.petcare.system.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * PreConsultationServiceImpl 单元测试。
 * 覆盖 analyze 方法的各分支：科室校验、LLM调用失败、响应解析、记录保存。
 * LLM 配置校验已移至 {@link LlmApiClient}，此处不再重复测试。
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PreConsultationServiceImplTest {

    @Mock
    private DepartmentMapper departmentMapper;
    @Mock
    private DoctorMapper doctorMapper;
    @Mock
    private UserMapper userMapper;
    @Mock
    private PreConsultationMapper preConsultationMapper;
    @Mock
    private LlmApiClient llmApiClient;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Spy
    @InjectMocks
    private PreConsultationServiceImpl service;

    @BeforeEach
    void setUp() {
        // LLM 配置已移至 LlmApiClient，测试中通过 mock 控制其行为
    }

    // ==================== 科室校验 ====================

    @Test
    void shouldThrowExceptionWhenNoDepartmentsAvailable() {
        when(departmentMapper.selectList(any())).thenReturn(Collections.emptyList());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.analyze(1L, createRequest("腹泻")));
        assertEquals("系统中暂无可用科室", ex.getMessage());
    }

    // ==================== LLM 调用失败处理 ====================

    @Test
    void shouldThrowExceptionWhenLlmCallFails() {
        Department dept = createDepartment(1L, "内科");
        when(departmentMapper.selectList(any())).thenReturn(List.of(dept));
        when(doctorMapper.selectList(any())).thenReturn(Collections.emptyList());
        when(userMapper.selectBatchIds(anyList())).thenReturn(Collections.emptyList());

        when(llmApiClient.call(anyString()))
                .thenThrow(new RuntimeException("Connection refused"));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.analyze(1L, createRequest("腹泻")));
        assertTrue(ex.getMessage().contains("AI分析服务暂时不可用"));
    }

    // ==================== LLM 响应解析失败 ====================

    @Test
    void shouldThrowExceptionWhenLlmResponseIsInvalidJson() {
        Department dept = createDepartment(1L, "内科");
        when(departmentMapper.selectList(any())).thenReturn(List.of(dept));
        when(doctorMapper.selectList(any())).thenReturn(Collections.emptyList());
        when(userMapper.selectBatchIds(anyList())).thenReturn(Collections.emptyList());

        when(llmApiClient.call(anyString()))
                .thenReturn("This is not JSON at all");

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.analyze(1L, createRequest("腹泻")));
        assertTrue(ex.getMessage().contains("AI分析结果解析失败"));
    }

    // ==================== 成功流程 ====================

    @Test
    void shouldAnalyzeSuccessfullyAndSaveRecord() throws Exception {
        Department dept = createDepartment(1L, "内科", "内科疾病诊断和治疗");
        when(departmentMapper.selectList(any())).thenReturn(List.of(dept));

        Doctor doctor = createDoctor(10L, 100L, 1L);
        when(doctorMapper.selectList(any())).thenReturn(List.of(doctor));

        User doctorUser = new User();
        doctorUser.setId(100L);
        doctorUser.setRealName("李医生");
        when(userMapper.selectBatchIds(List.of(100L))).thenReturn(List.of(doctorUser));

        when(llmApiClient.getLlmModel()).thenReturn("gpt-4o-mini");

        // 模拟 LLM 返回 OpenAI 格式的 JSON
        String llmResponse = buildMockOpenAiResponse(
                "{\"departmentId\":1,\"departmentName\":\"内科\",\"aiAnalysis\":\"症状分析内容\","
                        + "\"recommendedDoctors\":[{\"doctorId\":10,\"doctorName\":\"李医生\",\"matchReason\":\"专长匹配\"}],"
                        + "\"generalAdvice\":\"保持饮食清淡\"}");
        when(llmApiClient.call(anyString())).thenReturn(llmResponse);

        when(preConsultationMapper.insert(any(PreConsultation.class)))
                .thenAnswer(inv -> { inv.getArgument(0, PreConsultation.class).setId(1L); return 1; });

        PreConsultationResultDTO result = service.analyze(1L, createRequest("腹泻", "狗"));

        assertNotNull(result);
        assertEquals(1L, result.getPreConsultationId());
        assertEquals("内科", result.getDepartmentName());
        assertEquals("症状分析内容", result.getAiAnalysis());
        assertEquals("保持饮食清淡", result.getGeneralAdvice());
        assertEquals("gpt-4o-mini", result.getLlmModel());
        assertNotNull(result.getRecommendedDoctors());
        assertEquals(1, result.getRecommendedDoctors().size());
        assertEquals("李医生", result.getRecommendedDoctors().get(0).getDoctorName());
        assertEquals("专长匹配", result.getRecommendedDoctors().get(0).getMatchReason());

        verify(preConsultationMapper, times(1)).insert(any(PreConsultation.class));
    }

    @Test
    void shouldHandleLlmResponseWithMarkdownCodeBlock() throws Exception {
        Department dept = createDepartment(1L, "内科");
        when(departmentMapper.selectList(any())).thenReturn(List.of(dept));
        when(doctorMapper.selectList(any())).thenReturn(Collections.emptyList());
        when(userMapper.selectBatchIds(anyList())).thenReturn(Collections.emptyList());

        // LLM 返回带 ```json 标记的内容
        String rawJson = "{\"departmentId\":1,\"departmentName\":\"内科\",\"aiAnalysis\":\"分析内容\","
                + "\"recommendedDoctors\":[],\"generalAdvice\":\"护理建议\"}";
        String llmResponse = buildMockOpenAiResponse("```json\n" + rawJson + "\n```");
        when(llmApiClient.call(anyString())).thenReturn(llmResponse);

        when(preConsultationMapper.insert(any(PreConsultation.class)))
                .thenAnswer(inv -> { inv.getArgument(0, PreConsultation.class).setId(2L); return 1; });

        PreConsultationResultDTO result = service.analyze(1L, createRequest("喷嚏"));

        assertNotNull(result);
        assertEquals("内科", result.getDepartmentName());
        assertEquals("分析内容", result.getAiAnalysis());
    }

    @Test
    void shouldHandleLlmResponseWithoutChoicesWrapper() throws Exception {
        Department dept = createDepartment(2L, "外科");
        when(departmentMapper.selectList(any())).thenReturn(List.of(dept));
        when(doctorMapper.selectList(any())).thenReturn(Collections.emptyList());
        when(userMapper.selectBatchIds(anyList())).thenReturn(Collections.emptyList());

        // 直接返回 JSON 内容（非 OpenAI 格式）
        String rawJson = "{\"departmentId\":2,\"departmentName\":\"外科\",\"aiAnalysis\":\"需要手术\","
                + "\"recommendedDoctors\":[],\"generalAdvice\":null}";
        when(llmApiClient.call(anyString())).thenReturn(rawJson);

        when(preConsultationMapper.insert(any(PreConsultation.class)))
                .thenAnswer(inv -> { inv.getArgument(0, PreConsultation.class).setId(3L); return 1; });

        PreConsultationResultDTO result = service.analyze(1L, createRequest("骨折"));

        assertNotNull(result);
        assertEquals("外科", result.getDepartmentName());
    }

    @Test
    void shouldPopulateDoctorDetailsFromPreloadedMap() throws Exception {
        Department dept = createDepartment(1L, "内科");
        when(departmentMapper.selectList(any())).thenReturn(List.of(dept));

        Doctor doctor = createDoctor(20L, 200L, 1L);
        doctor.setTitle("主任医师");
        doctor.setHospital("上海宠物医院");
        doctor.setSpecialty("消化系统");
        doctor.setRating(new BigDecimal("4.9"));
        doctor.setConsultationFee(new BigDecimal("88.00"));
        when(doctorMapper.selectList(any())).thenReturn(List.of(doctor));

        User doctorUser = new User();
        doctorUser.setId(200L);
        doctorUser.setRealName("王医生");
        when(userMapper.selectBatchIds(List.of(200L))).thenReturn(List.of(doctorUser));

        String llmResponse = buildMockOpenAiResponse(
                "{\"departmentId\":1,\"departmentName\":\"内科\",\"aiAnalysis\":\"消化问题\","
                        + "\"recommendedDoctors\":[{\"doctorId\":20,\"doctorName\":\"王医生\",\"matchReason\":\"消化系统专家\"}],"
                        + "\"generalAdvice\":\"少量多餐\"}");
        when(llmApiClient.call(anyString())).thenReturn(llmResponse);

        when(preConsultationMapper.insert(any(PreConsultation.class)))
                .thenAnswer(inv -> { inv.getArgument(0, PreConsultation.class).setId(4L); return 1; });

        PreConsultationResultDTO result = service.analyze(1L, createRequest("呕吐"));

        assertNotNull(result);
        PreConsultationResultDTO.RecommendedDoctor rd = result.getRecommendedDoctors().get(0);
        assertEquals("主任医师", rd.getTitle());
        assertEquals("上海宠物医院", rd.getHospital());
        assertEquals("消化系统", rd.getSpecialty());
        assertEquals(new BigDecimal("4.9"), rd.getRating());
        assertEquals(new BigDecimal("88.00"), rd.getConsultationFee());
    }

    @Test
    void shouldFallbackToNicknameWhenRealNameIsNull() throws Exception {
        Department dept = createDepartment(1L, "内科");
        when(departmentMapper.selectList(any())).thenReturn(List.of(dept));

        Doctor doctor = createDoctor(30L, 300L, 1L);
        when(doctorMapper.selectList(any())).thenReturn(List.of(doctor));

        User doctorUser = new User();
        doctorUser.setId(300L);
        doctorUser.setRealName(null);
        doctorUser.setNickname("昵称医生");
        when(userMapper.selectBatchIds(List.of(300L))).thenReturn(List.of(doctorUser));

        String llmResponse = buildMockOpenAiResponse(
                "{\"departmentId\":1,\"departmentName\":\"内科\",\"aiAnalysis\":\"分析\","
                        + "\"recommendedDoctors\":[{\"doctorId\":30,\"doctorName\":\"昵称医生\",\"matchReason\":\"匹配\"}],"
                        + "\"generalAdvice\":\"建议\"}");
        when(llmApiClient.call(anyString())).thenReturn(llmResponse);

        when(preConsultationMapper.insert(any(PreConsultation.class)))
                .thenAnswer(inv -> { inv.getArgument(0, PreConsultation.class).setId(5L); return 1; });

        PreConsultationResultDTO result = service.analyze(1L, createRequest("咳嗽"));
        assertEquals("昵称医生", result.getRecommendedDoctors().get(0).getDoctorName());
    }

    // ==================== 入参组装测试 ====================

    @Test
    void shouldSavePreConsultationRecordWithAllFields() throws Exception {
        Department dept = createDepartment(1L, "内科");
        when(departmentMapper.selectList(any())).thenReturn(List.of(dept));
        when(doctorMapper.selectList(any())).thenReturn(Collections.emptyList());
        when(userMapper.selectBatchIds(anyList())).thenReturn(Collections.emptyList());

        String llmResponse = buildMockOpenAiResponse(
                "{\"departmentId\":1,\"departmentName\":\"内科\",\"aiAnalysis\":\"测试\","
                        + "\"recommendedDoctors\":[],\"generalAdvice\":\"建议\"}");
        when(llmApiClient.call(anyString())).thenReturn(llmResponse);

        when(preConsultationMapper.insert(any(PreConsultation.class)))
                .thenAnswer(inv -> { inv.getArgument(0, PreConsultation.class).setId(10L); return 1; });

        PreConsultationRequestDTO dto = new PreConsultationRequestDTO();
        dto.setPetId(5L);
        dto.setSpecies(2); // 狗
        dto.setBreed("金毛");
        dto.setAgeYears(3);
        dto.setAgeMonths(6);
        dto.setSymptoms("持续腹泻，食欲不振");
        dto.setSymptomDuration("3天");
        dto.setAdditionalInfo("近期换了狗粮品牌");

        service.analyze(1L, dto);

        verify(preConsultationMapper).insert(Mockito.<PreConsultation>argThat(record -> {
            assertEquals(1L, record.getUserId());
            assertEquals(5L, record.getPetId());
            assertEquals(Integer.valueOf(2), record.getSpecies());
            assertEquals("金毛", record.getBreed());
            assertEquals(Integer.valueOf(3), record.getAgeYears());
            assertEquals(Integer.valueOf(6), record.getAgeMonths());
            assertEquals("持续腹泻，食欲不振", record.getSymptoms());
            assertEquals("3天", record.getSymptomDuration());
            assertEquals("近期换了狗粮品牌", record.getAdditionalInfo());
            return true;
        }));
    }

    // ==================== Helpers ====================

    private PreConsultationRequestDTO createRequest(String symptoms) {
        PreConsultationRequestDTO dto = new PreConsultationRequestDTO();
        dto.setSymptoms(symptoms);
        return dto;
    }

    private PreConsultationRequestDTO createRequest(String symptoms, String speciesName) {
        PreConsultationRequestDTO dto = new PreConsultationRequestDTO();
        dto.setSymptoms(symptoms);
        dto.setSpecies("猫".equals(speciesName) ? 1 : "狗".equals(speciesName) ? 2 : 3);
        return dto;
    }

    private Department createDepartment(Long id, String name) {
        Department dept = new Department();
        dept.setId(id);
        dept.setName(name);
        dept.setStatus(1);
        dept.setSort(1);
        return dept;
    }

    private Department createDepartment(Long id, String name, String description) {
        Department dept = createDepartment(id, name);
        dept.setDescription(description);
        return dept;
    }

    private Doctor createDoctor(Long id, Long userId, Long departmentId) {
        Doctor doctor = new Doctor();
        doctor.setId(id);
        doctor.setUserId(userId);
        doctor.setDepartmentId(departmentId);
        doctor.setStatus(1);
        doctor.setRating(new BigDecimal("4.5"));
        doctor.setConsultationCount(10);
        return doctor;
    }

    /**
     * 构造模拟的 OpenAI 格式 LLM 响应。
     */
    private String buildMockOpenAiResponse(String innerJson) throws JsonProcessingException {
        Map<String, Object> message = Map.of("role", "assistant", "content", innerJson);
        Map<String, Object> choice = Map.of("index", 0, "message", message, "finish_reason", "stop");
        Map<String, Object> root = Map.of("choices", List.of(choice));
        return objectMapper.writeValueAsString(root);
    }
}
