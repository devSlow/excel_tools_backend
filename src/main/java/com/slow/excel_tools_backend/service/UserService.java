package com.slow.excel_tools_backend.service;

import com.slow.excel_tools_backend.entity.User;

public interface UserService {

    User loginOrRegister(String openid);

    User getById(Long id);
}
