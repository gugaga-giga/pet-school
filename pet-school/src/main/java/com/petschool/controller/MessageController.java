package com.petschool.controller;

import com.petschool.entity.Message;
import com.petschool.service.MessageService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/message")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @GetMapping("/user/{userId}")
    public Map<String, Object> getByUser(@PathVariable Long userId) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", messageService.getByUserId(userId));
        return result;
    }

    @PostMapping("/send")
    public Map<String, Object> send(@RequestBody Message message) {
        Map<String, Object> result = new HashMap<>();
        int rows = messageService.send(message);
        if (rows > 0) { result.put("code", 200); result.put("message", "发送成功"); }
        else { result.put("code", 500); result.put("message", "发送失败"); }
        return result;
    }

    @PutMapping("/read/{id}")
    public Map<String, Object> markRead(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        messageService.markRead(id);
        result.put("code", 200);
        result.put("message", "已标记已读");
        return result;
    }
}
