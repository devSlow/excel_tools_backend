package com.slow.excel_tools_backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.slow.excel_tools_backend.entity.Banner;
import com.slow.excel_tools_backend.mapper.BannerMapper;
import com.slow.excel_tools_backend.service.BannerService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

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

    @Override
    public IPage<Banner> listPage(int page, int size) {
        Page<Banner> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Banner> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Banner::getId);
        return bannerMapper.selectPage(pageParam, wrapper);
    }

    @Override
    public Banner getById(Long id) {
        return bannerMapper.selectById(id);
    }

    @Override
    public Banner create(Banner banner) {
        banner.setCreatedAt(LocalDateTime.now());
        banner.setUpdatedAt(LocalDateTime.now());
        bannerMapper.insert(banner);
        return banner;
    }

    @Override
    public Banner update(Banner banner) {
        banner.setUpdatedAt(LocalDateTime.now());
        bannerMapper.updateById(banner);
        return banner;
    }

    @Override
    public void deleteById(Long id) {
        bannerMapper.deleteById(id);
    }

    @Override
    public long countAll() {
        return bannerMapper.selectCount(new LambdaQueryWrapper<>());
    }
}