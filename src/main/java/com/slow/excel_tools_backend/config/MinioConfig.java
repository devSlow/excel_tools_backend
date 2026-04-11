package com.slow.excel_tools_backend.config;

import io.minio.MinioClient;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MinIO 配置类
 * <p>
 * 读取 application.yaml 中的 minio 配置，创建 MinioClient Bean
 * </p>
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "minio")
public class MinioConfig {

    /** MinIO 服务地址 */
    private String endpoint;

    /** 访问密钥 */
    private String accessKey;

    /** 密钥 */
    private String secretKey;

    /** 默认存储桶名称 */
    private String bucket;

    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }
}
