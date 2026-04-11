package com.slow.excel_tools_backend.service;

import com.slow.excel_tools_backend.entity.User;

/**
 * 用户服务接口
 */
public interface UserService {

    /**
     * 微信登录或自动注册
     *
     * @param openid 微信 openid
     * @return 用户信息（已存在则返回，不存在则新建）
     */
    User loginOrRegister(String openid);

    /**
     * 根据ID查询用户
     *
     * @param id 用户ID
     * @return 用户信息
     */
    User getById(Long id);
}
