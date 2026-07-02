package com.petcare.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.petcare.common.Result;
import com.petcare.system.dto.PetCreateDTO;
import com.petcare.system.dto.PetDetailDTO;
import com.petcare.system.dto.PetHistoryDTO;
import com.petcare.system.dto.PetListItemDTO;
import com.petcare.system.dto.PetUpdateDTO;
import com.petcare.system.service.PetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pet")
@RequiredArgsConstructor
public class PetController {

    private final PetService petService;

    @PostMapping
    public ResponseEntity<Result<PetDetailDTO>> create(
            Authentication authentication,
            @Valid @RequestBody PetCreateDTO dto) {
        Long userId = (Long) authentication.getPrincipal();
        PetDetailDTO result = petService.create(userId, dto);
        return ResponseEntity.ok(Result.success("创建成功", result));
    }

    @GetMapping
    public ResponseEntity<Result<Page<PetListItemDTO>>> list(
            Authentication authentication,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = (Long) authentication.getPrincipal();
        Page<PetListItemDTO> result = petService.list(userId, page, size);
        return ResponseEntity.ok(Result.success(result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Result<PetDetailDTO>> getById(
            Authentication authentication,
            @PathVariable Long id) {
        Long userId = (Long) authentication.getPrincipal();
        PetDetailDTO result = petService.getById(userId, id);
        return ResponseEntity.ok(Result.success(result));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Result<PetDetailDTO>> update(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody PetUpdateDTO dto) {
        Long userId = (Long) authentication.getPrincipal();
        PetDetailDTO result = petService.update(userId, id, dto);
        return ResponseEntity.ok(Result.success("更新成功", result));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Result<Void>> delete(
            Authentication authentication,
            @PathVariable Long id) {
        Long userId = (Long) authentication.getPrincipal();
        petService.delete(userId, id);
        return ResponseEntity.ok(Result.success("删除成功", null));
    }

    @GetMapping("/{id}/history")
    public ResponseEntity<Result<Page<PetHistoryDTO>>> getHistory(
            Authentication authentication,
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = (Long) authentication.getPrincipal();
        Page<PetHistoryDTO> result = petService.getHistory(userId, id, page, size);
        return ResponseEntity.ok(Result.success(result));
    }
}
