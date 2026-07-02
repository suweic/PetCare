package com.petcare.system.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class PetHistoryDTO {

    private Long consultationId;
    private Long doctorId;
    private String doctorName;
    private String departmentName;
    private Integer consultationType;
    private Integer status;
    private String statusName;
    private String chiefComplaint;
    private LocalDateTime createTime;
    private LocalDateTime endTime;
}
