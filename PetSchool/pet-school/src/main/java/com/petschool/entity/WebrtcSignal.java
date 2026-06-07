package com.petschool.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

public class WebrtcSignal implements Serializable {
    private Long id;
    private Long liveId;
    private String offerSdp;
    private String answerSdp;
    private String senderCandidates;
    private String receiverCandidates;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getLiveId() { return liveId; }
    public void setLiveId(Long liveId) { this.liveId = liveId; }
    public String getOfferSdp() { return offerSdp; }
    public void setOfferSdp(String offerSdp) { this.offerSdp = offerSdp; }
    public String getAnswerSdp() { return answerSdp; }
    public void setAnswerSdp(String answerSdp) { this.answerSdp = answerSdp; }
    public String getSenderCandidates() { return senderCandidates; }
    public void setSenderCandidates(String senderCandidates) { this.senderCandidates = senderCandidates; }
    public String getReceiverCandidates() { return receiverCandidates; }
    public void setReceiverCandidates(String receiverCandidates) { this.receiverCandidates = receiverCandidates; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
