package com.petcare.system.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Knife4j / Swagger API 文档配置
 * 访问地址: http://localhost:8080/doc.html
 */
@Configuration
public class Knife4jConfig {

    @Bean
    public OpenAPI petCareOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("PetCare 宠物在线问诊系统 API")
                        .description("""
                                PetCare 是一个面向宠物主人的在线医疗问诊平台。

                                ## 模块
                                - **用户端 API**: /api/user/**, /api/doctor/**, /api/pet/**, /api/consultation/**
                                - **管理端 API**: /api/admin/**
                                - **通用 API**: /api/upload/**

                                ## 认证
                                管理端和用户端使用不同的 SecurityFilterChain，均通过 JWT Bearer Token 认证。
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("PetCare Team")
                                .email("2733808428@qq.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")))
                // JWT 认证配置
                .addSecurityItem(new SecurityRequirement().addList("Bearer"))
                .addSecurityItem(new SecurityRequirement().addList("AdminBearer"))
                .schemaRequirement("Bearer", new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("用户端 JWT Token（在登录接口获取）"))
                .schemaRequirement("AdminBearer", new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("管理端 JWT Token（在管理端登录接口获取）"));
    }
}
