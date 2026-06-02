package com.petschool.service;

import com.petschool.entity.PackageItem;
import java.util.List;

public interface PackageItemService {
    List<PackageItem> getByPackageId(Long packageId);
}
