package com.petschool.service;

import com.petschool.entity.BoardingOrder;
import java.util.List;
import java.util.Map;

public interface BoardingOrderService {
    BoardingOrder create(Long userId, Long petId, Long roomId, String checkIn, String checkOut, Long couponId);
    BoardingOrder getById(Long id);
    List<BoardingOrder> getByUserId(Long userId);
    int pay(Long id, Long userId);
    int cancel(Long id, Long userId);
    Map<String, Object> page(String keyword, Integer status, int page, int pageSize);
    List<BoardingOrder> listAll();
    int updateStatus(Long id, Integer status);
    int deleteById(Long id);
    int deleteBatch(List<Long> ids);
    void adminRefund(Long id);
}
