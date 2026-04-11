package com.slow.excel_tools_backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.slow.excel_tools_backend.entity.User;
import com.slow.excel_tools_backend.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;

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

    public User getById(Long id) {
        return userMapper.selectById(id);
    }
}
