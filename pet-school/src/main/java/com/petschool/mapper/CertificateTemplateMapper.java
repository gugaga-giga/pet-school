package com.petschool.mapper;

import com.petschool.entity.CertificateTemplate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CertificateTemplateMapper {
    int insert(CertificateTemplate tmpl);
    CertificateTemplate selectById(@Param("id") Long id);
    List<CertificateTemplate> selectAll();
    CertificateTemplate selectActive();
    int update(CertificateTemplate tmpl);
    int deleteById(@Param("id") Long id);
}
