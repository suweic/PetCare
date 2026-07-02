package com.petcare.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.petcare.system.dto.DepartmentDTO;
import com.petcare.system.entity.Department;
import com.petcare.system.mapper.DepartmentMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DepartmentServiceImplTest {

    @Mock
    private DepartmentMapper departmentMapper;

    @InjectMocks
    private DepartmentServiceImpl departmentService;

    @Test
    void shouldReturnDepartmentList() {
        Department dept1 = new Department();
        dept1.setId(1L);
        dept1.setName("内科");
        dept1.setDescription("内科疾病诊断");
        dept1.setSort(1);
        dept1.setStatus(1);

        Department dept2 = new Department();
        dept2.setId(2L);
        dept2.setName("外科");
        dept2.setDescription("外科手术");
        dept2.setSort(2);
        dept2.setStatus(1);

        when(departmentMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(dept1, dept2));

        List<DepartmentDTO> result = departmentService.getDepartmentList();

        assertEquals(2, result.size());
        assertEquals("内科", result.get(0).getName());
        assertEquals("外科", result.get(1).getName());
    }

    @Test
    void shouldReturnEmptyListWhenNoDepartments() {
        when(departmentMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.emptyList());

        List<DepartmentDTO> result = departmentService.getDepartmentList();

        assertTrue(result.isEmpty());
    }
}
