package com.petschool.service;

import com.petschool.entity.WebrtcSignal;

public interface WebrtcSignalService {
    WebrtcSignal getByLiveId(Long liveId);
    int saveOffer(Long liveId, String offerSdp, String senderCandidates);
    int saveAnswer(Long liveId, String answerSdp, String receiverCandidates);
    int updateSenderCandidates(Long liveId, String senderCandidates);
    int updateReceiverCandidates(Long liveId, String receiverCandidates);
    int deleteByLiveId(Long liveId);
}
