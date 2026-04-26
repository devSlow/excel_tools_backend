package com.slow.excel_tools_backend.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.slow.excel_tools_backend.common.Result;
import com.slow.excel_tools_backend.entity.Notice;
import com.slow.excel_tools_backend.service.NoticeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.*;

/**
 * 公告管理控制器（后台）
 */
@Api(tags = "公告管理（后台）")
@RestController
@RequestMapping("/api/admin/notice")
public class AdminNoticeController {

    private final NoticeService noticeService;

    public AdminNoticeController(NoticeService noticeService) {
        this.noticeService = noticeService;
    }

    @ApiOperation("获取公告列表")
    @GetMapping
    public Result<IPage<Notice>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.ok(noticeService.listPage(page, size));
    }

    @ApiOperation("获取公告详情")
    @GetMapping("/{id}")
    public Result<Notice> detail(@ApiParam("公告ID") @PathVariable Long id) {
        return Result.ok(noticeService.getById(id));
    }

    @ApiOperation("创建公告")
    @PostMapping
    public Result<Notice> create(@RequestBody Notice notice) {
        return Result.ok(noticeService.create(notice));
    }

    @ApiOperation("更新公告")
    @PutMapping("/{id}")
    public Result<Notice> update(
            @ApiParam("公告ID") @PathVariable Long id,
            @RequestBody Notice notice) {
        notice.setId(id);
        return Result.ok(noticeService.update(notice));
    }

    @ApiOperation("删除公告")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@ApiParam("公告ID") @PathVariable Long id) {
        noticeService.deleteById(id);
        return Result.ok();
    }
}