package com.petcare.system.service;

import com.petcare.system.dto.DepartmentDTO;

import java.util.List;

public interface DepartmentService {

    List<DepartmentDTO> getDepartmentList();
}