package com.slow.excel_tools_backend.service;

import com.slow.excel_tools_backend.entity.Task;

public interface ParseService {

    Task parseText(String text);
}
