package com.slow.excel_tools_backend.service;

import com.slow.excel_tools_backend.entity.Task;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 数据任务服务接口
 */
public interface TaskService {

    /**
     * 获取指定用户的任务列表（按创建时间倒序）
     *
     * @param userId 用户ID
     * @return 任务列表
     */
    List<Task> listByUserId(Long userId);

    /**
     * 获取任务详情（含行列数据），校验用户权限
     *
     * @param id     任务ID
     * @param userId 用户ID
     * @return 任务详情
     */
    Task getById(Long id, Long userId);

    /**
     * 创建新任务
     *
     * @param task 任务数据
     * @return 创建后的任务（含自增ID）
     */
    Task create(Task task);

    /**
     * 更新任务数据
     *
     * @param id     任务ID
     * @param userId 用户ID
     * @param update 更新内容
     * @return 更新后的任务
     */
    Task update(Long id, Long userId, Task update);

    /**
     * 删除任务
     *
     * @param id     任务ID
     * @param userId 用户ID
     */
    void delete(Long id, Long userId);

    /**
     * 数据统计
     * <p>
     * 返回总行数，以及每列按值的分类计数
     * </p>
     *
     * @param id     任务ID
     * @param userId 用户ID
     * @return 统计结果
     */
    Map<String, Object> stats(Long id, Long userId);

    /**
     * 导出 Excel 文件
     * <p>
     * 根据任务的列定义和行数据生成 .xlsx 文件，直接写入 HTTP 响应流
     * </p>
     *
     * @param id       任务ID
     * @param userId   用户ID
     * @param response HTTP 响应
     */
    void exportExcel(Long id, Long userId, HttpServletResponse response) throws IOException;
}
