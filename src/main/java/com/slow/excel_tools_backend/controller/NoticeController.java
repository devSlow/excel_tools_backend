package com.slow.excel_tools_backend.controller;

import com.slow.excel_tools_backend.common.Result;
import com.slow.excel_tools_backend.entity.Notice;
import com.slow.excel_tools_backend.service.NoticeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

/**
 * 小程序公告控制器
 */
@Api(tags = "公告")
@RestController
@RequestMapping("/api/notice")
public class NoticeController {

    private final NoticeService noticeService;

    public NoticeController(NoticeService noticeService) {
        this.noticeService = noticeService;
    }

    @ApiOperation("获取公告列表")
    @GetMapping
    public Result<java.util.List<Notice>> list() {
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Notice> wrapper = 
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.eq(Notice::getStatus, 1)
               .orderByDesc(Notice::getPriority)
               .orderByDesc(Notice::getId);
        return Result.ok(noticeService.list(wrapper));
    }

    @ApiOperation("获取公告详情")
    @GetMapping("/{id}")
    public Result<Notice> detail(@PathVariable Long id) {
        Notice notice = noticeService.getById(id);
        if (notice == null || notice.getStatus() != 1) {
            return Result.fail(404, "公告不存在或已禁用");
        }
        return Result.ok(notice);
    }
}