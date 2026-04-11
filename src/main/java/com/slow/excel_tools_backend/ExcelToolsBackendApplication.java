package com.slow.excel_tools_backend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Excel 工具后端服务启动类
 * <p>
 * 文本转 Excel 与现场数据采集助手后端服务
 * </p>
 */
@SpringBootApplication
@MapperScan("com.slow.excel_tools_backend.mapper")
public class ExcelToolsBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExcelToolsBackendApplication.class, args);
    }

}
