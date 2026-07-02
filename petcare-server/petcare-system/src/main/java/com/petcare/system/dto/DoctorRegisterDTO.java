package com.petcare.system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DoctorRegisterDTO {

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 50, message = "密码长度必须在6-50之间")
    private String password;

    @NotBlank(message = "姓名不能为空")
    private String realName;

    @NotNull(message = "科室ID不能为空")
    private Long departmentId;

    private String title;
    private String specialty;
    private Integer experience;
    private String education;
    private String hospital;
    private String introduction;
}
