package com.slow.excel_tools_backend.controller;

import com.slow.excel_tools_backend.common.Result;
import com.slow.excel_tools_backend.service.GotenbergService;
import com.slow.excel_tools_backend.service.MinioService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;

@Api(tags = "PDF转换服务")
@RestController
@RequestMapping("/api/gotenberg")
public class GotenbergController {

    private final GotenbergService gotenbergService;
    private final MinioService minioService;

    public GotenbergController(GotenbergService gotenbergService, MinioService minioService) {
        this.gotenbergService = gotenbergService;
        this.minioService = minioService;
    }

    // ==================== 系统接口 ====================

    @ApiOperation("健康检查")
    @GetMapping("/health")
    public Result<String> health() {
        try {
            return Result.ok(gotenbergService.health());
        } catch (IOException e) {
            return Result.fail("健康检查失败: " + e.getMessage());
        }
    }

    @ApiOperation("获取版本")
    @GetMapping("/version")
    public Result<String> version() {
        try {
            return Result.ok(gotenbergService.version());
        } catch (IOException e) {
            return Result.fail("获取版本失败: " + e.getMessage());
        }
    }

    // ==================== LibreOffice 文档转换 ====================

    @ApiOperation("文档格式转换 - 支持Office与PDF互转")
    @PostMapping("/convert/document")
    public void convertDocument(
            @ApiParam(value = "文件", required = true) @RequestParam("file") MultipartFile file,
            @ApiParam(value = "目标格式: pdf, docx, xlsx, pptx", required = true) @RequestParam("targetFormat") String targetFormat,
            @ApiParam("密码(可选)") @RequestParam(value = "password", required = false) String password,
            HttpServletResponse response) {
        try {
            byte[] resultBytes;
            String filename = file.getOriginalFilename();
            String baseName = filename != null ? filename.substring(0, filename.lastIndexOf(".")) : "converted";

            if ("pdf".equalsIgnoreCase(targetFormat)) {
                resultBytes = password != null ? gotenbergService.convertToPdf(file, password) : gotenbergService.convertToPdf(file);
                baseName += ".pdf";
                response.setContentType("application/pdf");
            } else {
                resultBytes = gotenbergService.convertPdfToOffice(file, targetFormat);
                baseName += "." + targetFormat.toLowerCase();
                String contentType = getOfficeContentType(targetFormat);
                response.setContentType(contentType);
            }

            response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(baseName, StandardCharsets.UTF_8));
            response.getOutputStream().write(resultBytes);
        } catch (IOException e) {
            response.setStatus(500);
            try {
                response.getWriter().write("转换失败: " + e.getMessage());
            } catch (IOException ignored) {}
        }
    }

    @ApiOperation("文档格式转换(返回下载链接) - 支持Office与PDF互转")
    @PostMapping("/convert/document/url")
    public Result<String> convertDocumentUrl(
            @ApiParam(value = "文件", required = true) @RequestParam("file") MultipartFile file,
            @ApiParam(value = "目标格式: pdf, docx, xlsx, pptx", required = true) @RequestParam("targetFormat") String targetFormat,
            @ApiParam("密码(可选)") @RequestParam(value = "password", required = false) String password) {
        try {
            byte[] resultBytes;
            String filename = file.getOriginalFilename();
            String baseName = filename != null ? filename.substring(0, filename.lastIndexOf(".")) : "converted";
            String ext;

            if ("pdf".equalsIgnoreCase(targetFormat)) {
                resultBytes = password != null ? gotenbergService.convertToPdf(file, password) : gotenbergService.convertToPdf(file);
                ext = ".pdf";
            } else {
                resultBytes = gotenbergService.convertPdfToOffice(file, targetFormat);
                ext = "." + targetFormat.toLowerCase();
            }

            // 上传到 MinIO
            String objectName = "converted/" + UUID.randomUUID().toString() + ext;
            String contentType = "pdf".equalsIgnoreCase(targetFormat) ? "application/pdf" : getOfficeContentType(targetFormat);
            InputStream stream = new java.io.ByteArrayInputStream(resultBytes);
            String downloadUrl = minioService.upload(objectName, stream, contentType, resultBytes.length);

            return Result.ok(downloadUrl);
        } catch (IOException e) {
            return Result.fail("转换失败: " + e.getMessage());
        }
    }

    @ApiOperation("下载转换后的文件")
    @GetMapping("/download/{filename}")
    public void downloadFile(@PathVariable String filename, HttpServletResponse response) {
        try {
            // 先尝试从临时目录读取（兼容旧逻辑）
            String tempDir = System.getProperty("java.io.tmpdir");
            java.io.File file = new java.io.File(tempDir + "/" + filename);

            if (file.exists()) {
                response.setContentType("application/octet-stream");
                response.setHeader("Content-Disposition", "attachment; filename=" + filename);
                java.nio.file.Files.copy(file.toPath(), response.getOutputStream());
                return;
            }

            // 回退到 MinIO
            String objectName = "converted/" + filename;
            try (InputStream minioStream = minioService.download(objectName)) {
                response.setContentType("application/octet-stream");
                response.setHeader("Content-Disposition", "attachment; filename=" + filename);
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = minioStream.read(buffer)) != -1) {
                    response.getOutputStream().write(buffer, 0, bytesRead);
                }
            }
        } catch (Exception e) {
            response.setStatus(404);
            try {
                response.getWriter().write("文件不存在");
            } catch (IOException ignored) {}
        }
    }

    @ApiOperation("Office文档转PDF - 支持doc,docx,xls,xlsx,ppt,pptx,odt,ods,odp等格式")
    @PostMapping("/convert/office-to-pdf")
    public void convertOfficeToPdf(
            @ApiParam(value = "Office文件", required = true) @RequestParam("file") MultipartFile file,
            @ApiParam("密码(可选)") @RequestParam(value = "password", required = false) String password,
            HttpServletResponse response) {
        try {
            byte[] pdfBytes = password != null ? gotenbergService.convertToPdf(file, password) : gotenbergService.convertToPdf(file);
            String filename = file.getOriginalFilename();
            if (filename != null) {
                filename = filename.substring(0, filename.lastIndexOf(".")) + ".pdf";
            } else {
                filename = "converted.pdf";
            }
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(filename, StandardCharsets.UTF_8));
            response.getOutputStream().write(pdfBytes);
        } catch (IOException e) {
            response.setStatus(500);
            try {
                response.getWriter().write("转换失败: " + e.getMessage());
            } catch (IOException ignored) {}
        }
    }

    @ApiOperation("PDF转Word/Excel/PPT - 支持docx,xlsx,pptx格式")
    @PostMapping("/convert/pdf-to-office")
    public void convertPdfToOffice(
            @ApiParam(value = "PDF文件", required = true) @RequestParam("file") MultipartFile file,
            @ApiParam("目标格式: docx, xlsx, pptx") @RequestParam(value = "targetFormat", required = false, defaultValue = "docx") String targetFormat,
            HttpServletResponse response) {
        try {
            byte[] bytes = gotenbergService.convertPdfToOffice(file, targetFormat);
            String filename = file.getOriginalFilename();
            if (filename != null) {
                filename = filename.substring(0, filename.lastIndexOf(".")) + "." + targetFormat;
            } else {
                filename = "converted." + targetFormat;
            }
            String contentType = getOfficeContentType(targetFormat);
            response.setContentType(contentType);
            response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(filename, StandardCharsets.UTF_8));
            response.getOutputStream().write(bytes);
        } catch (IOException e) {
            response.setStatus(500);
            try {
                response.getWriter().write("转换失败: " + e.getMessage());
            } catch (IOException ignored) {}
        }
    }

    private String getOfficeContentType(String format) {
        switch (format.toLowerCase()) {
            case "docx":
                return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "xlsx":
                return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "pptx":
                return "application/vnd.openxmlformats-officedocument.presentationml.presentation";
            default:
                return "application/octet-stream";
        }
    }

    // ==================== PDF 操作接口 ====================

    @ApiOperation("合并PDF文件")
    @PostMapping("/pdf/merge")
    public void mergePdfs(
            @ApiParam(value = "PDF文件列表(至少2个)", required = true) @RequestParam("files") MultipartFile[] files,
            HttpServletResponse response) {
        try {
            byte[] pdfBytes = gotenbergService.mergePdfs(files);
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "attachment; filename=merged.pdf");
            response.getOutputStream().write(pdfBytes);
        } catch (IOException e) {
            response.setStatus(500);
            try {
                response.getWriter().write("合并失败: " + e.getMessage());
            } catch (IOException ignored) {}
        }
    }

    @ApiOperation("合并PDF文件(返回下载链接)")
    @PostMapping("/pdf/merge/url")
    public Result<String> mergePdfsUrl(
            @ApiParam(value = "PDF文件列表(至少2个)", required = true) @RequestParam("files") MultipartFile[] files) {
        try {
            byte[] pdfBytes = gotenbergService.mergePdfs(files);
            String objectName = "converted/" + UUID.randomUUID().toString() + ".pdf";
            InputStream stream = new java.io.ByteArrayInputStream(pdfBytes);
            String downloadUrl = minioService.upload(objectName, stream, "application/pdf", pdfBytes.length);
            return Result.ok(downloadUrl);
        } catch (IOException e) {
            return Result.fail("合并失败: " + e.getMessage());
        }
    }

    @ApiOperation("上传单个PDF文件到临时目录")
    @PostMapping("/pdf/merge/upload")
    public Result<String> uploadPdfForMerge(
            @ApiParam(value = "PDF文件", required = true) @RequestParam("file") MultipartFile file) {
        try {
            System.out.println("=== 上传PDF文件 ===");
            System.out.println("文件名: " + file.getOriginalFilename());
            System.out.println("文件大小: " + file.getSize() + " bytes");
            
            // 生成唯一文件名
            String uniqueFileName = UUID.randomUUID().toString() + ".pdf";
            String tempDir = System.getProperty("java.io.tmpdir");
            String filePath = tempDir + "/" + uniqueFileName;
            
            // 保存文件
            java.nio.file.Files.write(java.nio.file.Paths.get(filePath), file.getBytes());
            System.out.println("文件保存成功: " + filePath);
            
            return Result.ok(uniqueFileName);
        } catch (IOException e) {
            System.err.println("上传失败: " + e.getMessage());
            return Result.fail("上传失败: " + e.getMessage());
        }
    }

    @ApiOperation("合并已上传的PDF文件")
    @PostMapping("/pdf/merge/files")
    public Result<String> mergeUploadedPdfs(
            @ApiParam(value = "文件名列表", required = true) @RequestParam("filenames") String filenames) {
        try {
            String[] filenameArray = filenames.split(",");
            String tempDir = System.getProperty("java.io.tmpdir");
            java.util.List<MultipartFile> files = new java.util.ArrayList<>();
            
            for (String filename : filenameArray) {
                String filePath = tempDir + "/" + filename.trim();
                java.io.File file = new java.io.File(filePath);
                if (file.exists()) {
                    byte[] content = java.nio.file.Files.readAllBytes(file.toPath());
                    org.springframework.mock.web.MockMultipartFile multipartFile = 
                        new org.springframework.mock.web.MockMultipartFile(
                            "files", filename.trim(), "application/pdf", content);
                    files.add(multipartFile);
                }
            }
            
            if (files.size() < 2) {
                return Result.fail("至少需要2个文件");
            }
            
            byte[] pdfBytes = gotenbergService.mergePdfs(files.toArray(new MultipartFile[0]));
            
            // 上传到 MinIO
            String objectName = "converted/" + UUID.randomUUID().toString() + ".pdf";
            InputStream stream = new java.io.ByteArrayInputStream(pdfBytes);
            String downloadUrl = minioService.upload(objectName, stream, "application/pdf", pdfBytes.length);
            
            // 清理临时文件
            for (String filename : filenameArray) {
                new java.io.File(tempDir + "/" + filename.trim()).delete();
            }
            
            return Result.ok(downloadUrl);
        } catch (IOException e) {
            return Result.fail("合并失败: " + e.getMessage());
        }
    }

    @ApiOperation("拆分PDF文件")
    @PostMapping("/pdf/split")
    public void splitPdf(
            @ApiParam(value = "PDF文件", required = true) @RequestParam("file") MultipartFile file,
            @ApiParam(value = "拆分模式: intervals(按间隔), pages(按单页)", required = true) @RequestParam("splitMode") String splitMode,
            @ApiParam(value = "间隔数或页码", required = true) @RequestParam("splitSpan") String splitSpan,
            @ApiParam("合并模式(仅pages模式有效)") @RequestParam(value = "splitUnify", required = false, defaultValue = "false") boolean splitUnify,
            HttpServletResponse response) {
        try {
            System.out.println("=== 开始拆分PDF ===");
            System.out.println("文件名: " + file.getOriginalFilename());
            System.out.println("文件大小: " + file.getSize() + " bytes");
            System.out.println("拆分模式: " + splitMode);
            System.out.println("间隔数/页码: " + splitSpan);
            System.out.println("合并模式: " + splitUnify);
            
            byte[] pdfBytes = gotenbergService.splitPdf(file, splitMode, splitSpan, splitUnify);
            System.out.println("拆分结果大小: " + pdfBytes.length + " bytes");
            
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "attachment; filename=split.pdf");
            response.getOutputStream().write(pdfBytes);
        } catch (IOException e) {
            System.err.println("拆分失败: " + e.getMessage());
            e.printStackTrace();
            response.setStatus(500);
            try {
                response.getWriter().write("拆分失败: " + e.getMessage());
            } catch (IOException ignored) {}
        }
    }

    @ApiOperation("拆分PDF文件(返回下载链接)")
    @PostMapping("/pdf/split/url")
    public Result<List<String>> splitPdfUrl(
            @ApiParam(value = "PDF文件", required = true) @RequestParam("file") MultipartFile file,
            @ApiParam(value = "拆分模式: intervals(按间隔), pages(按单页)", required = true) @RequestParam("splitMode") String splitMode,
            @ApiParam(value = "间隔数或页码", required = true) @RequestParam("splitSpan") String splitSpan,
            @ApiParam("合并模式(仅pages模式有效)") @RequestParam(value = "splitUnify", required = false, defaultValue = "false") boolean splitUnify) {
        try {
            byte[] resultBytes = gotenbergService.splitPdf(file, splitMode, splitSpan, splitUnify);
            List<String> downloadUrls = new ArrayList<>();

            if (isZipFile(resultBytes)) {
                // ZIP: 解压后逐个上传到 MinIO
                String tempDir = System.getProperty("java.io.tmpdir");
                String zipFileName = UUID.randomUUID().toString() + ".zip";
                String zipFilePath = tempDir + "/" + zipFileName;
                java.nio.file.Files.write(java.nio.file.Paths.get(zipFilePath), resultBytes);

                try {
                    String extractDir = tempDir + "/extract_" + UUID.randomUUID().toString();
                    new java.io.File(extractDir).mkdirs();
                    Process process = Runtime.getRuntime().exec(new String[]{"unzip", "-o", zipFilePath, "-d", extractDir});
                    process.waitFor();

                    java.io.File extractDirFile = new java.io.File(extractDir);
                    java.io.File[] pdfFiles = extractDirFile.listFiles((dir, name) -> name.toLowerCase().endsWith(".pdf"));
                    if (pdfFiles != null) {
                        java.util.Arrays.sort(pdfFiles, (a, b) -> a.getName().compareTo(b.getName()));
                        for (java.io.File pdfFile : pdfFiles) {
                            byte[] fileBytes = java.nio.file.Files.readAllBytes(pdfFile.toPath());
                            String objectName = "converted/" + UUID.randomUUID().toString() + ".pdf";
                            InputStream stream = new java.io.ByteArrayInputStream(fileBytes);
                            downloadUrls.add(minioService.upload(objectName, stream, "application/pdf", fileBytes.length));
                        }
                    }
                    deleteDirectory(extractDirFile);
                } catch (Exception e) {
                    // 解压失败，作为单个 PDF 处理
                    String objectName = "converted/" + UUID.randomUUID().toString() + ".pdf";
                    InputStream stream = new java.io.ByteArrayInputStream(resultBytes);
                    downloadUrls.add(minioService.upload(objectName, stream, "application/pdf", resultBytes.length));
                }
                new java.io.File(zipFilePath).delete();
            } else {
                // 单个 PDF
                String objectName = "converted/" + UUID.randomUUID().toString() + ".pdf";
                InputStream stream = new java.io.ByteArrayInputStream(resultBytes);
                downloadUrls.add(minioService.upload(objectName, stream, "application/pdf", resultBytes.length));
            }

            return Result.ok(downloadUrls);
        } catch (IOException e) {
            return Result.fail("拆分失败: " + e.getMessage());
        }
    }
    
    private boolean isZipFile(byte[] bytes) {
        if (bytes == null || bytes.length < 4) {
            return false;
        }
        // ZIP文件的魔数: PK (0x504B)
        return bytes[0] == 0x50 && bytes[1] == 0x4B;
    }
    
    private void deleteDirectory(java.io.File directory) {
        java.io.File[] files = directory.listFiles();
        if (files != null) {
            for (java.io.File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
        }
        directory.delete();
    }

    @ApiOperation("扁平化PDF(合并注解到页面)")
    @PostMapping("/pdf/flatten")
    public void flattenPdf(
            @ApiParam(value = "PDF文件", required = true) @RequestParam("file") MultipartFile file,
            HttpServletResponse response) {
        try {
            byte[] pdfBytes = gotenbergService.flattenPdf(file);
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "attachment; filename=flattened.pdf");
            response.getOutputStream().write(pdfBytes);
        } catch (IOException e) {
            response.setStatus(500);
            try {
                response.getWriter().write("扁平化失败: " + e.getMessage());
            } catch (IOException ignored) {}
        }
    }

    @ApiOperation("转换PDF格式")
    @PostMapping("/pdf/convert")
    public void convertPdfFormat(
            @ApiParam(value = "PDF文件", required = true) @RequestParam("file") MultipartFile file,
            @ApiParam("PDF/A格式: 1a,1b,2a,2b,3a,3b,3u") @RequestParam(value = "pdfa", required = false) String pdfa,
            @ApiParam("PDF/UA格式") @RequestParam(value = "pdfua", required = false, defaultValue = "false") boolean pdfua,
            HttpServletResponse response) {
        try {
            byte[] pdfBytes = gotenbergService.convertPdfFormat(file, pdfa, pdfua);
            String filename = file.getOriginalFilename();
            if (filename != null) {
                filename = filename.substring(0, filename.lastIndexOf(".")) + "_converted.pdf";
            } else {
                filename = "converted.pdf";
            }
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(filename, StandardCharsets.UTF_8));
            response.getOutputStream().write(pdfBytes);
        } catch (IOException e) {
            response.setStatus(500);
            try {
                response.getWriter().write("格式转换失败: " + e.getMessage());
            } catch (IOException ignored) {}
        }
    }

    @ApiOperation("读取PDF元数据")
    @PostMapping("/pdf/metadata/read")
    public Result<String> readPdfMetadata(
            @ApiParam(value = "PDF文件", required = true) @RequestParam("file") MultipartFile file) {
        try {
            String metadata = gotenbergService.readPdfMetadata(file);
            return Result.ok(metadata);
        } catch (IOException e) {
            return Result.fail("读取元数据失败: " + e.getMessage());
        }
    }

    @ApiOperation("写入PDF元数据")
    @PostMapping("/pdf/metadata/write")
    public void writePdfMetadata(
            @ApiParam(value = "PDF文件", required = true) @RequestParam("file") MultipartFile file,
            @ApiParam(value = "元数据JSON", required = true) @RequestParam("metadata") String metadata,
            HttpServletResponse response) {
        try {
            byte[] pdfBytes = gotenbergService.writePdfMetadata(file, metadata);
            String filename = file.getOriginalFilename();
            if (filename != null) {
                filename = filename.substring(0, filename.lastIndexOf(".")) + "_metadata.pdf";
            } else {
                filename = "result.pdf";
            }
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(filename, StandardCharsets.UTF_8));
            response.getOutputStream().write(pdfBytes);
        } catch (IOException e) {
            response.setStatus(500);
            try {
                response.getWriter().write("写入元数据失败: " + e.getMessage());
            } catch (IOException ignored) {}
        }
    }

    @ApiOperation("读取PDF书签")
    @PostMapping("/pdf/bookmarks/read")
    public Result<String> readPdfBookmarks(
            @ApiParam(value = "PDF文件", required = true) @RequestParam("file") MultipartFile file) {
        try {
            String bookmarks = gotenbergService.readPdfBookmarks(file);
            return Result.ok(bookmarks);
        } catch (IOException e) {
            return Result.fail("读取书签失败: " + e.getMessage());
        }
    }

    @ApiOperation("写入PDF书签")
    @PostMapping("/pdf/bookmarks/write")
    public void writePdfBookmarks(
            @ApiParam(value = "PDF文件", required = true) @RequestParam("file") MultipartFile file,
            @ApiParam(value = "书签JSON", required = true) @RequestParam("bookmarks") String bookmarks,
            HttpServletResponse response) {
        try {
            byte[] pdfBytes = gotenbergService.writePdfBookmarks(file, bookmarks);
            String filename = file.getOriginalFilename();
            if (filename != null) {
                filename = filename.substring(0, filename.lastIndexOf(".")) + "_bookmarks.pdf";
            } else {
                filename = "result.pdf";
            }
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(filename, StandardCharsets.UTF_8));
            response.getOutputStream().write(pdfBytes);
        } catch (IOException e) {
            response.setStatus(500);
            try {
                response.getWriter().write("写入书签失败: " + e.getMessage());
            } catch (IOException ignored) {}
        }
    }

    @ApiOperation("加密PDF")
    @PostMapping("/pdf/encrypt")
    public void encryptPdf(
            @ApiParam(value = "PDF文件", required = true) @RequestParam("file") MultipartFile file,
            @ApiParam(value = "用户密码", required = true) @RequestParam("userPassword") String userPassword,
            @ApiParam("所有者密码") @RequestParam(value = "ownerPassword", required = false) String ownerPassword,
            HttpServletResponse response) {
        try {
            byte[] pdfBytes = gotenbergService.encryptPdf(file, userPassword, ownerPassword);
            String filename = file.getOriginalFilename();
            if (filename != null) {
                filename = filename.substring(0, filename.lastIndexOf(".")) + "_encrypted.pdf";
            } else {
                filename = "encrypted.pdf";
            }
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(filename, StandardCharsets.UTF_8));
            response.getOutputStream().write(pdfBytes);
        } catch (IOException e) {
            response.setStatus(500);
            try {
                response.getWriter().write("加密失败: " + e.getMessage());
            } catch (IOException ignored) {}
        }
    }

    @ApiOperation("加密PDF(返回下载链接)")
    @PostMapping("/pdf/encrypt/url")
    public Result<String> encryptPdfUrl(
            @ApiParam(value = "PDF文件", required = true) @RequestParam("file") MultipartFile file,
            @ApiParam(value = "用户密码", required = true) @RequestParam("userPassword") String userPassword,
            @ApiParam("所有者密码") @RequestParam(value = "ownerPassword", required = false) String ownerPassword) {
        try {
            byte[] pdfBytes = gotenbergService.encryptPdf(file, userPassword, ownerPassword);
            String objectName = "converted/" + UUID.randomUUID().toString() + ".pdf";
            InputStream stream = new java.io.ByteArrayInputStream(pdfBytes);
            String downloadUrl = minioService.upload(objectName, stream, "application/pdf", pdfBytes.length);
            return Result.ok(downloadUrl);
        } catch (IOException e) {
            return Result.fail("加密失败: " + e.getMessage());
        }
    }

    @ApiOperation("向PDF嵌入文件")
    @PostMapping("/pdf/embed")
    public void embedFiles(
            @ApiParam(value = "PDF文件", required = true) @RequestParam("file") MultipartFile pdfFile,
            @ApiParam(value = "要嵌入的文件", required = true) @RequestParam("embeds") MultipartFile[] embedFiles,
            HttpServletResponse response) {
        try {
            byte[] pdfBytes = gotenbergService.embedFiles(pdfFile, embedFiles);
            String filename = pdfFile.getOriginalFilename();
            if (filename != null) {
                filename = filename.substring(0, filename.lastIndexOf(".")) + "_embedded.pdf";
            } else {
                filename = "embedded.pdf";
            }
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(filename, StandardCharsets.UTF_8));
            response.getOutputStream().write(pdfBytes);
        } catch (IOException e) {
            response.setStatus(500);
            try {
                response.getWriter().write("嵌入文件失败: " + e.getMessage());
            } catch (IOException ignored) {}
        }
    }

    @ApiOperation("添加水印")
    @PostMapping("/pdf/watermark")
    public void addWatermark(
            @ApiParam(value = "PDF文件", required = true) @RequestParam("file") MultipartFile file,
            @ApiParam(value = "水印类型: text, image, pdf", required = true) @RequestParam("source") String source,
            @ApiParam("水印内容/图片路径/PDF路径") @RequestParam(value = "expression", required = false) String expression,
            @ApiParam("页码范围(如: 1-3,5,7-10)") @RequestParam(value = "pages", required = false) String pages,
            HttpServletResponse response) {
        try {
            byte[] pdfBytes = gotenbergService.addWatermark(file, source, expression, pages);
            String filename = file.getOriginalFilename();
            if (filename != null) {
                filename = filename.substring(0, filename.lastIndexOf(".")) + "_watermarked.pdf";
            } else {
                filename = "watermarked.pdf";
            }
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(filename, StandardCharsets.UTF_8));
            response.getOutputStream().write(pdfBytes);
        } catch (IOException e) {
            response.setStatus(500);
            try {
                response.getWriter().write("添加水印失败: " + e.getMessage());
            } catch (IOException ignored) {}
        }
    }

    @ApiOperation("添加水印(返回下载链接)")
    @PostMapping("/pdf/watermark/url")
    public Result<String> addWatermarkUrl(
            @ApiParam(value = "PDF文件", required = true) @RequestParam("file") MultipartFile file,
            @ApiParam(value = "水印类型: text, image, pdf", required = true) @RequestParam("source") String source,
            @ApiParam("水印内容/图片路径/PDF路径") @RequestParam(value = "expression", required = false) String expression,
            @ApiParam("页码范围(如: 1-3,5,7-10)") @RequestParam(value = "pages", required = false) String pages) {
        try {
            byte[] pdfBytes = gotenbergService.addWatermark(file, source, expression, pages);
            String objectName = "converted/" + UUID.randomUUID().toString() + ".pdf";
            InputStream stream = new java.io.ByteArrayInputStream(pdfBytes);
            String downloadUrl = minioService.upload(objectName, stream, "application/pdf", pdfBytes.length);
            return Result.ok(downloadUrl);
        } catch (IOException e) {
            return Result.fail("添加水印失败: " + e.getMessage());
        }
    }

    @ApiOperation("添加印章")
    @PostMapping("/pdf/stamp")
    public void addStamp(
            @ApiParam(value = "PDF文件", required = true) @RequestParam("file") MultipartFile file,
            @ApiParam(value = "印章类型: text, image, pdf", required = true) @RequestParam("source") String source,
            @ApiParam("印章内容/图片路径/PDF路径") @RequestParam(value = "expression", required = false) String expression,
            @ApiParam("页码范围(如: 1-3,5,7-10)") @RequestParam(value = "pages", required = false) String pages,
            HttpServletResponse response) {
        try {
            byte[] pdfBytes = gotenbergService.addStamp(file, source, expression, pages);
            String filename = file.getOriginalFilename();
            if (filename != null) {
                filename = filename.substring(0, filename.lastIndexOf(".")) + "_stamped.pdf";
            } else {
                filename = "stamped.pdf";
            }
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(filename, StandardCharsets.UTF_8));
            response.getOutputStream().write(pdfBytes);
        } catch (IOException e) {
            response.setStatus(500);
            try {
                response.getWriter().write("添加印章失败: " + e.getMessage());
            } catch (IOException ignored) {}
        }
    }

    @ApiOperation("旋转PDF页面")
    @PostMapping("/pdf/rotate")
    public void rotatePdf(
            @ApiParam(value = "PDF文件", required = true) @RequestParam("file") MultipartFile file,
            @ApiParam(value = "旋转角度: 90, 180, 270", required = true) @RequestParam("angle") int angle,
            @ApiParam("页码范围(如: 1-3,5,7-10)") @RequestParam(value = "pages", required = false) String pages,
            HttpServletResponse response) {
        try {
            byte[] pdfBytes = gotenbergService.rotatePdf(file, angle, pages);
            String filename = file.getOriginalFilename();
            if (filename != null) {
                filename = filename.substring(0, filename.lastIndexOf(".")) + "_rotated.pdf";
            } else {
                filename = "rotated.pdf";
            }
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(filename, StandardCharsets.UTF_8));
            response.getOutputStream().write(pdfBytes);
        } catch (IOException e) {
            response.setStatus(500);
            try {
                response.getWriter().write("旋转失败: " + e.getMessage());
            } catch (IOException ignored) {}
        }
    }

    @ApiOperation("旋转PDF页面(返回下载链接)")
    @PostMapping("/pdf/rotate/url")
    public Result<String> rotatePdfUrl(
            @ApiParam(value = "PDF文件", required = true) @RequestParam("file") MultipartFile file,
            @ApiParam(value = "旋转角度: 90, 180, 270", required = true) @RequestParam("angle") int angle,
            @ApiParam("页码范围(如: 1-3,5,7-10)") @RequestParam(value = "pages", required = false) String pages) {
        try {
            byte[] pdfBytes = gotenbergService.rotatePdf(file, angle, pages);
            String objectName = "converted/" + UUID.randomUUID().toString() + ".pdf";
            InputStream stream = new java.io.ByteArrayInputStream(pdfBytes);
            String downloadUrl = minioService.upload(objectName, stream, "application/pdf", pdfBytes.length);
            return Result.ok(downloadUrl);
        } catch (IOException e) {
            return Result.fail("旋转失败: " + e.getMessage());
        }
    }

    // ==================== Chromium HTML转换 ====================

    @ApiOperation("网页转PDF - 统一接口")
    @PostMapping("/convert/to-pdf")
    public void convertToPdf(
            @ApiParam(value = "来源类型: url, html, markdown", required = true) @RequestParam("sourceType") String sourceType,
            @ApiParam("URL (sourceType=url时必填)") @RequestParam(value = "url", required = false) String url,
            @ApiParam("HTML文件 (sourceType=html或markdown时必填)") @RequestParam(value = "htmlFile", required = false) MultipartFile htmlFile,
            @ApiParam("额外文件 (html时为assets, markdown时为markdown文件)") @RequestParam(value = "extraFiles", required = false) MultipartFile[] extraFiles,
            HttpServletResponse response) {
        try {
            byte[] pdfBytes = gotenbergService.convertToPdf(sourceType, url, htmlFile, extraFiles);
            String filename = sourceType + "_converted.pdf";
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(filename, StandardCharsets.UTF_8));
            response.getOutputStream().write(pdfBytes);
        } catch (IOException e) {
            response.setStatus(500);
            try {
                response.getWriter().write("转换失败: " + e.getMessage());
            } catch (IOException ignored) {}
        }
    }

    // ==================== Chromium 截图 ====================

    @ApiOperation("网页截图 - 统一接口")
    @PostMapping("/screenshot")
    public void screenshot(
            @ApiParam(value = "来源类型: url, html, markdown", required = true) @RequestParam("sourceType") String sourceType,
            @ApiParam("URL (sourceType=url时必填)") @RequestParam(value = "url", required = false) String url,
            @ApiParam("HTML文件 (sourceType=html或markdown时必填)") @RequestParam(value = "htmlFile", required = false) MultipartFile htmlFile,
            @ApiParam("额外文件 (markdown时为markdown文件)") @RequestParam(value = "extraFiles", required = false) MultipartFile[] extraFiles,
            @ApiParam("图片格式: png, jpeg, webp") @RequestParam(value = "format", required = false, defaultValue = "png") String format,
            HttpServletResponse response) {
        try {
            byte[] imageBytes = gotenbergService.screenshot(sourceType, url, htmlFile, extraFiles, format);
            String contentType = "image/" + (format.equals("jpeg") ? "jpeg" : format);
            response.setContentType(contentType);
            response.setHeader("Content-Disposition", "attachment; filename=screenshot." + format);
            response.getOutputStream().write(imageBytes);
        } catch (IOException e) {
            response.setStatus(500);
            try {
                response.getWriter().write("截图失败: " + e.getMessage());
            } catch (IOException ignored) {}
        }
    }
}