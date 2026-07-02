package com.petcare.system.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
public class DoctorScheduleDTO {

    private Long id;
    private Long doctorId;
    private LocalDate scheduleDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer status;
    private String statusName;
}
