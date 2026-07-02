package com.petcare.system.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class PetListItemDTO {

    private Long id;
    private String name;
    private Integer species;
    private String speciesName;
    private String breed;
    private Integer gender;
    private String genderName;
    private LocalDate birthDate;
    private BigDecimal weight;
    private String avatar;
    private Integer neutered;
}
