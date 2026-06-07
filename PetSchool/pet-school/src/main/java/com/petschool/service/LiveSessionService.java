package com.petschool.service;

import com.petschool.entity.LiveSession;
import java.util.List;

public interface LiveSessionService {
    int create(LiveSession session);
    List<LiveSession> listAll();
    int startLive(Long id);
    int endLive(Long id);
}
