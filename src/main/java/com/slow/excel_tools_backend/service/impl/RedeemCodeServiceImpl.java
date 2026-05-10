package com.slow.excel_tools_backend.service.impl;

import com.slow.excel_tools_backend.entity.RedeemCode;
import com.slow.excel_tools_backend.mapper.RedeemCodeMapper;
import com.slow.excel_tools_backend.service.RedeemCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class RedeemCodeServiceImpl implements RedeemCodeService {

    @Autowired
    private RedeemCodeMapper redeemCodeMapper;

    @Override
    public Map<String, Object> generateCodes(int count, int maxUsage, Integer expireDays) {
        List<String> plainCodes = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            String plainCode = generatePlainCode();
            String salt = UUID.randomUUID().toString().replace("-", "");
            String hashedCode = hashCode(plainCode, salt);

            RedeemCode redeemCode = new RedeemCode();
            redeemCode.setPlainCode(plainCode);
            redeemCode.setCode(hashedCode);
            redeemCode.setSalt(salt);
            redeemCode.setMaxUsage(maxUsage);
            redeemCode.setUsedCount(0);
            redeemCode.setStatus(1);
            redeemCode.setCreatedAt(LocalDateTime.now());

            if (expireDays != null && expireDays > 0) {
                redeemCode.setExpiresAt(LocalDateTime.now().plusDays(expireDays));
            }

            redeemCodeMapper.insert(redeemCode);
            plainCodes.add(plainCode);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("count", count);
        result.put("codes", plainCodes);
        result.put("maxUsage", maxUsage);
        return result;
    }

    @Override
    public Map<String, Object> verifyCode(String code) {
        Map<String, Object> result = new HashMap<>();

        if (code == null || code.trim().isEmpty()) {
            result.put("valid", false);
            result.put("message", "兑换码不能为空");
            return result;
        }

        code = code.trim();

        List<RedeemCode> allCodes = redeemCodeMapper.selectList(null);

        for (RedeemCode redeemCode : allCodes) {
            if (redeemCode.getStatus() == 0) {
                continue;
            }

            if (redeemCode.getExpiresAt() != null && LocalDateTime.now().isAfter(redeemCode.getExpiresAt())) {
                continue;
            }

            String hashedInput = hashCode(code, redeemCode.getSalt());
            if (hashedInput.equals(redeemCode.getCode())) {
                int remaining = redeemCode.getMaxUsage() - redeemCode.getUsedCount();
                if (remaining <= 0) {
                    result.put("valid", false);
                    result.put("message", "兑换码已使用完毕");
                    return result;
                }

                redeemCode.setUsedCount(redeemCode.getUsedCount() + 1);
                redeemCodeMapper.updateById(redeemCode);

                result.put("valid", true);
                result.put("remaining", remaining - 1);
                result.put("message", "兑换成功");
                return result;
            }
        }

        result.put("valid", false);
        result.put("message", "无效的兑换码");
        return result;
    }

    @Override
    public Integer getRemainingUsage(String code) {
        if (code == null || code.trim().isEmpty()) {
            return 0;
        }

        code = code.trim();
        List<RedeemCode> allCodes = redeemCodeMapper.selectList(null);

        for (RedeemCode redeemCode : allCodes) {
            if (redeemCode.getStatus() == 0) {
                continue;
            }

            if (redeemCode.getExpiresAt() != null && LocalDateTime.now().isAfter(redeemCode.getExpiresAt())) {
                continue;
            }

            String hashedInput = hashCode(code, redeemCode.getSalt());
            if (hashedInput.equals(redeemCode.getCode())) {
                return redeemCode.getMaxUsage() - redeemCode.getUsedCount();
            }
        }

        return 0;
    }

    @Override
    public Map<String, Object> getAvailableCode() {
        Map<String, Object> result = new HashMap<>();

        List<RedeemCode> allCodes = redeemCodeMapper.selectList(null);

        for (RedeemCode redeemCode : allCodes) {
            if (redeemCode.getStatus() == 0) {
                continue;
            }

            if (redeemCode.getExpiresAt() != null && LocalDateTime.now().isAfter(redeemCode.getExpiresAt())) {
                continue;
            }

            int remaining = redeemCode.getMaxUsage() - redeemCode.getUsedCount();
            if (remaining > 0) {
                result.put("hasCode", true);
                result.put("code", redeemCode.getPlainCode());
                result.put("remaining", remaining);
                result.put("maxUsage", redeemCode.getMaxUsage());
                return result;
            }
        }

        result.put("hasCode", false);
        result.put("message", "暂无可用兑换码");
        return result;
    }

    private String generatePlainCode() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 12; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    private String hashCode(String plainCode, String salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String input = plainCode + salt;
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }
}