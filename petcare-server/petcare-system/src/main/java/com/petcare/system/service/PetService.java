package com.petcare.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.petcare.system.dto.PetCreateDTO;
import com.petcare.system.dto.PetDetailDTO;
import com.petcare.system.dto.PetHistoryDTO;
import com.petcare.system.dto.PetListItemDTO;
import com.petcare.system.dto.PetUpdateDTO;

public interface PetService {

    PetDetailDTO create(Long userId, PetCreateDTO dto);

    Page<PetListItemDTO> list(Long userId, int page, int size);

    PetDetailDTO getById(Long userId, Long petId);

    PetDetailDTO update(Long userId, Long petId, PetUpdateDTO dto);

    void delete(Long userId, Long petId);

    Page<PetHistoryDTO> getHistory(Long userId, Long petId, int page, int size);
}
