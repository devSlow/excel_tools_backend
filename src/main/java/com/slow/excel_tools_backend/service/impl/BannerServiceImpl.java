package com.slow.excel_tools_backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.slow.excel_tools_backend.entity.Banner;
import com.slow.excel_tools_backend.mapper.BannerMapper;
import com.slow.excel_tools_backend.service.BannerService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 轮播图服务实现类
 */
@Service
public class BannerServiceImpl implements BannerService {

    private final BannerMapper bannerMapper;

    public BannerServiceImpl(BannerMapper bannerMapper) {
        this.bannerMapper = bannerMapper;
    }

    @Override
    public List<Banner> listActive() {
        LambdaQueryWrapper<Banner> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Banner::getStatus, 1)
               .orderByAsc(Banner::getSortOrder)
               .orderByDesc(Banner::getCreatedAt);
        return bannerMapper.selectList(wrapper);
    }
}