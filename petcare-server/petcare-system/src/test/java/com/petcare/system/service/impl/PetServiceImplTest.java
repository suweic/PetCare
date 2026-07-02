package com.petcare.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.petcare.common.BusinessException;
import com.petcare.system.dto.PetCreateDTO;
import com.petcare.system.dto.PetDetailDTO;
import com.petcare.system.dto.PetHistoryDTO;
import com.petcare.system.dto.PetListItemDTO;
import com.petcare.system.dto.PetUpdateDTO;
import com.petcare.system.entity.Consultation;
import com.petcare.system.entity.Department;
import com.petcare.system.entity.Doctor;
import com.petcare.system.entity.Pet;
import com.petcare.system.entity.User;
import com.petcare.system.enums.ConsultationStatus;
import com.petcare.system.mapper.ConsultationMapper;
import com.petcare.system.mapper.DepartmentMapper;
import com.petcare.system.mapper.DoctorMapper;
import com.petcare.system.mapper.PetMapper;
import com.petcare.system.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PetServiceImplTest {

    @Mock
    private PetMapper petMapper;
    @Mock
    private ConsultationMapper consultationMapper;
    @Mock
    private DoctorMapper doctorMapper;
    @Mock
    private DepartmentMapper departmentMapper;
    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private PetServiceImpl petService;

    private Pet pet;

    @BeforeEach
    void setUp() {
        pet = new Pet();
        pet.setId(1L);
        pet.setUserId(100L);
        pet.setName("旺财");
        pet.setSpecies(2); // 狗
        pet.setBreed("金毛");
        pet.setGender(1); // 公
        pet.setBirthday(LocalDate.of(2022, 6, 1));
    }

    // ===================== Create Tests =====================

    @Test
    void shouldCreatePetSuccessfully() {
        PetCreateDTO dto = new PetCreateDTO();
        dto.setName("旺财");
        dto.setSpecies(2);
        dto.setBreed("金毛");
        dto.setGender(1);
        dto.setBirthDate(LocalDate.of(2022, 6, 1));

        when(petMapper.insert(any(Pet.class))).thenAnswer(inv -> {
            Pet p = inv.getArgument(0);
            p.setId(1L);
            return 1;
        });

        PetDetailDTO result = petService.create(100L, dto);

        assertNotNull(result);
        assertEquals("旺财", result.getName());
        assertEquals(2, result.getSpecies());
        verify(petMapper, times(1)).insert(any(Pet.class));
    }

    @Test
    void shouldThrowExceptionWhenSpeciesInvalid() {
        PetCreateDTO dto = new PetCreateDTO();
        dto.setName("旺财");
        dto.setSpecies(99); // 无效物种
        dto.setGender(1);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> petService.create(100L, dto));
        assertTrue(ex.getMessage().contains("物种无效"));
        verify(petMapper, never()).insert(any());
    }

    @Test
    void shouldThrowExceptionWhenGenderInvalid() {
        PetCreateDTO dto = new PetCreateDTO();
        dto.setName("旺财");
        dto.setSpecies(2);
        dto.setGender(99); // 无效性别

        BusinessException ex = assertThrows(BusinessException.class,
                () -> petService.create(100L, dto));
        assertTrue(ex.getMessage().contains("性别无效"));
        verify(petMapper, never()).insert(any());
    }

    // ===================== List Tests =====================

    @Test
    void shouldReturnPetList() {
        Page<Pet> petPage = new Page<>(1, 10);
        petPage.setRecords(List.of(pet));
        petPage.setTotal(1);

        when(petMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(petPage);

        Page<PetListItemDTO> result = petService.list(100L, 1, 10);

        assertEquals(1, result.getTotal());
        assertEquals(1, result.getRecords().size());
    }

    // ===================== GetById Tests =====================

    @Test
    void shouldGetPetById() {
        when(petMapper.selectById(1L)).thenReturn(pet);

        PetDetailDTO result = petService.getById(100L, 1L);

        assertNotNull(result);
        assertEquals("旺财", result.getName());
        assertEquals(2, result.getSpecies());
    }

    @Test
    void shouldThrowExceptionWhenPetNotFound() {
        when(petMapper.selectById(999L)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> petService.getById(100L, 999L));
        assertEquals("宠物档案不存在", ex.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenNotOwner() {
        pet.setUserId(200L); // 属于用户200
        when(petMapper.selectById(1L)).thenReturn(pet);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> petService.getById(100L, 1L));
        assertEquals("只能操作自己的宠物档案", ex.getMessage());
    }

    // ===================== Update Tests =====================

    @Test
    void shouldUpdatePetSuccessfully() {
        when(petMapper.selectById(1L)).thenReturn(pet);
        when(petMapper.updateById(any(Pet.class))).thenReturn(1);

        PetUpdateDTO dto = new PetUpdateDTO();
        dto.setName("小旺");
        dto.setWeight(30.5);

        PetDetailDTO result = petService.update(100L, 1L, dto);

        assertNotNull(result);
        verify(petMapper, times(1)).updateById(any(Pet.class));
    }

    // ===================== Delete Tests =====================

    @Test
    void shouldDeletePetSuccessfully() {
        when(petMapper.selectById(1L)).thenReturn(pet);
        when(petMapper.deleteById(1L)).thenReturn(1);

        assertDoesNotThrow(() -> petService.delete(100L, 1L));
        verify(petMapper, times(1)).deleteById(1L);
    }

    @Test
    void shouldThrowExceptionWhenDeleteOthersPet() {
        pet.setUserId(200L);
        when(petMapper.selectById(1L)).thenReturn(pet);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> petService.delete(100L, 1L));
        assertEquals("只能操作自己的宠物档案", ex.getMessage());
        verify(petMapper, never()).deleteById(any());
    }

    // ===================== GetHistory Tests =====================

    @Test
    void shouldReturnPetHistory() {
        when(petMapper.selectById(1L)).thenReturn(pet);

        Consultation consultation = new Consultation();
        consultation.setId(1L);
        consultation.setPetId(1L);
        consultation.setUserId(100L);
        consultation.setDoctorId(10L);
        consultation.setDepartmentId(5L);
        consultation.setStatus(ConsultationStatus.COMPLETED.getCode());

        Page<Consultation> consultationPage = new Page<>(1, 10);
        consultationPage.setRecords(List.of(consultation));
        consultationPage.setTotal(1);

        when(consultationMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(consultationPage);

        Department dept = new Department();
        dept.setId(5L);
        dept.setName("内科");
        when(departmentMapper.selectBatchIds(List.of(5L))).thenReturn(List.of(dept));

        Doctor doctor = new Doctor();
        doctor.setId(10L);
        doctor.setUserId(30L);
        when(doctorMapper.selectBatchIds(List.of(10L))).thenReturn(List.of(doctor));

        User doctorUser = new User();
        doctorUser.setId(30L);
        doctorUser.setRealName("李医生");
        when(userMapper.selectBatchIds(List.of(30L))).thenReturn(List.of(doctorUser));

        Page<PetHistoryDTO> result = petService.getHistory(100L, 1L, 1, 10);

        assertEquals(1, result.getTotal());
        PetHistoryDTO dto = result.getRecords().get(0);
        assertEquals("内科", dto.getDepartmentName());
        assertEquals("李医生", dto.getDoctorName());
    }
}
