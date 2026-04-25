package com.slow.excel_tools_backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.slow.excel_tools_backend.entity.User;
import com.slow.excel_tools_backend.mapper.UserMapper;
import com.slow.excel_tools_backend.service.UserService;
import org.springframework.stereotype.Service;

/**
 * 用户服务实现类
 */
@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;

    public UserServiceImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public User loginOrRegister(String openid) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getOpenid, openid);
        User user = userMapper.selectOne(wrapper);
        if (user == null) {
            user = new User();
            user.setOpenid(openid);
            userMapper.insert(user);
        }
        return user;
    }

    @Override
    public User getById(Long id) {
        return userMapper.selectById(id);
    }

    @Override
    public IPage<User> listPage(int page, int size, String keyword) {
        Page<User> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(User::getNickname, keyword)
                   .or()
                   .like(User::getOpenid, keyword);
        }
        wrapper.orderByDesc(User::getId);
        return userMapper.selectPage(pageParam, wrapper);
    }

    @Override
    public void deleteById(Long id) {
        userMapper.deleteById(id);
    }

    @Override
    public long countAll() {
        return userMapper.selectCount(new LambdaQueryWrapper<>());
    }
}