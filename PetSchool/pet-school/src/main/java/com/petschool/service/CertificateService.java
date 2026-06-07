package com.petschool.service;

import com.petschool.entity.Certificate;

import java.util.List;
import java.util.Map;

public interface CertificateService {
    Certificate generateFromOrder(Long orderId);
    Certificate generateTest(String petName, String ownerName, String courseName);
    Certificate regenerate(Long id);
    Certificate getById(Long id);
    Certificate getByCertificateNo(String certificateNo);
    List<Certificate> getByUserId(Long userId);
    Map<String, Object> page(String keyword, Integer status, int page, int pageSize);
    List<Certificate> listAll();
    int updateStatus(Long id, Integer status);
    int deleteById(Long id);
    int deleteBatch(List<Long> ids);
    String getCertificateFilePath(Long id);
}
