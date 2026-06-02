package com.petschool.service.impl;

import com.petschool.entity.*;
import com.petschool.mapper.*;
import com.petschool.service.BoardingOrderService;
import com.petschool.service.CouponService;
import com.petschool.service.RoomTypeService;
import com.petschool.service.WalletService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class BoardingOrderServiceImpl implements BoardingOrderService {

    private final BoardingOrderMapper boardingOrderMapper;
    private final RoomMapper roomMapper;
    private final RoomTypeMapper roomTypeMapper;
    private final PetMapper petMapper;
    private final RoomTypeService roomTypeService;
    private final CouponService couponService;
    private final WalletService walletService;

    public BoardingOrderServiceImpl(BoardingOrderMapper boardingOrderMapper, RoomMapper roomMapper,
                                    RoomTypeMapper roomTypeMapper, PetMapper petMapper, RoomTypeService roomTypeService, CouponService couponService, WalletService walletService) {
        this.boardingOrderMapper = boardingOrderMapper;
        this.roomMapper = roomMapper;
        this.roomTypeMapper = roomTypeMapper;
        this.petMapper = petMapper;
        this.roomTypeService = roomTypeService;
        this.couponService = couponService;
        this.walletService = walletService;
    }

    @Override
    @Transactional
    public BoardingOrder create(Long userId, Long petId, Long roomId, String checkIn, String checkOut, Long couponId) {
        Room room = roomMapper.selectById(roomId);
        if (room == null) throw new RuntimeException("房间不存在");

        RoomType roomType = roomTypeMapper.selectById(room.getTypeId());
        if (roomType == null) throw new RuntimeException("房型不存在");

        Pet pet = petMapper.selectById(petId);
        if (pet == null) throw new RuntimeException("宠物不存在");
        if (!pet.getUserId().equals(userId)) throw new RuntimeException("该宠物不属于当前用户");

        LocalDate ci = LocalDate.parse(checkIn);
        LocalDate co = LocalDate.parse(checkOut);
        long days = ChronoUnit.DAYS.between(ci, co);
        if (days <= 0) throw new RuntimeException("退房日期必须晚于入住日期");

        BigDecimal originalPrice = roomType.getDailyPrice().multiply(BigDecimal.valueOf(days));

        BoardingOrder order = new BoardingOrder();
        order.setOrderNo(generateOrderNo("B"));
        order.setUserId(userId);
        order.setPetId(petId);
        order.setRoomId(roomId);
        order.setCheckIn(ci);
        order.setCheckOut(co);
        order.setTotalPrice(originalPrice);
        order.setStatus(0);
        order.setRoomTypeName(roomType.getName());
        order.setRoomNumber(room.getRoomNumber());
        order.setPetName(pet.getName());

        if (couponId != null) {
            BigDecimal discount = couponService.calculateDiscount(couponId, "boarding", originalPrice, null);
            if (discount.compareTo(BigDecimal.ZERO) > 0) {
                order.setCouponId(couponId);
                order.setCouponAmount(discount);
                order.setFinalPrice(originalPrice.subtract(discount));
            } else {
                order.setCouponId(null);
                order.setCouponAmount(BigDecimal.ZERO);
                order.setFinalPrice(originalPrice);
            }
        } else {
            order.setCouponId(null);
            order.setCouponAmount(BigDecimal.ZERO);
            order.setFinalPrice(originalPrice);
        }

        boardingOrderMapper.insert(order);

        if (couponId != null && order.getCouponAmount() != null && order.getCouponAmount().compareTo(BigDecimal.ZERO) > 0) {
            couponService.useCoupon(couponId, order.getId(), order.getCouponAmount());
        }

        return order;
    }

    @Override
    public BoardingOrder getById(Long id) {
        return boardingOrderMapper.selectById(id);
    }

    @Override
    public List<BoardingOrder> getByUserId(Long userId) {
        return boardingOrderMapper.selectByUserId(userId);
    }

    @Override
    @Transactional
    public int pay(Long id, Long userId) {
        BoardingOrder order = boardingOrderMapper.selectById(id);
        if (order == null) throw new RuntimeException("订单不存在");
        if (!order.getUserId().equals(userId)) throw new RuntimeException("无权操作此订单");
        if (order.getStatus() != 0) throw new RuntimeException("订单状态不允许支付");

        Room room = roomMapper.selectById(order.getRoomId());
        if (room != null && room.getTypeId() != null) {
            RoomType rt = roomTypeMapper.selectById(room.getTypeId());
            if (rt != null && (rt.getCapacity() == null || rt.getCapacity() <= 0)) {
                throw new RuntimeException("该房型已满，无法支付");
            }
        }

        BigDecimal payAmount = order.getFinalPrice() != null ? order.getFinalPrice() : order.getTotalPrice();
        walletService.deduct(userId, payAmount, "boarding", order.getId(), "寄宿支付-" + order.getOrderNo());

        if (room != null && room.getTypeId() != null) {
            roomTypeService.decrementCapacity(room.getTypeId());
        }

        return boardingOrderMapper.updateStatus(id, 1);
    }

    @Override
    @Transactional
    public int cancel(Long id, Long userId) {
        BoardingOrder order = boardingOrderMapper.selectById(id);
        if (order == null) throw new RuntimeException("订单不存在");
        if (!order.getUserId().equals(userId)) throw new RuntimeException("无权操作此订单");
        if (order.getStatus() != 0 && order.getStatus() != 1) throw new RuntimeException("订单状态不允许取消");

        if (order.getStatus() == 1) {
            BigDecimal payAmount = order.getFinalPrice() != null ? order.getFinalPrice() : order.getTotalPrice();
            walletService.refund(userId, payAmount, "boarding", order.getId(), "寄宿退款-" + order.getOrderNo());
            Room room = roomMapper.selectById(order.getRoomId());
            if (room != null && room.getTypeId() != null) {
                roomTypeService.incrementCapacity(room.getTypeId());
            }
        }

        if (order.getCouponId() != null) {
            couponService.returnCoupon(order.getCouponId());
        }

        return boardingOrderMapper.updateStatus(id, 2);
    }

    @Override
    public Map<String, Object> page(String keyword, Integer status, int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        List<BoardingOrder> list = boardingOrderMapper.selectPage(keyword, status, offset, pageSize);
        int total = boardingOrderMapper.selectPageCount(keyword, status);
        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);
        result.put("page", page);
        result.put("pageSize", pageSize);
        result.put("totalPages", (total + pageSize - 1) / pageSize);
        return result;
    }

    @Override
    public List<BoardingOrder> listAll() {
        return boardingOrderMapper.selectAll();
    }

    @Override
    public int updateStatus(Long id, Integer status) {
        return boardingOrderMapper.updateStatus(id, status);
    }

    @Override
    public int deleteById(Long id) {
        return boardingOrderMapper.deleteById(id);
    }

    @Override
    public int deleteBatch(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return 0;
        return boardingOrderMapper.deleteBatch(ids);
    }

    @Override
    @Transactional
    public void adminRefund(Long id) {
        BoardingOrder order = boardingOrderMapper.selectById(id);
        if (order == null) throw new RuntimeException("订单不存在");
        if (order.getStatus() != 1) throw new RuntimeException("只有已支付订单才能退款");
        BigDecimal payAmount = order.getFinalPrice() != null ? order.getFinalPrice() : order.getTotalPrice();
        walletService.refund(order.getUserId(), payAmount, "boarding", order.getId(), "管理员退款-" + order.getOrderNo());
        if (order.getCouponId() != null) {
            couponService.returnCoupon(order.getCouponId());
        }
        Room room = roomMapper.selectById(order.getRoomId());
        if (room != null && room.getTypeId() != null) {
            roomTypeService.incrementCapacity(room.getTypeId());
        }
        boardingOrderMapper.updateStatus(id, 4);
    }

    private String generateOrderNo(String prefix) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int random = ThreadLocalRandom.current().nextInt(1000, 9999);
        return prefix + timestamp + random;
    }
}
