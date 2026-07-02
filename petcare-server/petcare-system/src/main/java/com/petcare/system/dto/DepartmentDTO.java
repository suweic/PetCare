package com.petcare.system.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DepartmentDTO {

    private Long id;
    private String name;
    private String description;
    private String icon;
    private Integer sort;
}
