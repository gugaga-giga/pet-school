package com.petschool.service;

import com.petschool.entity.PackageItem;
import java.util.List;

public interface PackageItemService {
    List<PackageItem> getByPackageId(Long packageId);
    int add(PackageItem packageItem);
    int deleteById(Long id);
    int deleteByPackageId(Long packageId);
}
