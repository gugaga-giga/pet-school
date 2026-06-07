package com.petschool.mapper;

import com.petschool.entity.Certificate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CertificateMapper {
    int insert(Certificate cert);
    Certificate selectById(@Param("id") Long id);
    Certificate selectByCertificateNo(@Param("certificateNo") String certificateNo);
    List<Certificate> selectByUserId(@Param("userId") Long userId);
    Certificate selectByOrderId(@Param("orderId") Long orderId);
    List<Certificate> selectPage(@Param("keyword") String keyword, @Param("status") Integer status, @Param("offset") int offset, @Param("pageSize") int pageSize);
    int selectPageCount(@Param("keyword") String keyword, @Param("status") Integer status);
    List<Certificate> selectAll();
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);
    int update(Certificate cert);
    int deleteById(@Param("id") Long id);
    int deleteBatch(@Param("ids") List<Long> ids);
}
