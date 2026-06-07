package com.petschool.mapper;

import com.petschool.entity.Wallet;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface WalletMapper {
    Wallet selectByUserId(@Param("userId") Long userId);
    Wallet selectById(@Param("id") Long id);
    int insert(Wallet wallet);
    int updateBalance(@Param("userId") Long userId, @Param("balance") BigDecimal balance, @Param("totalRecharge") BigDecimal totalRecharge, @Param("totalConsume") BigDecimal totalConsume);
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);
    List<Wallet> selectAll();
    List<Wallet> selectPage(@Param("keyword") String keyword, @Param("offset") int offset, @Param("limit") int limit);
    int selectPageCount(@Param("keyword") String keyword);
}
