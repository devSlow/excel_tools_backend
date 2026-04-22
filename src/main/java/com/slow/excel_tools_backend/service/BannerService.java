package com.slow.excel_tools_backend.service;

import com.slow.excel_tools_backend.entity.Banner;
import java.util.List;

/**
 * 轮播图服务接口
 */
public interface BannerService {
    /**
     * 获取启用的轮播图列表
     */
    List<Banner> listActive();
}