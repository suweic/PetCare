package com.petcare.system.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 根路径欢迎页 & 健康检查。
 * 解决浏览器直接访问 http://localhost:8080/ 显示 404 的问题。
 */
@RestController
public class IndexController {

    @GetMapping("/")
    public Map<String, Object> index() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("service", "petcare-system");
        body.put("status", "UP");
        body.put("message", "PetCare 后端 API 服务运行中。请通过前端或 API 文档访问业务接口。");

        Map<String, String> links = new LinkedHashMap<>();
        links.put("apiDocs",   "/v3/api-docs");
        links.put("swaggerUi", "/swagger-ui.html");
        links.put("knife4j",   "/doc.html");
        links.put("health",    "/actuator/health");
        links.put("ping",      "/api/ping");
        body.put("links", links);

        return body;
    }

    @GetMapping("/api/ping")
    public Map<String, String> ping() {
        Map<String, String> result = new LinkedHashMap<>();
        result.put("status", "UP");
        result.put("service", "petcare-system");
        return result;
    }
}
