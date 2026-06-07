package com.petschool.mapper;

import com.petschool.entity.WalletRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface WalletRecordMapper {
    int insert(WalletRecord record);
    List<WalletRecord> selectByUserId(@Param("userId") Long userId, @Param("type") Integer type, @Param("offset") int offset, @Param("limit") int limit);
    int selectCountByUserId(@Param("userId") Long userId, @Param("type") Integer type);
    List<WalletRecord> selectPage(@Param("keyword") String keyword, @Param("type") Integer type, @Param("businessType") String businessType, @Param("offset") int offset, @Param("limit") int limit);
    int selectPageCount(@Param("keyword") String keyword, @Param("type") Integer type, @Param("businessType") String businessType);
    WalletRecord selectByTransactionNo(@Param("transactionNo") String transactionNo);
}
