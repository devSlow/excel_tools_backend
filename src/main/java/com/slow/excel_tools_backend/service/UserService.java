package com.slow.excel_tools_backend.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
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

    /**
     * 分页查询用户列表
     *
     * @param page    页码
     * @param size   每页数量
     * @param keyword 搜索关键字
     * @return 分页结果
     */
    IPage<User> listPage(int page, int size, String keyword);

    /**
     * 删除用户
     *
     * @param id 用户ID
     */
    void deleteById(Long id);

    /**
     * 获取用户总数
     */
    long countAll();
}