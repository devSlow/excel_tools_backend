package com.slow.excel_tools_backend.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
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

    /**
     * 分页获取轮播图列表
     */
    IPage<Banner> listPage(int page, int size);

    /**
     * 根据ID获取轮播图
     */
    Banner getById(Long id);

    /**
     * 创建轮播图
     */
    Banner create(Banner banner);

    /**
     * 更新轮播图
     */
    Banner update(Banner banner);

    /**
     * 删除轮播图
     */
    void deleteById(Long id);

    /**
     * 获取轮播图总数
     */
    long countAll();
}