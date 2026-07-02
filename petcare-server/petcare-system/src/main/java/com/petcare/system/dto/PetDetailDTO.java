package com.petcare.system.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class PetDetailDTO {

    private Long id;
    private Long userId;
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
    private String allergyInfo;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
