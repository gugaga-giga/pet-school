package com.petschool.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

public class CertificateTemplate implements Serializable {
    private Long id;
    private String name;
    private String backgroundUrl;
    private String style;
    private Integer status;
    private LocalDateTime createTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getBackgroundUrl() { return backgroundUrl; }
    public void setBackgroundUrl(String backgroundUrl) { this.backgroundUrl = backgroundUrl; }
    public String getStyle() { return style; }
    public void setStyle(String style) { this.style = style; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}
