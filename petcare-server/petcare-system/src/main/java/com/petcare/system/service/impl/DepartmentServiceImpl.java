package com.petcare.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.petcare.system.dto.DepartmentDTO;
import com.petcare.system.entity.Department;
import com.petcare.system.mapper.DepartmentMapper;
import com.petcare.system.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentMapper departmentMapper;

    @Override
    public List<DepartmentDTO> getDepartmentList() {
        List<Department> departments = departmentMapper.selectList(
                new LambdaQueryWrapper<Department>()
                        .eq(Department::getStatus, 1)
                        .orderByAsc(Department::getSort));

        return departments.stream().map(dept -> {
            DepartmentDTO dto = new DepartmentDTO();
            dto.setId(dept.getId());
            dto.setName(dept.getName());
            dto.setDescription(dept.getDescription());
            dto.setIcon(dept.getIcon());
            dto.setSort(dept.getSort());
            return dto;
        }).collect(Collectors.toList());
    }
}
