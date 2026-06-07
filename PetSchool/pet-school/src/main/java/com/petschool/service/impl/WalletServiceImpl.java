package com.petschool.service.impl;

import com.petschool.entity.Wallet;
import com.petschool.entity.WalletRecord;
import com.petschool.mapper.WalletMapper;
import com.petschool.mapper.WalletRecordMapper;
import com.petschool.service.WalletService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class WalletServiceImpl implements WalletService {

    private final WalletMapper walletMapper;
    private final WalletRecordMapper walletRecordMapper;
    private static final AtomicInteger seq = new AtomicInteger(0);

    public WalletServiceImpl(WalletMapper walletMapper, WalletRecordMapper walletRecordMapper) {
        this.walletMapper = walletMapper;
        this.walletRecordMapper = walletRecordMapper;
    }

    @Override
    public Wallet getByUserId(Long userId) {
        return walletMapper.selectByUserId(userId);
    }

    @Override
    @Transactional
    public Wallet ensureWallet(Long userId) {
        Wallet wallet = walletMapper.selectByUserId(userId);
        if (wallet == null) {
            wallet = new Wallet();
            wallet.setUserId(userId);
            wallet.setBalance(BigDecimal.ZERO);
            wallet.setTotalRecharge(BigDecimal.ZERO);
            wallet.setTotalConsume(BigDecimal.ZERO);
            wallet.setStatus(1);
            walletMapper.insert(wallet);
            wallet = walletMapper.selectByUserId(userId);
        }
        return wallet;
    }

    @Override
    @Transactional
    public void recharge(Long userId, BigDecimal amount, String remark) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("充值金额必须大于0");
        }
        Wallet wallet = ensureWallet(userId);
        if (wallet.getStatus() == 0) {
            throw new RuntimeException("钱包已冻结，无法充值");
        }
        BigDecimal balanceBefore = wallet.getBalance();
        BigDecimal balanceAfter = balanceBefore.add(amount);
        BigDecimal totalRecharge = wallet.getTotalRecharge().add(amount);
        walletMapper.updateBalance(userId, balanceAfter, totalRecharge, wallet.getTotalConsume());
        WalletRecord record = new WalletRecord();
        record.setUserId(userId);
        record.setType(1);
        record.setAmount(amount);
        record.setBalanceBefore(balanceBefore);
        record.setBalanceAfter(balanceAfter);
        record.setBusinessType("system");
        record.setTransactionNo(generateTransactionNo());
        record.setRemark(remark != null ? remark : "用户充值");
        walletRecordMapper.insert(record);
    }

    @Override
    @Transactional
    public void deduct(Long userId, BigDecimal amount, String businessType, Long businessId, String remark) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("支付金额必须大于0");
        }
        Wallet wallet = ensureWallet(userId);
        if (wallet.getStatus() == 0) {
            throw new RuntimeException("钱包已冻结，无法支付");
        }
        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("余额不足，请先充值");
        }
        BigDecimal balanceBefore = wallet.getBalance();
        BigDecimal balanceAfter = balanceBefore.subtract(amount);
        BigDecimal totalConsume = wallet.getTotalConsume().add(amount);
        walletMapper.updateBalance(userId, balanceAfter, wallet.getTotalRecharge(), totalConsume);
        WalletRecord record = new WalletRecord();
        record.setUserId(userId);
        record.setType(2);
        record.setAmount(amount);
        record.setBalanceBefore(balanceBefore);
        record.setBalanceAfter(balanceAfter);
        record.setBusinessType(businessType);
        record.setBusinessId(businessId);
        record.setTransactionNo(generateTransactionNo());
        record.setRemark(remark);
        walletRecordMapper.insert(record);
    }

    @Override
    @Transactional
    public void refund(Long userId, BigDecimal amount, String businessType, Long businessId, String remark) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("退款金额必须大于0");
        }
        Wallet wallet = ensureWallet(userId);
        BigDecimal balanceBefore = wallet.getBalance();
        BigDecimal balanceAfter = balanceBefore.add(amount);
        BigDecimal totalConsume = wallet.getTotalConsume().subtract(amount);
        if (totalConsume.compareTo(BigDecimal.ZERO) < 0) {
            totalConsume = BigDecimal.ZERO;
        }
        walletMapper.updateBalance(userId, balanceAfter, wallet.getTotalRecharge(), totalConsume);
        WalletRecord record = new WalletRecord();
        record.setUserId(userId);
        record.setType(3);
        record.setAmount(amount);
        record.setBalanceBefore(balanceBefore);
        record.setBalanceAfter(balanceAfter);
        record.setBusinessType(businessType);
        record.setBusinessId(businessId);
        record.setTransactionNo(generateTransactionNo());
        record.setRemark(remark);
        walletRecordMapper.insert(record);
    }

    @Override
    @Transactional
    public void adjust(Long walletId, BigDecimal amount, String remark, String operatorName) {
        Wallet wallet = walletMapper.selectById(walletId);
        if (wallet == null) {
            throw new RuntimeException("钱包不存在");
        }
        BigDecimal balanceBefore = wallet.getBalance();
        BigDecimal balanceAfter;
        BigDecimal totalRecharge = wallet.getTotalRecharge();
        BigDecimal totalConsume = wallet.getTotalConsume();
        if (amount.compareTo(BigDecimal.ZERO) > 0) {
            balanceAfter = balanceBefore.add(amount);
            totalRecharge = totalRecharge.add(amount);
        } else {
            BigDecimal absAmount = amount.abs();
            if (balanceBefore.compareTo(absAmount) < 0) {
                throw new RuntimeException("余额不足，无法扣减");
            }
            balanceAfter = balanceBefore.subtract(absAmount);
            totalConsume = totalConsume.add(absAmount);
        }
        walletMapper.updateBalance(wallet.getUserId(), balanceAfter, totalRecharge, totalConsume);
        WalletRecord record = new WalletRecord();
        record.setUserId(wallet.getUserId());
        record.setType(4);
        record.setAmount(amount.abs());
        record.setBalanceBefore(balanceBefore);
        record.setBalanceAfter(balanceAfter);
        record.setBusinessType("system");
        record.setTransactionNo(generateTransactionNo());
        record.setRemark("后台调整(" + operatorName + "): " + (remark != null ? remark : ""));
        walletRecordMapper.insert(record);
    }

    @Override
    public void updateStatus(Long walletId, Integer status) {
        walletMapper.updateStatus(walletId, status);
    }

    @Override
    public List<WalletRecord> getRecords(Long userId, Integer type, int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        return walletRecordMapper.selectByUserId(userId, type, offset, pageSize);
    }

    @Override
    public int getRecordCount(Long userId, Integer type) {
        return walletRecordMapper.selectCountByUserId(userId, type);
    }

    @Override
    public Map<String, Object> getWalletPage(String keyword, int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        List<Wallet> list = walletMapper.selectPage(keyword, offset, pageSize);
        int total = walletMapper.selectPageCount(keyword);
        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);
        return result;
    }

    @Override
    public Map<String, Object> getRecordPage(String keyword, Integer type, String businessType, int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        List<WalletRecord> list = walletRecordMapper.selectPage(keyword, type, businessType, offset, pageSize);
        int total = walletRecordMapper.selectPageCount(keyword, type, businessType);
        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);
        return result;
    }

    private String generateTransactionNo() {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int s = seq.incrementAndGet() % 10000;
        return "TXN" + date + String.format("%04d", s);
    }
}
