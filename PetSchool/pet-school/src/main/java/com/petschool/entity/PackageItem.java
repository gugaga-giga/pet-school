package com.petschool.entity;

import java.io.Serializable;
import java.math.BigDecimal;

public class PackageItem implements Serializable {
    private Long id;
    private Long packageId;
    private String name;
    private Integer included;
    private BigDecimal extraPrice;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getPackageId() { return packageId; }
    public void setPackageId(Long packageId) { this.packageId = packageId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getIncluded() { return included; }
    public void setIncluded(Integer included) { this.included = included; }
    public BigDecimal getExtraPrice() { return extraPrice; }
    public void setExtraPrice(BigDecimal extraPrice) { this.extraPrice = extraPrice; }
}
