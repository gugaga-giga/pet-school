package com.petschool.controller;

import com.petschool.entity.*;
import com.petschool.service.*;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/health-ai")
public class HealthController {

    private final HealthRecordService healthRecordService;
    private final AiService aiService;

    public HealthController(HealthRecordService healthRecordService, AiService aiService) {
        this.healthRecordService = healthRecordService;
        this.aiService = aiService;
    }

    @PostMapping("/record/add")
    public Map<String, Object> addRecord(@RequestBody HealthRecord record) {
        Map<String, Object> result = new HashMap<>();
        int rows = healthRecordService.add(record);
        if (rows > 0) { result.put("code", 200); result.put("message", "添加成功"); result.put("data", record); }
        else { result.put("code", 500); result.put("message", "添加失败"); }
        return result;
    }

    @GetMapping("/record/pet/{petId}")
    public Map<String, Object> recordsByPet(@PathVariable Long petId) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", healthRecordService.getByPetId(petId));
        return result;
    }

    @GetMapping("/ai/warning/{petId}")
    public Map<String, Object> aiWarning(@PathVariable Long petId) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", aiService.healthWarning(petId));
        return result;
    }

    @GetMapping("/ai/recommend/{petId}")
    public Map<String, Object> aiRecommend(@PathVariable Long petId) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", aiService.courseRecommend(petId));
        return result;
    }

    @GetMapping("/ai/vaccine/{petId}")
    public Map<String, Object> aiVaccine(@PathVariable Long petId) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", aiService.vaccineReminder(petId));
        return result;
    }
}
