package com.petschool.mapper;

import com.petschool.entity.Coupon;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CouponMapper {
    int insert(Coupon coupon);
    Coupon selectById(@Param("id") Long id);
    List<Coupon> selectPage(@Param("keyword") String keyword, @Param("type") Integer type, @Param("status") Integer status, @Param("offset") int offset, @Param("pageSize") int pageSize);
    int selectPageCount(@Param("keyword") String keyword, @Param("type") Integer type, @Param("status") Integer status);
    List<Coupon> selectAll();
    List<Coupon> selectActive();
    int update(Coupon coupon);
    int updateReceiveCount(@Param("id") Long id);
    int deleteById(@Param("id") Long id);
}
