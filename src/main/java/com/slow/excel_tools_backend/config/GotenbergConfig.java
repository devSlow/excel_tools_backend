package com.slow.excel_tools_backend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "gotenberg")
public class GotenbergConfig {
    private String baseUrl = "http://localhost:3000";
    private int timeout = 60;
}