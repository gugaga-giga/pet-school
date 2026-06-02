package com.petschool.controller;

import com.petschool.entity.WebrtcSignal;
import com.petschool.service.WebrtcSignalService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/webrtc")
public class WebrtcSignalController {

    private final WebrtcSignalService webrtcSignalService;

    public WebrtcSignalController(WebrtcSignalService webrtcSignalService) {
        this.webrtcSignalService = webrtcSignalService;
    }

    @GetMapping("/signal/{liveId}")
    public Map<String, Object> getSignal(@PathVariable Long liveId) {
        Map<String, Object> result = new HashMap<>();
        WebrtcSignal signal = webrtcSignalService.getByLiveId(liveId);
        if (signal != null) {
            result.put("code", 200);
            result.put("data", signal);
        } else {
            result.put("code", 404);
            result.put("message", "信令不存在");
        }
        return result;
    }

    @PostMapping("/offer/{liveId}")
    public Map<String, Object> saveOffer(@PathVariable Long liveId, @RequestBody Map<String, String> body) {
        Map<String, Object> result = new HashMap<>();
        String offerSdp = body.get("offerSdp");
        String senderCandidates = body.get("senderCandidates");
        int rows = webrtcSignalService.saveOffer(liveId, offerSdp, senderCandidates);
        result.put("code", rows > 0 ? 200 : 500);
        result.put("message", rows > 0 ? "Offer已保存" : "保存失败");
        return result;
    }

    @PostMapping("/answer/{liveId}")
    public Map<String, Object> saveAnswer(@PathVariable Long liveId, @RequestBody Map<String, String> body) {
        Map<String, Object> result = new HashMap<>();
        String answerSdp = body.get("answerSdp");
        String receiverCandidates = body.get("receiverCandidates");
        int rows = webrtcSignalService.saveAnswer(liveId, answerSdp, receiverCandidates);
        result.put("code", rows > 0 ? 200 : 500);
        result.put("message", rows > 0 ? "Answer已保存" : "保存失败");
        return result;
    }

    @PostMapping("/candidates/sender/{liveId}")
    public Map<String, Object> updateSenderCandidates(@PathVariable Long liveId, @RequestBody Map<String, String> body) {
        Map<String, Object> result = new HashMap<>();
        int rows = webrtcSignalService.updateSenderCandidates(liveId, body.get("senderCandidates"));
        result.put("code", rows > 0 ? 200 : 500);
        return result;
    }

    @PostMapping("/candidates/receiver/{liveId}")
    public Map<String, Object> updateReceiverCandidates(@PathVariable Long liveId, @RequestBody Map<String, String> body) {
        Map<String, Object> result = new HashMap<>();
        int rows = webrtcSignalService.updateReceiverCandidates(liveId, body.get("receiverCandidates"));
        result.put("code", rows > 0 ? 200 : 500);
        return result;
    }

    @DeleteMapping("/signal/{liveId}")
    public Map<String, Object> deleteSignal(@PathVariable Long liveId) {
        Map<String, Object> result = new HashMap<>();
        webrtcSignalService.deleteByLiveId(liveId);
        result.put("code", 200);
        result.put("message", "信令已清除");
        return result;
    }
}
