package com.petschool.mapper;

import com.petschool.entity.WalletOrder;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;

@Mapper
public interface WalletOrderMapper {

    @Insert("INSERT INTO wallet_order(user_id, order_no, amount, status, create_time, remark) " +
            "VALUES(#{userId}, #{orderNo}, #{amount}, #{status}, #{createTime}, #{remark})")
    int insert(WalletOrder order);

    @Select("SELECT * FROM wallet_order WHERE order_no = #{orderNo}")
    WalletOrder selectByOrderNo(String orderNo);

    @Update("UPDATE wallet_order SET status = 1, pay_time = #{payTime} WHERE order_no = #{orderNo} AND status = 0")
    int updatePaySuccess(@Param("orderNo") String orderNo, @Param("payTime") LocalDateTime payTime);
}
