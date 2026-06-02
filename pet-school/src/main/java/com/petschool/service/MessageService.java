package com.petschool.service;

import com.petschool.entity.Message;
import java.util.List;

public interface MessageService {
    int send(Message message);
    List<Message> getByUserId(Long userId);
    int markRead(Long id);
}
