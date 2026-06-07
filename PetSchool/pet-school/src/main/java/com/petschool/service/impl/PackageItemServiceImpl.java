package com.petschool.service.impl;

import com.petschool.entity.PackageItem;
import com.petschool.mapper.PackageItemMapper;
import com.petschool.service.PackageItemService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PackageItemServiceImpl implements PackageItemService {

    private final PackageItemMapper packageItemMapper;

    public PackageItemServiceImpl(PackageItemMapper packageItemMapper) {
        this.packageItemMapper = packageItemMapper;
    }

    @Override
    public List<PackageItem> getByPackageId(Long packageId) {
        return packageItemMapper.selectByPackageId(packageId);
    }

    @Override
    public int add(PackageItem packageItem) {
        return packageItemMapper.insert(packageItem);
    }

    @Override
    public int deleteById(Long id) {
        return packageItemMapper.deleteById(id);
    }

    @Override
    public int deleteByPackageId(Long packageId) {
        return packageItemMapper.deleteByPackageId(packageId);
    }
}
