/*
 * Copyright 2026 PetCare Contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.petcare.system;

import com.petcare.security.config.JwtProperties;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@ComponentScan(basePackages = {"com.petcare.common", "com.petcare.security", "com.petcare.system"})
@MapperScan("com.petcare.system.mapper")
@EnableConfigurationProperties(JwtProperties.class)
@EnableRetry
public class PetCareApplication {

    public static void main(String[] args) {
        SpringApplication.run(PetCareApplication.class, args);
    }
}