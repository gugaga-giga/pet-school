package com.petschool.mapper;

import com.petschool.entity.PackageItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PackageItemMapper {
    List<PackageItem> selectByPackageId(@Param("packageId") Long packageId);
}
