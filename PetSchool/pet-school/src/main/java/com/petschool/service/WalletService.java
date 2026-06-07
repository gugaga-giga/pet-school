package com.petschool.service;

import com.petschool.entity.Wallet;
import com.petschool.entity.WalletRecord;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface WalletService {
    Wallet getByUserId(Long userId);
    Wallet ensureWallet(Long userId);
    void recharge(Long userId, BigDecimal amount, String remark);
    void deduct(Long userId, BigDecimal amount, String businessType, Long businessId, String remark);
    void refund(Long userId, BigDecimal amount, String businessType, Long businessId, String remark);
    void adjust(Long walletId, BigDecimal amount, String remark, String operatorName);
    void updateStatus(Long walletId, Integer status);
    List<WalletRecord> getRecords(Long userId, Integer type, int page, int pageSize);
    int getRecordCount(Long userId, Integer type);
    Map<String, Object> getWalletPage(String keyword, int page, int pageSize);
    Map<String, Object> getRecordPage(String keyword, Integer type, String businessType, int page, int pageSize);
}
