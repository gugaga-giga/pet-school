package com.petschool.service.impl;

import com.petschool.entity.WebrtcSignal;
import com.petschool.mapper.WebrtcSignalMapper;
import com.petschool.service.WebrtcSignalService;
import org.springframework.stereotype.Service;

@Service
public class WebrtcSignalServiceImpl implements WebrtcSignalService {

    private final WebrtcSignalMapper webrtcSignalMapper;

    public WebrtcSignalServiceImpl(WebrtcSignalMapper webrtcSignalMapper) {
        this.webrtcSignalMapper = webrtcSignalMapper;
    }

    @Override
    public WebrtcSignal getByLiveId(Long liveId) {
        return webrtcSignalMapper.selectByLiveId(liveId);
    }

    @Override
    public int saveOffer(Long liveId, String offerSdp, String senderCandidates) {
        WebrtcSignal existing = webrtcSignalMapper.selectByLiveId(liveId);
        if (existing != null) {
            WebrtcSignal s = new WebrtcSignal();
            s.setLiveId(liveId);
            s.setOfferSdp(offerSdp);
            s.setSenderCandidates(senderCandidates);
            return webrtcSignalMapper.updateOffer(s);
        }
        WebrtcSignal s = new WebrtcSignal();
        s.setLiveId(liveId);
        s.setOfferSdp(offerSdp);
        s.setSenderCandidates(senderCandidates);
        return webrtcSignalMapper.insert(s);
    }

    @Override
    public int saveAnswer(Long liveId, String answerSdp, String receiverCandidates) {
        WebrtcSignal s = new WebrtcSignal();
        s.setLiveId(liveId);
        s.setAnswerSdp(answerSdp);
        s.setReceiverCandidates(receiverCandidates);
        return webrtcSignalMapper.updateAnswer(s);
    }

    @Override
    public int updateSenderCandidates(Long liveId, String senderCandidates) {
        WebrtcSignal s = new WebrtcSignal();
        s.setLiveId(liveId);
        s.setSenderCandidates(senderCandidates);
        return webrtcSignalMapper.updateSenderCandidates(s);
    }

    @Override
    public int updateReceiverCandidates(Long liveId, String receiverCandidates) {
        WebrtcSignal s = new WebrtcSignal();
        s.setLiveId(liveId);
        s.setReceiverCandidates(receiverCandidates);
        return webrtcSignalMapper.updateReceiverCandidates(s);
    }

    @Override
    public int deleteByLiveId(Long liveId) {
        return webrtcSignalMapper.deleteByLiveId(liveId);
    }
}
