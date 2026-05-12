package com.slow.excel_tools_backend.controller;

import com.slow.excel_tools_backend.common.Result;
import com.slow.excel_tools_backend.service.GotenbergService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Api(tags = "PDF转换服务")
@RestController
@RequestMapping("/api/gotenberg")
public class GotenbergController {

    private final GotenbergService gotenbergService;

    public GotenbergController(GotenbergService gotenbergService) {
        this.gotenbergService = gotenbergService;
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

    @ApiOperation("拆分PDF文件")
    @PostMapping("/pdf/split")
    public void splitPdf(
            @ApiParam(value = "PDF文件", required = true) @RequestParam("file") MultipartFile file,
            @ApiParam(value = "拆分模式: intervals(按间隔), pages(按单页)", required = true) @RequestParam("splitMode") String splitMode,
            @ApiParam(value = "间隔数或页码", required = true) @RequestParam("splitSpan") String splitSpan,
            @ApiParam("合并模式(仅pages模式有效)") @RequestParam(value = "splitUnify", required = false, defaultValue = "false") boolean splitUnify,
            HttpServletResponse response) {
        try {
            byte[] pdfBytes = gotenbergService.splitPdf(file, splitMode, splitSpan, splitUnify);
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "attachment; filename=split.pdf");
            response.getOutputStream().write(pdfBytes);
        } catch (IOException e) {
            response.setStatus(500);
            try {
                response.getWriter().write("拆分失败: " + e.getMessage());
            } catch (IOException ignored) {}
        }
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