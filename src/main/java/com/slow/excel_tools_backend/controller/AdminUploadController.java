package com.slow.excel_tools_backend.controller;

import com.slow.excel_tools_backend.common.Result;
import com.slow.excel_tools_backend.service.MinioService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * 文件上传控制器
 */
@Api(tags = "文件上传")
@RestController
@RequestMapping("/api/admin/upload")
public class AdminUploadController {

    private final MinioService minioService;

    public AdminUploadController(MinioService minioService) {
        this.minioService = minioService;
    }

    @ApiOperation("上传图片")
    @PostMapping("/image")
    public Result<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            String url = minioService.upload(file, "banner");
            Map<String, String> data = new HashMap<>();
            data.put("url", url);
            return Result.ok(data);
        } catch (Exception e) {
            return Result.fail(500, "上传失败: " + e.getMessage());
        }
    }
}