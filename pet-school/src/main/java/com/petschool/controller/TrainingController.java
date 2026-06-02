package com.petschool.controller;

import com.petschool.entity.*;
import com.petschool.service.*;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/training")
public class TrainingController {

    private final TrainingRecordService trainingRecordService;
    private final TrainingVideoService trainingVideoService;

    public TrainingController(TrainingRecordService trainingRecordService, TrainingVideoService trainingVideoService) {
        this.trainingRecordService = trainingRecordService;
        this.trainingVideoService = trainingVideoService;
    }

    @PostMapping("/record/add")
    public Map<String, Object> addRecord(@RequestBody TrainingRecord record) {
        Map<String, Object> result = new HashMap<>();
        int rows = trainingRecordService.add(record);
        if (rows > 0) { result.put("code", 200); result.put("message", "添加成功"); result.put("data", record); }
        else { result.put("code", 500); result.put("message", "添加失败"); }
        return result;
    }

    @GetMapping("/record/pet/{petId}")
    public Map<String, Object> recordsByPet(@PathVariable Long petId) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", trainingRecordService.getByPetId(petId));
        return result;
    }

    @PostMapping("/video/add")
    public Map<String, Object> addVideo(@RequestBody TrainingVideo video) {
        Map<String, Object> result = new HashMap<>();
        int rows = trainingVideoService.add(video);
        if (rows > 0) { result.put("code", 200); result.put("message", "上传成功"); result.put("data", video); }
        else { result.put("code", 500); result.put("message", "上传失败"); }
        return result;
    }

    @GetMapping("/video/record/{recordId}")
    public Map<String, Object> videosByRecord(@PathVariable Long recordId) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", trainingVideoService.getByRecordId(recordId));
        return result;
    }
}
