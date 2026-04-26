package com.slow.excel_tools_backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.slow.excel_tools_backend.entity.Notice;
import com.slow.excel_tools_backend.mapper.NoticeMapper;
import com.slow.excel_tools_backend.service.NoticeService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NoticeServiceImpl implements NoticeService {

    private final NoticeMapper noticeMapper;

    public NoticeServiceImpl(NoticeMapper noticeMapper) {
        this.noticeMapper = noticeMapper;
    }

    @Override
    public List<Notice> list(LambdaQueryWrapper<Notice> wrapper) {
        return noticeMapper.selectList(wrapper);
    }

    @Override
    public IPage<Notice> listPage(int page, int size) {
        Page<Notice> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Notice> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Notice::getId);
        return noticeMapper.selectPage(pageParam, wrapper);
    }

    @Override
    public Notice getById(Long id) {
        return noticeMapper.selectById(id);
    }

    @Override
    public Notice create(Notice notice) {
        notice.setCreatedAt(LocalDateTime.now());
        notice.setUpdatedAt(LocalDateTime.now());
        noticeMapper.insert(notice);
        return notice;
    }

    @Override
    public Notice update(Notice notice) {
        notice.setUpdatedAt(LocalDateTime.now());
        noticeMapper.updateById(notice);
        return notice;
    }

    @Override
    public void deleteById(Long id) {
        noticeMapper.deleteById(id);
    }

    @Override
    public long countAll() {
        return noticeMapper.selectCount(new LambdaQueryWrapper<>());
    }
}