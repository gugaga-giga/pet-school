package com.petschool.mapper;

import com.petschool.entity.HealthRule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface HealthRuleMapper {
    int insert(HealthRule rule);
    HealthRule selectById(@Param("id") Long id);
    List<HealthRule> selectAll();
    List<HealthRule> selectByPetType(@Param("petType") String petType);
    int update(HealthRule rule);
    int deleteById(@Param("id") Long id);
}
