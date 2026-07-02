package com.petcare.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.petcare.common.BusinessException;
import com.petcare.system.converter.PetConverter;
import com.petcare.system.dto.PetCreateDTO;
import com.petcare.system.dto.PetDetailDTO;
import com.petcare.system.dto.PetHistoryDTO;
import com.petcare.system.dto.PetListItemDTO;
import com.petcare.system.dto.PetUpdateDTO;
import com.petcare.system.entity.Consultation;
import com.petcare.system.entity.Doctor;
import com.petcare.system.entity.Pet;
import com.petcare.system.entity.Department;
import com.petcare.system.enums.Gender;
import com.petcare.system.enums.Species;
import com.petcare.system.mapper.ConsultationMapper;
import com.petcare.system.mapper.DepartmentMapper;
import com.petcare.system.mapper.DoctorMapper;
import com.petcare.system.mapper.PetMapper;
import com.petcare.system.mapper.UserMapper;
import com.petcare.system.service.PetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 宠物服务实现。
 *
 * TODO: 添加单元测试 — 覆盖 create/list/getById/update/delete/getHistory 核心方法
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PetServiceImpl implements PetService {

    private final PetMapper petMapper;
    private final ConsultationMapper consultationMapper;
    private final DoctorMapper doctorMapper;
    private final DepartmentMapper departmentMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public PetDetailDTO create(Long userId, PetCreateDTO dto) {
        validateSpecies(dto.getSpecies());
        validateGender(dto.getGender());

        Pet pet = new Pet();
        pet.setUserId(userId);
        pet.setName(dto.getName());
        pet.setSpecies(dto.getSpecies());
        pet.setBreed(dto.getBreed());
        pet.setGender(dto.getGender());
        pet.setBirthday(dto.getBirthDate());
        pet.setWeight(dto.getWeight());
        pet.setSterilized(dto.getNeutered());
        pet.setAllergyInfo(dto.getAllergyInfo());

        petMapper.insert(pet);
        log.info("宠物档案创建成功: petId={}, userId={}, name={}", pet.getId(), userId, pet.getName());

        return PetConverter.INSTANCE.toDetailDTO(pet);
    }

    @Override
    public Page<PetListItemDTO> list(Long userId, int page, int size) {
        Page<Pet> pageParam = new Page<>(page, size);
        Page<Pet> petPage = petMapper.selectPage(pageParam,
                new LambdaQueryWrapper<Pet>()
                        .eq(Pet::getUserId, userId)
                        .orderByDesc(Pet::getCreateTime));

        List<PetListItemDTO> records = PetConverter.INSTANCE.toListItemDTOList(petPage.getRecords());

        Page<PetListItemDTO> result = new Page<>(page, size);
        result.setTotal(petPage.getTotal());
        result.setRecords(records);
        result.setPages(petPage.getPages());
        result.setCurrent(petPage.getCurrent());
        return result;
    }

    @Override
    public PetDetailDTO getById(Long userId, Long petId) {
        Pet pet = findPetAndCheckOwner(userId, petId);
        return PetConverter.INSTANCE.toDetailDTO(pet);
    }

    @Override
    @Transactional
    public PetDetailDTO update(Long userId, Long petId, PetUpdateDTO dto) {
        Pet pet = findPetAndCheckOwner(userId, petId);

        if (dto.getSpecies() != null) {
            validateSpecies(dto.getSpecies());
        }
        if (dto.getGender() != null) {
            validateGender(dto.getGender());
        }

        if (dto.getName() != null) {
            pet.setName(dto.getName());
        }
        if (dto.getSpecies() != null) {
            pet.setSpecies(dto.getSpecies());
        }
        if (dto.getBreed() != null) {
            pet.setBreed(dto.getBreed());
        }
        if (dto.getGender() != null) {
            pet.setGender(dto.getGender());
        }
        if (dto.getBirthDate() != null) {
            pet.setBirthday(dto.getBirthDate());
        }
        if (dto.getWeight() != null) {
            pet.setWeight(dto.getWeight());
        }
        if (dto.getNeutered() != null) {
            pet.setSterilized(dto.getNeutered());
        }
        if (dto.getAllergyInfo() != null) {
            pet.setAllergyInfo(dto.getAllergyInfo());
        }

        petMapper.updateById(pet);
        log.info("宠物档案更新成功: petId={}, userId={}", petId, userId);

        return PetConverter.INSTANCE.toDetailDTO(pet);
    }

    @Override
    @Transactional
    public void delete(Long userId, Long petId) {
        Pet pet = findPetAndCheckOwner(userId, petId);
        petMapper.deleteById(pet.getId());
        log.info("宠物档案逻辑删除成功: petId={}, userId={}", petId, userId);
    }

    @Override
    public Page<PetHistoryDTO> getHistory(Long userId, Long petId, int page, int size) {
        findPetAndCheckOwner(userId, petId);

        Page<Consultation> pageParam = new Page<>(page, size);
        Page<Consultation> consultationPage = consultationMapper.selectPage(pageParam,
                new LambdaQueryWrapper<Consultation>()
                        .eq(Consultation::getPetId, petId)
                        .eq(Consultation::getUserId, userId)
                        .orderByDesc(Consultation::getCreateTime));

        List<Consultation> consultations = consultationPage.getRecords();
        if (consultations.isEmpty()) {
            Page<PetHistoryDTO> empty = new Page<>(page, size);
            empty.setTotal(0);
            return empty;
        }

        // 批量查询医生和科室名称
        List<Long> doctorIds = consultations.stream()
                .map(Consultation::getDoctorId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());

        List<Long> departmentIds = consultations.stream()
                .map(Consultation::getDepartmentId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, String> deptNameMap = departmentIds.isEmpty()
                ? Collections.emptyMap()
                : departmentMapper.selectBatchIds(departmentIds).stream()
                        .collect(Collectors.toMap(Department::getId, Department::getName));

        // 对于有userId的医生，查user表获取真实姓名
        Map<Long, String> doctorRealNameMap = doctorIds.isEmpty()
                ? Collections.emptyMap()
                : getDoctorRealNames(doctorIds);

        List<PetHistoryDTO> records = consultations.stream().map(c -> {
            PetHistoryDTO dto = new PetHistoryDTO();
            dto.setConsultationId(c.getId());
            dto.setDoctorId(c.getDoctorId());
            dto.setDoctorName(doctorRealNameMap.getOrDefault(c.getDoctorId(), "未知"));
            dto.setDepartmentName(deptNameMap.getOrDefault(c.getDepartmentId(), "未知"));
            dto.setConsultationType(c.getType());
            dto.setStatus(c.getStatus());
            dto.setStatusName(toStatusName(c.getStatus()));
            dto.setChiefComplaint(c.getChiefComplaint());
            dto.setCreateTime(c.getCreateTime());
            dto.setEndTime(c.getEndTime());
            return dto;
        }).collect(Collectors.toList());

        Page<PetHistoryDTO> result = new Page<>(page, size);
        result.setTotal(consultationPage.getTotal());
        result.setRecords(records);
        result.setPages(consultationPage.getPages());
        result.setCurrent(consultationPage.getCurrent());
        return result;
    }

    private Map<Long, String> getDoctorRealNames(List<Long> doctorIds) {
        // 批量查doctor表获取user_id列表
        List<Doctor> doctors = doctorMapper.selectBatchIds(doctorIds);
        List<Long> userIds = doctors.stream()
                .map(Doctor::getUserId)
                .distinct()
                .collect(Collectors.toList());

        if (userIds.isEmpty()) {
            return Collections.emptyMap();
        }

        // 批量查user表获取真实姓名
        Map<Long, String> userNameMap = userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(
                        com.petcare.system.entity.User::getId,
                        u -> u.getRealName() != null ? u.getRealName() : u.getNickname(),
                        (existing, replacement) -> existing));

        // 构建 doctorId → realName 的映射
        return doctors.stream()
                .collect(Collectors.toMap(
                        Doctor::getId,
                        d -> userNameMap.getOrDefault(d.getUserId(), "未知"),
                        (existing, replacement) -> existing));
    }

    private Pet findPetAndCheckOwner(Long userId, Long petId) {
        Pet pet = petMapper.selectById(petId);
        if (pet == null) {
            throw BusinessException.notFound("宠物档案不存在");
        }
        if (pet.getUserId() == null || !pet.getUserId().equals(userId)) {
            throw BusinessException.forbidden("只能操作自己的宠物档案");
        }
        return pet;
    }

    private void validateSpecies(Integer species) {
        if (!Species.isValid(species)) {
            throw BusinessException.badRequest("物种无效，有效值：1-猫 2-狗 3-其他");
        }
    }

    private void validateGender(Integer gender) {
        if (gender != null && !Gender.isValid(gender)) {
            throw BusinessException.badRequest("性别无效，有效值：1-公 2-母");
        }
    }

    private String toStatusName(Integer status) {
        if (status == null) return "未知";
        return switch (status) {
            case 0 -> "待接单";
            case 1 -> "进行中";
            case 2 -> "已完成";
            case 3 -> "已取消";
            case 4 -> "已拒绝";
            case 5 -> "超时";
            default -> "未知";
        };
    }
}
