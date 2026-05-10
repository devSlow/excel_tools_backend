package com.slow.excel_tools_backend.service;

import java.util.List;
import java.util.Map;

public interface RedeemCodeService {

    Map<String, Object> generateCodes(int count, int maxUsage, Integer expireDays);

    Map<String, Object> verifyCode(String code);

    Integer getRemainingUsage(String code);

    Map<String, Object> getAvailableCode();
}