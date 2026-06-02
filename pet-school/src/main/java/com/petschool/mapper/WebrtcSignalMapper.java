package com.petschool.mapper;

import com.petschool.entity.WebrtcSignal;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface WebrtcSignalMapper {
    WebrtcSignal selectByLiveId(@Param("liveId") Long liveId);
    int insert(WebrtcSignal signal);
    int updateOffer(WebrtcSignal signal);
    int updateAnswer(WebrtcSignal signal);
    int updateSenderCandidates(WebrtcSignal signal);
    int updateReceiverCandidates(WebrtcSignal signal);
    int deleteByLiveId(@Param("liveId") Long liveId);
}
