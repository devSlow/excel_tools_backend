package com.slow.excel_tools_backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.slow.excel_tools_backend.entity.Notice;

/**
 * 公告服务接口
 */
public interface NoticeService {

    java.util.List<Notice> list(LambdaQueryWrapper<Notice> wrapper);

    IPage<Notice> listPage(int page, int size);

    Notice getById(Long id);

    Notice create(Notice notice);

    Notice update(Notice notice);

    void deleteById(Long id);

    long countAll();
}