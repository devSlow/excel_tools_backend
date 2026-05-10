package com.slow.excel_tools_backend.service;

import java.util.Map;

public interface AigcService {

    Map<String, Object> optimizeArticle(String content, String apiUrl, String apiKey, String modelName);
}