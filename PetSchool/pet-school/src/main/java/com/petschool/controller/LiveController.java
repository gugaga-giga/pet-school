package com.petschool.controller;

import com.petschool.entity.LiveSession;
import com.petschool.service.LiveSessionService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/live")
public class LiveController {

    private final LiveSessionService liveSessionService;

    public LiveController(LiveSessionService liveSessionService) {
        this.liveSessionService = liveSessionService;
    }

    @PostMapping("/create")
    public Map<String, Object> create(@RequestBody LiveSession session) {
        Map<String, Object> result = new HashMap<>();
        int rows = liveSessionService.create(session);
        if (rows > 0) { result.put("code", 200); result.put("message", "创建成功"); result.put("data", session); }
        else { result.put("code", 500); result.put("message", "创建失败"); }
        return result;
    }

    @GetMapping("/list")
    public Map<String, Object> listAll() {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", liveSessionService.listAll());
        return result;
    }

    @PutMapping("/start/{id}")
    public Map<String, Object> start(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        liveSessionService.startLive(id);
        result.put("code", 200);
        result.put("message", "直播已开始");
        return result;
    }

    @PutMapping("/end/{id}")
    public Map<String, Object> end(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        liveSessionService.endLive(id);
        result.put("code", 200);
        result.put("message", "直播已结束");
        return result;
    }
}
