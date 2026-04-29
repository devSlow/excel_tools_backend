package com.slow.excel_tools_backend.service;

import com.slow.excel_tools_backend.entity.AppConfig;

public interface AppConfigService {

    AppConfig getByKey(String key);

    String getValue(String key);

    void setValue(String key, String value, String updateContent, String updateImage, String remark);
}
