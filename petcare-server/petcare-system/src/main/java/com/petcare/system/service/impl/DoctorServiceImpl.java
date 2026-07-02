package com.petcare.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.petcare.common.BusinessException;
import com.petcare.security.util.JwtUtils;
import com.petcare.system.converter.UserConverter;
import com.petcare.system.dto.DoctorAuditDTO;
import com.petcare.system.dto.DoctorDetailDTO;
import com.petcare.system.dto.DoctorEvaluationDTO;
import com.petcare.system.dto.DoctorListItemDTO;
import com.petcare.system.dto.DoctorPendingDTO;
import com.petcare.system.dto.DoctorRegisterDTO;
import com.petcare.system.dto.DoctorScheduleDTO;
import com.petcare.system.dto.LoginResultDTO;
import com.petcare.system.dto.UserLoginDTO;
import com.petcare.system.entity.Doctor;
import com.petcare.system.entity.DoctorSchedule;
import com.petcare.system.entity.Evaluation;
import com.petcare.system.entity.User;
import com.petcare.system.entity.DoctorAuditLog;
import com.petcare.system.enums.DoctorStatus;
import com.petcare.system.enums.UserStatus;
import com.petcare.system.enums.UserType;
import com.petcare.system.mapper.DoctorMapper;
import com.petcare.system.mapper.DoctorScheduleMapper;
import com.petcare.system.mapper.DoctorAuditLogMapper;
import com.petcare.system.mapper.EvaluationMapper;
import com.petcare.system.mapper.UserMapper;
import com.petcare.system.service.DoctorService;
import com.petcare.system.service.LoginAttemptService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DoctorServiceImpl implements DoctorService {

    private final DoctorMapper doctorMapper;
    private final UserMapper userMapper;
    private final DoctorScheduleMapper scheduleMapper;
    private final EvaluationMapper evaluationMapper;
    private final DoctorAuditLogMapper auditLogMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final LoginAttemptService loginAttemptService;

    @Override
    public LoginResultDTO login(UserLoginDTO dto) {
        // 检查是否已被锁定（暴力破解防护）
        if (loginAttemptService.isLocked(dto.getPhone())) {
            long remaining = loginAttemptService.getRemainingLockSeconds(dto.getPhone());
            throw BusinessException.badRequest(
                    "登录失败次数过多，请" + remaining + "秒后再试");
        }

        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getPhone, dto.getPhone())
                .eq(User::getUserType, UserType.DOCTOR.getCode()));

        // 统一使用"手机号或密码错误"防止账号枚举攻击
        if (user == null || !passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            loginAttemptService.recordFailedAttempt(dto.getPhone());
            throw BusinessException.badRequest("手机号或密码错误");
        }

        // 禁用账号也记录失败尝试
        if (user.getStatus() != UserStatus.ENABLED.getCode()) {
            loginAttemptService.recordFailedAttempt(dto.getPhone());
            throw BusinessException.badRequest("手机号或密码错误");
        }

        Doctor doctor = doctorMapper.selectOne(new LambdaQueryWrapper<Doctor>()
                .eq(Doctor::getUserId, user.getId()));

        // 医生状态异常统一返回认证失败，不泄露具体状态
        if (doctor == null
                || doctor.getStatus() == DoctorStatus.PENDING.getCode()
                || doctor.getStatus() == DoctorStatus.REJECTED.getCode()
                || doctor.getStatus() == DoctorStatus.DISABLED.getCode()) {
            loginAttemptService.recordFailedAttempt(dto.getPhone());
            throw BusinessException.badRequest("手机号或密码错误");
        }

        // 登录成功，清除失败记录
        loginAttemptService.clearAttempts(dto.getPhone());

        String token = jwtUtils.generateToken(user.getId(), user.getUserType());

        LoginResultDTO result = new LoginResultDTO();
        result.setToken(token);
        result.setUser(UserConverter.INSTANCE.toUserInfoDTO(user));

        log.info("医生登录成功: userId={}, phone={}", user.getId(), user.getPhone());
        return result;
    }

    @Override
    @Transactional
    public void register(DoctorRegisterDTO dto) {
        User existing = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getPhone, dto.getPhone()));

        if (existing != null) {
            throw BusinessException.badRequest("手机号已注册");
        }

        User user = new User();
        user.setPhone(dto.getPhone());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setNickname("医生" + dto.getRealName());
        user.setRealName(dto.getRealName());
        user.setUserType(UserType.DOCTOR.getCode());
        user.setStatus(UserStatus.ENABLED.getCode());
        userMapper.insert(user);

        Doctor doctor = new Doctor();
        doctor.setUserId(user.getId());
        doctor.setDepartmentId(dto.getDepartmentId());
        doctor.setTitle(dto.getTitle());
        doctor.setSpecialty(dto.getSpecialty());
        doctor.setExperience(dto.getExperience());
        doctor.setEducation(dto.getEducation());
        doctor.setHospital(dto.getHospital());
        doctor.setIntroduction(dto.getIntroduction());
        doctor.setStatus(DoctorStatus.PENDING.getCode());
        doctorMapper.insert(doctor);

        log.info("医生注册成功: userId={}, phone={}", user.getId(), user.getPhone());
    }

    @Override
    public Page<DoctorListItemDTO> getDoctorList(Long deptId, String keyword, int page, int size) {
        Page<DoctorListItemDTO> pageParam = new Page<>(page, size);
        return doctorMapper.selectDoctorPage(pageParam, deptId, keyword);
    }

    @Override
    public DoctorDetailDTO getDoctorDetail(Long doctorId) {
        DoctorDetailDTO detail = doctorMapper.selectDoctorDetail(doctorId);
        if (detail == null) {
            throw BusinessException.notFound("医生不存在或已禁用");
        }

        // 查询最近5条评价
        List<Evaluation> evaluations = evaluationMapper.selectList(
                new LambdaQueryWrapper<Evaluation>()
                        .eq(Evaluation::getDoctorId, doctorId)
                        .eq(Evaluation::getStatus, 1)
                        .orderByDesc(Evaluation::getCreateTime)
                        .last("LIMIT 5"));

        // 批量查询评价关联的用户信息，避免N+1问题
        List<DoctorEvaluationDTO> evalDTOs = batchToEvaluationDTOs(evaluations);
        detail.setRecentEvaluations(evalDTOs);

        return detail;
    }

    @Override
    public Page<DoctorScheduleDTO> getDoctorSchedules(Long doctorId, int page, int size) {
        // 验证医生存在且已启用
        Doctor doctor = doctorMapper.selectOne(new LambdaQueryWrapper<Doctor>()
                .eq(Doctor::getId, doctorId)
                .eq(Doctor::getStatus, DoctorStatus.ENABLED.getCode()));
        if (doctor == null) {
            throw BusinessException.notFound("医生不存在或已禁用");
        }

        Page<DoctorSchedule> pageParam = new Page<>(page, size);
        Page<DoctorSchedule> schedulePage = scheduleMapper.selectPage(pageParam,
                new LambdaQueryWrapper<DoctorSchedule>()
                        .eq(DoctorSchedule::getDoctorId, doctorId)
                        .orderByAsc(DoctorSchedule::getScheduleDate)
                        .orderByAsc(DoctorSchedule::getStartTime));

        List<DoctorScheduleDTO> records = schedulePage.getRecords().stream()
                .map(this::toScheduleDTO)
                .collect(Collectors.toList());

        Page<DoctorScheduleDTO> result = new Page<>(page, size);
        result.setTotal(schedulePage.getTotal());
        result.setRecords(records);
        result.setPages(schedulePage.getPages());
        result.setCurrent(schedulePage.getCurrent());
        return result;
    }

    @Override
    public Page<DoctorEvaluationDTO> getDoctorEvaluations(Long doctorId, int page, int size) {
        Page<Evaluation> pageParam = new Page<>(page, size);
        Page<Evaluation> evalPage = evaluationMapper.selectPage(pageParam,
                new LambdaQueryWrapper<Evaluation>()
                        .eq(Evaluation::getDoctorId, doctorId)
                        .eq(Evaluation::getStatus, 1)
                        .orderByDesc(Evaluation::getCreateTime));

        // 批量查询用户信息，避免N+1问题
        List<DoctorEvaluationDTO> records = batchToEvaluationDTOs(evalPage.getRecords());

        Page<DoctorEvaluationDTO> result = new Page<>(page, size);
        result.setTotal(evalPage.getTotal());
        result.setRecords(records);
        result.setPages(evalPage.getPages());
        result.setCurrent(evalPage.getCurrent());
        return result;
    }

    @Override
    public Page<DoctorPendingDTO> getPendingList(int page, int size) {
        Page<DoctorPendingDTO> pageParam = new Page<>(page, size);
        return doctorMapper.selectPendingPage(pageParam);
    }

    @Override
    @Transactional
    public void audit(Long doctorId, DoctorAuditDTO dto, Long auditorId) {
        Doctor doctor = doctorMapper.selectById(doctorId);
        if (doctor == null) {
            throw BusinessException.notFound("医生不存在");
        }

        if (doctor.getStatus() != DoctorStatus.PENDING.getCode()) {
            throw BusinessException.badRequest("该医生不在待审核状态");
        }

        if (dto.getAuditStatus() != 1 && dto.getAuditStatus() != 2) {
            throw BusinessException.badRequest("审核状态无效：1-通过 2-拒绝");
        }

        // 更新医生状态
        if (dto.getAuditStatus() == 1) {
            doctor.setStatus(DoctorStatus.ENABLED.getCode());
        } else {
            doctor.setStatus(DoctorStatus.REJECTED.getCode());
        }
        doctorMapper.updateById(doctor);

        // 记录审核日志
        DoctorAuditLog auditLog = new DoctorAuditLog();
        auditLog.setDoctorId(doctorId);
        auditLog.setAuditUserId(auditorId);
        auditLog.setAuditStatus(dto.getAuditStatus());
        auditLog.setAuditComment(dto.getAuditComment());
        auditLogMapper.insert(auditLog);

        log.info("医生审核完成: doctorId={}, status={}, auditorId={}",
                doctorId, dto.getAuditStatus(), auditorId);
    }

    private DoctorScheduleDTO toScheduleDTO(DoctorSchedule s) {
        DoctorScheduleDTO dto = new DoctorScheduleDTO();
        dto.setId(s.getId());
        dto.setDoctorId(s.getDoctorId());
        dto.setScheduleDate(s.getScheduleDate());
        dto.setStartTime(s.getStartTime());
        dto.setEndTime(s.getEndTime());
        dto.setStatus(s.getStatus());
        dto.setStatusName(toScheduleStatusName(s.getStatus()));
        return dto;
    }

    /**
     * 批量将评价实体转换为DTO，一次性查询所有关联用户避免N+1问题
     */
    private List<DoctorEvaluationDTO> batchToEvaluationDTOs(List<Evaluation> evaluations) {
        if (evaluations.isEmpty()) {
            return Collections.emptyList();
        }

        // 批量查询所有评价关联的用户
        List<Long> userIds = evaluations.stream()
                .map(Evaluation::getUserId)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, User> userMap = userIds.isEmpty()
                ? Collections.emptyMap()
                : userMapper.selectBatchIds(userIds).stream()
                        .collect(Collectors.toMap(User::getId, u -> u, (a, b) -> a));

        // 批量转换
        return evaluations.stream().map(e -> {
            DoctorEvaluationDTO dto = new DoctorEvaluationDTO();
            dto.setId(e.getId());
            dto.setUserId(e.getUserId());
            dto.setRating(e.getRating());
            dto.setContent(e.getContent());
            dto.setIsAnonymous(e.getIsAnonymous());
            dto.setReply(e.getReply());
            dto.setReplyTime(e.getReplyTime());
            dto.setCreateTime(e.getCreateTime());

            // 从批量加载的用户Map中获取用户名和头像
            boolean anonymous = e.getIsAnonymous() != null && e.getIsAnonymous() == 1;
            if (e.getUserId() != null && userMap.containsKey(e.getUserId())) {
                User user = userMap.get(e.getUserId());
                dto.setUserName(anonymous ? "匿名用户" : user.getNickname());
                dto.setUserAvatar(anonymous ? null : user.getAvatar());
            } else {
                dto.setUserName(anonymous ? "匿名用户" : "用户" + e.getUserId());
            }
            return dto;
        }).collect(Collectors.toList());
    }

    private String toScheduleStatusName(Integer status) {
        if (status == null) return "未知";
        return switch (status) {
            case 0 -> "休息";
            case 1 -> "出诊";
            case 2 -> "已约满";
            default -> "未知";
        };
    }
}
