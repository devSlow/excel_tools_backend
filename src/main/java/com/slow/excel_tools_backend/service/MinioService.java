package com.slow.excel_tools_backend.service;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.InputStream;

/**
 * MinIO 文件存储服务
 */
@Service
public class MinioService {

    private static final Logger log = LoggerFactory.getLogger(MinioService.class);

    private final MinioClient minioClient;

    public MinioService(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    /**
     * 上传文件
     *
     * @param bucket      桶名称
     * @param objectName  对象名称（含路径，如 excel/xxx.xlsx）
     * @param stream      文件输入流
     * @param contentType 文件 MIME 类型
     * @param size        文件大小（字节）
     */
    public void upload(String bucket, String objectName, InputStream stream, String contentType, long size) {
        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .stream(stream, size, -1)
                    .contentType(contentType)
                    .build());
            log.info("文件上传成功: bucket={}, object={}", bucket, objectName);
        } catch (Exception e) {
            log.error("文件上传失败: bucket={}, object={}", bucket, objectName, e);
            throw new RuntimeException("文件上传失败", e);
        }
    }

    /**
     * 下载文件
     *
     * @param bucket     桶名称
     * @param objectName 对象名称
     * @return 文件输入流
     */
    public InputStream download(String bucket, String objectName) {
        try {
            return minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .build());
        } catch (Exception e) {
            log.error("文件下载失败: bucket={}, object={}", bucket, objectName, e);
            throw new RuntimeException("文件下载失败", e);
        }
    }

    /**
     * 删除文件
     *
     * @param bucket     桶名称
     * @param objectName 对象名称
     */
    public void remove(String bucket, String objectName) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .build());
            log.info("文件删除成功: bucket={}, object={}", bucket, objectName);
        } catch (Exception e) {
            log.error("文件删除失败: bucket={}, object={}", bucket, objectName, e);
            throw new RuntimeException("文件删除失败", e);
        }
    }
}
