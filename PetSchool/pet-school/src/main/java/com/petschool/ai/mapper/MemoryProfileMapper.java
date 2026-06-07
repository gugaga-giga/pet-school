package com.petschool.ai.mapper;

import com.petschool.ai.entity.MemoryProfile;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户记忆档案Mapper
 */
@Mapper
public interface MemoryProfileMapper {

    int insert(MemoryProfile profile);

    MemoryProfile selectById(@Param("id") Long id);

    List<MemoryProfile> selectByUserId(@Param("userId") Long userId);

    MemoryProfile selectByUserIdAndKey(@Param("userId") Long userId, @Param("key") String key);

    int update(MemoryProfile profile);

    int deleteById(@Param("id") Long id);
}
