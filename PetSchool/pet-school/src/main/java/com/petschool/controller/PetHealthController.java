package com.petschool.controller;

import com.petschool.entity.HealthRule;
import com.petschool.entity.PetHealthRecord;
import com.petschool.service.PetHealthService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/health")
public class PetHealthController {

    private final PetHealthService petHealthService;

    public PetHealthController(PetHealthService petHealthService) {
        this.petHealthService = petHealthService;
    }

    private Map<String, Object> ok(Object data) {
        Map<String, Object> r = new HashMap<>();
        r.put("code", 200);
        r.put("data", data);
        return r;
    }

    private Map<String, Object> ok(String msg, Object data) {
        Map<String, Object> r = new HashMap<>();
        r.put("code", 200);
        r.put("message", msg);
        r.put("data", data);
        return r;
    }

    private Map<String, Object> fail(String msg) {
        Map<String, Object> r = new HashMap<>();
        r.put("code", 500);
        r.put("message", msg);
        return r;
    }

    @GetMapping("/page")
    public Map<String, Object> page(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(required = false) Integer riskLevel,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ok(petHealthService.page(keyword, riskLevel, startDate, endDate, page, pageSize));
    }

    @GetMapping("/detail/{id}")
    public Map<String, Object> detail(@PathVariable Long id) {
        PetHealthRecord record = petHealthService.getById(id);
        return record != null ? ok(record) : fail("记录不存在");
    }

    @PostMapping("/create")
    public Map<String, Object> create(@RequestBody PetHealthRecord record) {
        try {
            PetHealthRecord created = petHealthService.create(record);
            return ok("创建成功", created);
        } catch (Exception e) {
            return fail("创建失败: " + e.getMessage());
        }
    }

    @PutMapping("/update")
    public Map<String, Object> update(@RequestBody PetHealthRecord record) {
        try {
            PetHealthRecord updated = petHealthService.update(record);
            return ok("更新成功", updated);
        } catch (Exception e) {
            return fail("更新失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/delete/{id}")
    public Map<String, Object> delete(@PathVariable Long id) {
        int rows = petHealthService.deleteById(id);
        return rows > 0 ? ok("删除成功", null) : fail("删除失败");
    }

    @GetMapping("/my")
    public Map<String, Object> myRecords(@RequestParam Long userId) {
        return ok(petHealthService.getByUserId(userId));
    }

    @GetMapping("/pet/{petId}")
    public Map<String, Object> petRecords(@PathVariable Long petId) {
        return ok(petHealthService.getByPetId(petId));
    }

    @GetMapping("/latest/{petId}")
    public Map<String, Object> latestRecord(@PathVariable Long petId) {
        PetHealthRecord record = petHealthService.getLatestByPetId(petId);
        return record != null ? ok(record) : fail("暂无体检记录");
    }

    @GetMapping("/trend/{petId}")
    public Map<String, Object> trend(@PathVariable Long petId,
                                     @RequestParam(defaultValue = "30") int limit) {
        return ok(petHealthService.getTrendByPetId(petId, limit));
    }

    @GetMapping("/rules")
    public Map<String, Object> rules() {
        return ok(petHealthService.getRules());
    }

    @GetMapping("/rules/{petType}")
    public Map<String, Object> rulesByPetType(@PathVariable String petType) {
        return ok(petHealthService.getRulesByPetType(petType));
    }

    @PostMapping("/rules")
    public Map<String, Object> createRule(@RequestBody HealthRule rule) {
        int rows = petHealthService.createRule(rule);
        return rows > 0 ? ok("规则创建成功", null) : fail("规则创建失败");
    }

    @PutMapping("/rules")
    public Map<String, Object> updateRule(@RequestBody HealthRule rule) {
        int rows = petHealthService.updateRule(rule);
        return rows > 0 ? ok("规则更新成功", null) : fail("规则更新失败");
    }

    @DeleteMapping("/rules/{id}")
    public Map<String, Object> deleteRule(@PathVariable Long id) {
        int rows = petHealthService.deleteRule(id);
        return rows > 0 ? ok("规则删除成功", null) : fail("规则删除失败");
    }
}
