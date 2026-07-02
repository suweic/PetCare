package com.petcare.system;

import com.petcare.security.config.JwtProperties;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.petcare.common", "com.petcare.security", "com.petcare.system"})
@MapperScan("com.petcare.system.mapper")
@EnableConfigurationProperties(JwtProperties.class)
public class PetCareApplication {

    public static void main(String[] args) {
        SpringApplication.run(PetCareApplication.class, args);
    }
}