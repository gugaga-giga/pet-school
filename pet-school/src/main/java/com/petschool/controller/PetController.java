package com.petschool.controller;

import com.petschool.entity.Pet;
import com.petschool.service.PetService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/pet")
public class PetController {

    private final PetService petService;

    public PetController(PetService petService) {
        this.petService = petService;
    }

    @PostMapping("/add")
    public Map<String, Object> add(@RequestBody Pet pet) {
        Map<String, Object> result = new HashMap<>();
        int rows = petService.add(pet);
        if (rows > 0) {
            result.put("code", 200);
            result.put("message", "添加成功");
            result.put("data", pet);
        } else {
            result.put("code", 500);
            result.put("message", "添加失败");
        }
        return result;
    }

    @GetMapping("/{id}")
    public Map<String, Object> getById(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        Pet pet = petService.getById(id);
        if (pet != null) {
            result.put("code", 200);
            result.put("data", pet);
        } else {
            result.put("code", 404);
            result.put("message", "宠物不存在");
        }
        return result;
    }

    @GetMapping("/user/{userId}")
    public Map<String, Object> getByUserId(@PathVariable Long userId) {
        Map<String, Object> result = new HashMap<>();
        List<Pet> pets = petService.getByUserId(userId);
        result.put("code", 200);
        result.put("data", pets);
        return result;
    }
}
