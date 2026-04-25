package com.slow.excel_tools_backend.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.slow.excel_tools_backend.common.Result;
import com.slow.excel_tools_backend.entity.User;
import com.slow.excel_tools_backend.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.*;

/**
 * 用户管理控制器（后台）
 */
@Api(tags = "用户管理（后台）")
@RestController
@RequestMapping("/api/admin/user")
public class AdminUserController {

    private final UserService userService;

    public AdminUserController(UserService userService) {
        this.userService = userService;
    }

    @ApiOperation("获取用户列表")
    @GetMapping
    public Result<IPage<User>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        return Result.ok(userService.listPage(page, size, keyword));
    }

    @ApiOperation("获取用户详情")
    @GetMapping("/{id}")
    public Result<User> detail(@ApiParam("用户ID") @PathVariable Long id) {
        return Result.ok(userService.getById(id));
    }

    @ApiOperation("删除用户")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@ApiParam("用户ID") @PathVariable Long id) {
        userService.deleteById(id);
        return Result.ok();
    }
}