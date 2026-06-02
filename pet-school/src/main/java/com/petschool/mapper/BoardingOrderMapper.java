package com.petschool.mapper;

import com.petschool.entity.BoardingOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BoardingOrderMapper {
    int insert(BoardingOrder order);
    BoardingOrder selectById(@Param("id") Long id);
    List<BoardingOrder> selectByUserId(@Param("userId") Long userId);
    List<BoardingOrder> selectPage(@Param("keyword") String keyword, @Param("status") Integer status, @Param("offset") int offset, @Param("pageSize") int pageSize);
    int selectPageCount(@Param("keyword") String keyword, @Param("status") Integer status);
    List<BoardingOrder> selectAll();
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);
    int deleteById(@Param("id") Long id);
    int deleteBatch(@Param("ids") List<Long> ids);
}
