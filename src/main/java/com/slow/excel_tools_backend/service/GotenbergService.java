package com.slow.excel_tools_backend.service;

import com.slow.excel_tools_backend.config.GotenbergConfig;
import okhttp3.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Service
public class GotenbergService {

    private final GotenbergConfig gotenbergConfig;
    private final OkHttpClient httpClient;

    public GotenbergService(GotenbergConfig gotenbergConfig) {
        this.gotenbergConfig = gotenbergConfig;
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(gotenbergConfig.getTimeout(), TimeUnit.SECONDS)
                .readTimeout(gotenbergConfig.getTimeout(), TimeUnit.SECONDS)
                .writeTimeout(gotenbergConfig.getTimeout(), TimeUnit.SECONDS)
                .build();
    }

    public String getBaseUrl() {
        return gotenbergConfig.getBaseUrl();
    }

    // ==================== LibreOffice 文档转换 ====================

    /**
     * 将 Office 文档转换为 PDF
     * 支持: doc, docx, xls, xlsx, ppt, pptx, odt, ods, odp, etc.
     */
    public byte[] convertToPdf(MultipartFile file) throws IOException {
        return convertToPdf(file, null);
    }

    public byte[] convertToPdf(MultipartFile file, String password) throws IOException {
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("files", file.getOriginalFilename(),
                        RequestBody.create(file.getBytes(), MediaType.parse("application/octet-stream")))
                .build();

        if (password != null && !password.isEmpty()) {
            requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("files", file.getOriginalFilename(),
                            RequestBody.create(file.getBytes(), MediaType.parse("application/octet-stream")))
                    .addFormDataPart("password", password)
                    .build();
        }

        Request request = new Request.Builder()
                .url(gotenbergConfig.getBaseUrl() + "/forms/libreoffice/convert")
                .post(requestBody)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("转换失败: " + response.code() + " " + response.message());
            }
            return response.body().bytes();
        }
    }

    /**
     * 合并多个 PDF 文件
     */
    public byte[] mergePdfs(MultipartFile[] files) throws IOException {
        MultipartBody.Builder requestBodyBuilder = new MultipartBody.Builder().setType(MultipartBody.FORM);
        for (MultipartFile file : files) {
            requestBodyBuilder.addFormDataPart("files", file.getOriginalFilename(),
                    RequestBody.create(file.getBytes(), MediaType.parse("application/pdf")));
        }

        Request request = new Request.Builder()
                .url(gotenbergConfig.getBaseUrl() + "/forms/pdfengines/merge")
                .post(requestBodyBuilder.build())
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("合并失败: " + response.code() + " " + response.message());
            }
            return response.body().bytes();
        }
    }

    /**
     * 拆分 PDF 文件
     * @param file PDF文件
     * @param splitMode splitMode: intervals 或 pages
     * @param splitSpan 间隔数或页码
     */
    public byte[] splitPdf(MultipartFile file, String splitMode, String splitSpan) throws IOException {
        return splitPdf(file, splitMode, splitSpan, false);
    }

    public byte[] splitPdf(MultipartFile file, String splitMode, String splitSpan, boolean splitUnify) throws IOException {
        System.out.println("=== GotenbergService.splitPdf ===");
        System.out.println("文件名: " + file.getOriginalFilename());
        System.out.println("文件大小: " + file.getSize() + " bytes");
        System.out.println("Content-Type: " + file.getContentType());
        System.out.println("拆分模式: " + splitMode);
        System.out.println("间隔数/页码: " + splitSpan);
        System.out.println("合并模式: " + splitUnify);
        
        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("files", file.getOriginalFilename(),
                        RequestBody.create(file.getBytes(), MediaType.parse("application/pdf")))
                .addFormDataPart("splitMode", splitMode);
        
        // 根据模式添加 splitSpan
        if ("intervals".equals(splitMode)) {
            // intervals 模式：splitSpan 是间隔数
            if (splitSpan != null && !splitSpan.isEmpty()) {
                builder.addFormDataPart("splitSpan", splitSpan);
            } else {
                builder.addFormDataPart("splitSpan", "3"); // 默认间隔数
            }
        } else if ("pages".equals(splitMode)) {
            // pages 模式：splitSpan 是页码范围，"1-" 表示所有页面
            builder.addFormDataPart("splitSpan", "1-");
        }
        
        // 总是传递 splitUnify 参数
        builder.addFormDataPart("splitUnify", String.valueOf(splitUnify));

        Request request = new Request.Builder()
                .url(gotenbergConfig.getBaseUrl() + "/forms/pdfengines/split")
                .post(builder.build())
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            System.out.println("Gotenberg 响应码: " + response.code());
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "";
                System.out.println("Gotenberg 错误: " + errorBody);
                throw new IOException("拆分失败: " + response.code() + " " + response.message());
            }
            return response.body().bytes();
        }
    }

    /**
     * 扁平化 PDF (将注解合并到页面)
     */
    public byte[] flattenPdf(MultipartFile file) throws IOException {
        MultipartBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("files", file.getOriginalFilename(),
                        RequestBody.create(file.getBytes(), MediaType.parse("application/pdf")))
                .build();

        Request request = new Request.Builder()
                .url(gotenbergConfig.getBaseUrl() + "/forms/pdfengines/flatten")
                .post(requestBody)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("扁平化失败: " + response.code() + " " + response.message());
            }
            return response.body().bytes();
        }
    }

    /**
     * 转换 PDF 格式
     * @param file PDF文件
     * @param pdfa PDF/A 格式 (如: 1a, 1b, 2a, 2b, 3a, 3b, 3u)
     * @param pdfua 是否转换为 PDF/UA
     */
    public byte[] convertPdfFormat(MultipartFile file, String pdfa, boolean pdfua) throws IOException {
        MultipartBody.Builder requestBodyBuilder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("files", file.getOriginalFilename(),
                        RequestBody.create(file.getBytes(), MediaType.parse("application/pdf")));

        if (pdfa != null && !pdfa.isEmpty()) {
            requestBodyBuilder.addFormDataPart("pdfa", pdfa);
        }
        if (pdfua) {
            requestBodyBuilder.addFormDataPart("pdfua", "true");
        }

        Request request = new Request.Builder()
                .url(gotenbergConfig.getBaseUrl() + "/forms/pdfengines/convert")
                .post(requestBodyBuilder.build())
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("格式转换失败: " + response.code() + " " + response.message());
            }
            return response.body().bytes();
        }
    }

    /**
     * 读取 PDF 元数据
     */
    public String readPdfMetadata(MultipartFile file) throws IOException {
        MultipartBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("files", file.getOriginalFilename(),
                        RequestBody.create(file.getBytes(), MediaType.parse("application/pdf")))
                .build();

        Request request = new Request.Builder()
                .url(gotenbergConfig.getBaseUrl() + "/forms/pdfengines/metadata/read")
                .post(requestBody)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("读取元数据失败: " + response.code() + " " + response.message());
            }
            return response.body().string();
        }
    }

    /**
     * 写入 PDF 元数据
     */
    public byte[] writePdfMetadata(MultipartFile file, String metadataJson) throws IOException {
        MultipartBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("files", file.getOriginalFilename(),
                        RequestBody.create(file.getBytes(), MediaType.parse("application/pdf")))
                .addFormDataPart("metadata", metadataJson)
                .build();

        Request request = new Request.Builder()
                .url(gotenbergConfig.getBaseUrl() + "/forms/pdfengines/metadata/write")
                .post(requestBody)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("写入元数据失败: " + response.code() + " " + response.message());
            }
            return response.body().bytes();
        }
    }

    /**
     * 读取 PDF 书签
     */
    public String readPdfBookmarks(MultipartFile file) throws IOException {
        MultipartBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("files", file.getOriginalFilename(),
                        RequestBody.create(file.getBytes(), MediaType.parse("application/pdf")))
                .build();

        Request request = new Request.Builder()
                .url(gotenbergConfig.getBaseUrl() + "/forms/pdfengines/bookmarks/read")
                .post(requestBody)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("读取书签失败: " + response.code() + " " + response.message());
            }
            return response.body().string();
        }
    }

    /**
     * 写入 PDF 书签
     */
    public byte[] writePdfBookmarks(MultipartFile file, String bookmarksJson) throws IOException {
        MultipartBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("files", file.getOriginalFilename(),
                        RequestBody.create(file.getBytes(), MediaType.parse("application/pdf")))
                .addFormDataPart("bookmarks", bookmarksJson)
                .build();

        Request request = new Request.Builder()
                .url(gotenbergConfig.getBaseUrl() + "/forms/pdfengines/bookmarks/write")
                .post(requestBody)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("写入书签失败: " + response.code() + " " + response.message());
            }
            return response.body().bytes();
        }
    }

    /**
     * 加密 PDF
     */
    public byte[] encryptPdf(MultipartFile file, String userPassword, String ownerPassword) throws IOException {
        MultipartBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("files", file.getOriginalFilename(),
                        RequestBody.create(file.getBytes(), MediaType.parse("application/pdf")))
                .addFormDataPart("userPassword", userPassword != null ? userPassword : "")
                .addFormDataPart("ownerPassword", ownerPassword != null ? ownerPassword : "")
                .build();

        Request request = new Request.Builder()
                .url(gotenbergConfig.getBaseUrl() + "/forms/pdfengines/encrypt")
                .post(requestBody)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("加密失败: " + response.code() + " " + response.message());
            }
            return response.body().bytes();
        }
    }

    /**
     * 向 PDF 嵌入文件
     */
    public byte[] embedFiles(MultipartFile pdfFile, MultipartFile[] embedFiles) throws IOException {
        MultipartBody.Builder requestBodyBuilder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("files", pdfFile.getOriginalFilename(),
                        RequestBody.create(pdfFile.getBytes(), MediaType.parse("application/pdf")));

        for (MultipartFile embedFile : embedFiles) {
            requestBodyBuilder.addFormDataPart("embeds", embedFile.getOriginalFilename(),
                    RequestBody.create(embedFile.getBytes(), MediaType.parse("application/octet-stream")));
        }

        Request request = new Request.Builder()
                .url(gotenbergConfig.getBaseUrl() + "/forms/pdfengines/embed")
                .post(requestBodyBuilder.build())
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("嵌入文件失败: " + response.code() + " " + response.message());
            }
            return response.body().bytes();
        }
    }

    /**
     * 添加水印
     * @param source watermarkSource: text, image, pdf
     * @param expression 水印内容/图片路径/PDF路径
     * @param pages 页码范围 (如: 1-3, 5, 7-10)
     */
    public byte[] addWatermark(MultipartFile file, String source, String expression, String pages) throws IOException {
        return addWatermarkOrStamp(file, source, expression, pages, "watermark");
    }

    /**
     * 添加印章
     */
    public byte[] addStamp(MultipartFile file, String source, String expression, String pages) throws IOException {
        return addWatermarkOrStamp(file, source, expression, pages, "stamp");
    }

    private byte[] addWatermarkOrStamp(MultipartFile file, String source, String expression, String pages, String prefix) throws IOException {
        MultipartBody.Builder requestBodyBuilder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("files", file.getOriginalFilename(),
                        RequestBody.create(file.getBytes(), MediaType.parse("application/pdf")))
                .addFormDataPart(prefix + "Source", source)
                .addFormDataPart(prefix + "Expression", expression != null ? expression : "")
                .addFormDataPart(prefix + "Pages", pages != null ? pages : "");

        Request request = new Request.Builder()
                .url(gotenbergConfig.getBaseUrl() + "/forms/pdfengines/" + prefix)
                .post(requestBodyBuilder.build())
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("添加" + (prefix.equals("watermark") ? "水印" : "印章") + "失败: " + response.code() + " " + response.message());
            }
            return response.body().bytes();
        }
    }

    /**
     * 旋转 PDF 页面
     * @param angle 旋转角度: 90, 180, 270
     * @param pages 页码范围
     */
    public byte[] rotatePdf(MultipartFile file, int angle, String pages) throws IOException {
        MultipartBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("files", file.getOriginalFilename(),
                        RequestBody.create(file.getBytes(), MediaType.parse("application/pdf")))
                .addFormDataPart("rotateAngle", String.valueOf(angle))
                .addFormDataPart("rotatePages", pages != null ? pages : "")
                .build();

        Request request = new Request.Builder()
                .url(gotenbergConfig.getBaseUrl() + "/forms/pdfengines/rotate")
                .post(requestBody)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("旋转失败: " + response.code() + " " + response.message());
            }
            return response.body().bytes();
        }
    }

    // ==================== Chromium HTML 转换 ====================

    /**
     * 统一转换为 PDF
     * @param sourceType 来源类型: url, html, markdown
     * @param url URL (当 sourceType=url 时必填)
     * @param htmlFile HTML文件 (当 sourceType=html 或 markdown 时必填)
     * @param extraFiles 额外文件 (assets 或 markdown files)
     */
    public byte[] convertToPdf(String sourceType, String url, MultipartFile htmlFile, MultipartFile[] extraFiles) throws IOException {
        Request request;
        String endpoint;

        switch (sourceType.toLowerCase()) {
            case "url":
                if (url == null || url.isEmpty()) {
                    throw new IOException("URL转PDF需要url参数");
                }
                MultipartBody urlBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("url", url)
                        .build();
                request = new Request.Builder()
                        .url(gotenbergConfig.getBaseUrl() + "/forms/chromium/convert/url")
                        .post(urlBody)
                        .build();
                break;

            case "html":
                if (htmlFile == null) {
                    throw new IOException("HTML转PDF需要htmlFile参数");
                }
                MultipartBody.Builder htmlBuilder = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("index.html", "index.html",
                                RequestBody.create(htmlFile.getBytes(), MediaType.parse("text/html")));
                if (extraFiles != null) {
                    for (MultipartFile asset : extraFiles) {
                        htmlBuilder.addFormDataPart("assets", asset.getOriginalFilename(),
                                RequestBody.create(asset.getBytes(), MediaType.parse("application/octet-stream")));
                    }
                }
                request = new Request.Builder()
                        .url(gotenbergConfig.getBaseUrl() + "/forms/chromium/convert/html")
                        .post(htmlBuilder.build())
                        .build();
                break;

            case "markdown":
                if (htmlFile == null || extraFiles == null || extraFiles.length == 0) {
                    throw new IOException("Markdown转PDF需要htmlFile和markdownFiles参数");
                }
                MultipartBody.Builder mdBuilder = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("index.html", "index.html",
                                RequestBody.create(htmlFile.getBytes(), MediaType.parse("text/html")));
                for (MultipartFile mdFile : extraFiles) {
                    mdBuilder.addFormDataPart("files", mdFile.getOriginalFilename(),
                            RequestBody.create(mdFile.getBytes(), MediaType.parse("text/markdown")));
                }
                request = new Request.Builder()
                        .url(gotenbergConfig.getBaseUrl() + "/forms/chromium/convert/markdown")
                        .post(mdBuilder.build())
                        .build();
                break;

            default:
                throw new IOException("不支持的转换类型: " + sourceType + ", 仅支持 url, html, markdown");
        }

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException(sourceType + "转PDF失败: " + response.code() + " " + response.message());
            }
            return response.body().bytes();
        }
    }

    // ==================== Chromium 截图 ====================

    /**
     * 统一截图
     * @param sourceType 来源类型: url, html, markdown
     * @param url URL (sourceType=url 时必填)
     * @param htmlFile HTML文件 (sourceType=html 或 markdown 时必填)
     * @param extraFiles markdown文件 (sourceType=markdown 时必填)
     * @param format 图片格式: png, jpeg, webp
     */
    public byte[] screenshot(String sourceType, String url, MultipartFile htmlFile, MultipartFile[] extraFiles, String format) throws IOException {
        Request request;
        String contentType = "image/" + (format != null && format.equals("jpeg") ? "jpeg" : (format != null ? format : "png"));
        String effectiveFormat = format != null ? format : "png";

        switch (sourceType.toLowerCase()) {
            case "url":
                if (url == null || url.isEmpty()) {
                    throw new IOException("URL截图需要url参数");
                }
                MultipartBody urlBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("url", url)
                        .addFormDataPart("format", effectiveFormat)
                        .build();
                request = new Request.Builder()
                        .url(gotenbergConfig.getBaseUrl() + "/forms/chromium/screenshot/url")
                        .post(urlBody)
                        .build();
                break;

            case "html":
                if (htmlFile == null) {
                    throw new IOException("HTML截图需要htmlFile参数");
                }
                MultipartBody htmlBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("index.html", "index.html",
                                RequestBody.create(htmlFile.getBytes(), MediaType.parse("text/html")))
                        .addFormDataPart("format", effectiveFormat)
                        .build();
                request = new Request.Builder()
                        .url(gotenbergConfig.getBaseUrl() + "/forms/chromium/screenshot/html")
                        .post(htmlBody)
                        .build();
                break;

            case "markdown":
                if (htmlFile == null || extraFiles == null || extraFiles.length == 0) {
                    throw new IOException("Markdown截图需要htmlFile和markdownFiles参数");
                }
                MultipartBody.Builder mdBuilder = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("index.html", "index.html",
                                RequestBody.create(htmlFile.getBytes(), MediaType.parse("text/html")))
                        .addFormDataPart("format", effectiveFormat);
                for (MultipartFile mdFile : extraFiles) {
                    mdBuilder.addFormDataPart("files", mdFile.getOriginalFilename(),
                            RequestBody.create(mdFile.getBytes(), MediaType.parse("text/markdown")));
                }
                request = new Request.Builder()
                        .url(gotenbergConfig.getBaseUrl() + "/forms/chromium/screenshot/markdown")
                        .post(mdBuilder.build())
                        .build();
                break;

            default:
                throw new IOException("不支持的截图类型: " + sourceType + ", 仅支持 url, html, markdown");
        }

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException(sourceType + "截图失败: " + response.code() + " " + response.message());
            }
            return response.body().bytes();
        }
    }

    // ==================== PDF 转 Office ====================

    /**
     * PDF 转 Word/Excel/PPT
     * @param file PDF文件
     * @param targetFormat 目标格式: docx, xlsx, pptx
     */
    public byte[] convertPdfToOffice(MultipartFile file, String targetFormat) throws IOException {
        MultipartBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("files", file.getOriginalFilename(),
                        RequestBody.create(file.getBytes(), MediaType.parse("application/pdf")))
                .addFormDataPart("targetFormat", targetFormat != null ? targetFormat : "docx")
                .build();

        Request request = new Request.Builder()
                .url(gotenbergConfig.getBaseUrl() + "/forms/libreoffice/convert")
                .post(requestBody)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("PDF转" + targetFormat + "失败: " + response.code() + " " + response.message());
            }
            return response.body().bytes();
        }
    }

    // ==================== 系统 API ====================

    /**
     * 健康检查
     */
    public String health() throws IOException {
        Request request = new Request.Builder()
                .url(gotenbergConfig.getBaseUrl() + "/health")
                .get()
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            return response.body().string();
        }
    }

    /**
     * 获取版本
     */
    public String version() throws IOException {
        Request request = new Request.Builder()
                .url(gotenbergConfig.getBaseUrl() + "/version")
                .get()
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            return response.body().string();
        }
    }
}