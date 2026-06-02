package com.petschool.service.impl;

import com.petschool.entity.LiveSession;
import com.petschool.mapper.LiveSessionMapper;
import com.petschool.service.LiveSessionService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LiveSessionServiceImpl implements LiveSessionService {

    private final LiveSessionMapper liveSessionMapper;

    public LiveSessionServiceImpl(LiveSessionMapper liveSessionMapper) {
        this.liveSessionMapper = liveSessionMapper;
    }

    @Override
    public int create(LiveSession session) {
        session.setStatus(0);
        return liveSessionMapper.insert(session);
    }

    @Override
    public List<LiveSession> listAll() {
        return liveSessionMapper.selectAll();
    }

    @Override
    public int startLive(Long id) {
        return liveSessionMapper.updateStatus(id, 1);
    }

    @Override
    public int endLive(Long id) {
        liveSessionMapper.updateEndTime(id);
        return liveSessionMapper.updateStatus(id, 2);
    }
}
