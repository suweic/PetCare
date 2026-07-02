package com.petcare.system.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class PetUpdateDTO {

    private String name;

    private Integer species;

    private String breed;

    private Integer gender;

    private LocalDate birthDate;

    @DecimalMin(value = "0.01", message = "体重不能低于0.01公斤")
    @DecimalMax(value = "200.00", message = "体重不能超过200.00公斤")
    private BigDecimal weight;

    private Integer neutered;

    /** 过敏信息 */
    private String allergyInfo;
}
