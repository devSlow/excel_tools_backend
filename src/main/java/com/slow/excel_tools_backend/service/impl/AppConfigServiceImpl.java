package com.slow.excel_tools_backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.slow.excel_tools_backend.entity.AppConfig;
import com.slow.excel_tools_backend.mapper.AppConfigMapper;
import com.slow.excel_tools_backend.service.AppConfigService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AppConfigServiceImpl implements AppConfigService {

    private final AppConfigMapper appConfigMapper;

    public AppConfigServiceImpl(AppConfigMapper appConfigMapper) {
        this.appConfigMapper = appConfigMapper;
    }

    @Override
    public AppConfig getByKey(String key) {
        LambdaQueryWrapper<AppConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AppConfig::getConfigKey, key);
        return appConfigMapper.selectOne(wrapper);
    }

    @Override
    public String getValue(String key) {
        AppConfig config = getByKey(key);
        return config != null ? config.getConfigValue() : null;
    }

    @Override
    public void setValue(String key, String value, String updateContent, String updateImage, String remark) {
        AppConfig config = getByKey(key);
        if (config == null) {
            config = new AppConfig();
            config.setConfigKey(key);
            config.setConfigValue(value);
            config.setUpdateContent(updateContent);
            config.setUpdateImage(updateImage);
            config.setRemark(remark);
            config.setCreatedAt(LocalDateTime.now());
            config.setUpdatedAt(LocalDateTime.now());
            appConfigMapper.insert(config);
        } else {
            config.setConfigValue(value);
            if (updateContent != null) {
                config.setUpdateContent(updateContent);
            }
            if (updateImage != null) {
                config.setUpdateImage(updateImage);
            }
            if (remark != null) {
                config.setRemark(remark);
            }
            config.setUpdatedAt(LocalDateTime.now());
            appConfigMapper.updateById(config);
        }
    }
}
