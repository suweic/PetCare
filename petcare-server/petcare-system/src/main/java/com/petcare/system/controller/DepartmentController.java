package com.petcare.system.controller;

import com.petcare.common.Result;
import com.petcare.system.dto.DepartmentDTO;
import com.petcare.system.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/department")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    @GetMapping("/list")
    public ResponseEntity<Result<List<DepartmentDTO>>> getDepartmentList() {
        List<DepartmentDTO> result = departmentService.getDepartmentList();
        return ResponseEntity.ok(Result.success(result));
    }
}