package com.slow.excel_tools_backend.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.slow.excel_tools_backend.common.Result;
import com.slow.excel_tools_backend.entity.Banner;
import com.slow.excel_tools_backend.service.BannerService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpHeaders;

import java.util.List;

/**
 * 轮播图管理控制器（后台）
 */
@Api(tags = "轮播图管理（后台）")
@RestController
@RequestMapping("/api/admin/banner")
public class AdminBannerController {

    private final BannerService bannerService;

    public AdminBannerController(BannerService bannerService) {
        this.bannerService = bannerService;
    }

    @ApiOperation("获取轮播图列表")
    @GetMapping
    public Result<IPage<Banner>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.ok(bannerService.listPage(page, size));
    }

    @ApiOperation("获取启用的轮播图（小程序用）")
    @GetMapping("/active")
    public Result<List<Banner>> activeList() {
        return Result.ok(bannerService.listActive());
    }

    @ApiOperation("获取轮播图详情")
    @GetMapping("/{id}")
    public Result<Banner> detail(@ApiParam("轮播图ID") @PathVariable Long id) {
        return Result.ok(bannerService.getById(id));
    }

    @ApiOperation("创建轮播图")
    @PostMapping
    public Result<Banner> create(@RequestBody Banner banner) {
        return Result.ok(bannerService.create(banner));
    }

    @ApiOperation("更新轮播图")
    @PutMapping("/{id}")
    public Result<Banner> update(
            @ApiParam("轮播图ID") @PathVariable Long id,
            @RequestBody Banner banner) {
        banner.setId(id);
        return Result.ok(bannerService.update(banner));
    }

    @ApiOperation("删除轮播图")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@ApiParam("轮播图ID") @PathVariable Long id) {
        bannerService.deleteById(id);
        return Result.ok();
    }
}