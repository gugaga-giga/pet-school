package com.petschool.service.impl;

import com.petschool.entity.Message;
import com.petschool.mapper.MessageMapper;
import com.petschool.service.MessageService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MessageServiceImpl implements MessageService {

    private final MessageMapper messageMapper;

    public MessageServiceImpl(MessageMapper messageMapper) {
        this.messageMapper = messageMapper;
    }

    @Override
    public int send(Message message) {
        message.setIsRead(0);
        return messageMapper.insert(message);
    }

    @Override
    public List<Message> getByUserId(Long userId) {
        return messageMapper.selectByUserId(userId);
    }

    @Override
    public int markRead(Long id) {
        return messageMapper.updateRead(id);
    }
}
