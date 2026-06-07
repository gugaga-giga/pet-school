package com.petschool.service;

import com.petschool.entity.PetOrder;
import java.util.List;
import java.util.Map;

public interface PetOrderService {
    PetOrder create(Long userId, Long petId, Long packageId, Integer months, Long couponId);
    PetOrder getById(Long id);
    List<PetOrder> getByUserId(Long userId);
    int pay(Long id, Long userId);
    int cancel(Long id, Long userId);
    Map<String, Object> page(String keyword, Integer status, int page, int pageSize);
    List<PetOrder> listAll();
    int updateStatus(Long id, Integer status);
    int deleteById(Long id);
    int deleteBatch(List<Long> ids);
    void adminRefund(Long id);
}
