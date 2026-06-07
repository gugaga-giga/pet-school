package com.petschool.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

public class TrainingVideo implements Serializable {
    private Long id;
    private Long recordId;
    private String videoUrl;
    private String coverUrl;
    private LocalDateTime uploadTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getRecordId() { return recordId; }
    public void setRecordId(Long recordId) { this.recordId = recordId; }
    public String getVideoUrl() { return videoUrl; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }
    public String getCoverUrl() { return coverUrl; }
    public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }
    public LocalDateTime getUploadTime() { return uploadTime; }
    public void setUploadTime(LocalDateTime uploadTime) { this.uploadTime = uploadTime; }
}
