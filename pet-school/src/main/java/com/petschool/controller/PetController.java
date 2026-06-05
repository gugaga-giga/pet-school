package com.petschool.controller;

import com.petschool.entity.Pet;
import com.petschool.service.PetService;
import com.petschool.util.JwtUtil;
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

    private Map<String, Object> ok(Object data) { Map<String, Object> r = new HashMap<>(); r.put("code", 200); r.put("data", data); return r; }
    private Map<String, Object> ok(String msg, Object data) { Map<String, Object> r = new HashMap<>(); r.put("code", 200); r.put("message", msg); r.put("data", data); return r; }
    private Map<String, Object> fail(String msg) { Map<String, Object> r = new HashMap<>(); r.put("code", 500); r.put("message", msg); return r; }

    /** Get current user's pets */
    @GetMapping("/my")
    public Map<String, Object> myPets(@RequestHeader("Authorization") String auth) {
        Long userId = JwtUtil.getUserId(auth.substring(7));
        List<Pet> pets = petService.getByUserId(userId);
        return ok(pets);
    }

    /** Get pet detail with related data */
    @GetMapping("/detail/{id}")
    public Map<String, Object> detail(@PathVariable Long id) {
        Pet pet = petService.getDetailById(id);
        if (pet == null) {
            return fail("宠物不存在");
        }
        return ok(pet);
    }

    /** Create pet - userId from token */
    @PostMapping("/create")
    public Map<String, Object> create(@RequestBody Pet pet, @RequestHeader("Authorization") String auth) {
        Long userId = JwtUtil.getUserId(auth.substring(7));
        pet.setUserId(userId);
        pet.setId(null);
        int rows = petService.add(pet);
        if (rows > 0) {
            return ok("添加成功", pet);
        }
        return fail("添加失败");
    }

    /** Update pet */
    @PutMapping("/update")
    public Map<String, Object> update(@RequestBody Pet pet, @RequestHeader("Authorization") String auth) {
        Long userId = JwtUtil.getUserId(auth.substring(7));
        Pet existing = petService.getById(pet.getId());
        if (existing == null) {
            return fail("宠物不存在");
        }
        if (!existing.getUserId().equals(userId)) {
            return fail("无权修改此宠物");
        }
        pet.setUserId(null); // Prevent changing owner
        int rows = petService.update(pet);
        if (rows > 0) {
            return ok("修改成功", null);
        }
        return fail("修改失败");
    }

    /** Delete pet with protection check */
    @DeleteMapping("/delete/{id}")
    public Map<String, Object> delete(@PathVariable Long id, @RequestHeader("Authorization") String auth) {
        Long userId = JwtUtil.getUserId(auth.substring(7));
        Pet existing = petService.getById(id);
        if (existing == null) {
            return fail("宠物不存在");
        }
        if (!existing.getUserId().equals(userId)) {
            return fail("无权删除此宠物");
        }
        try {
            petService.checkCanDelete(id);
        } catch (RuntimeException e) {
            return fail(e.getMessage());
        }
        int rows = petService.deleteById(id);
        if (rows > 0) {
            return ok("删除成功", null);
        }
        return fail("删除失败");
    }

    /** Check if current user has pets (for first-login guidance) */
    @GetMapping("/has-pet")
    public Map<String, Object> hasPet(@RequestHeader("Authorization") String auth) {
        Long userId = JwtUtil.getUserId(auth.substring(7));
        int count = petService.countByUserId(userId);
        Map<String, Object> data = new HashMap<>();
        data.put("hasPet", count > 0);
        data.put("count", count);
        return ok(data);
    }

    // Legacy endpoints for backward compatibility
    @PostMapping("/add")
    public Map<String, Object> add(@RequestBody Pet pet) {
        int rows = petService.add(pet);
        if (rows > 0) {
            return ok("添加成功", pet);
        }
        return fail("添加失败");
    }

    @GetMapping("/{id}")
    public Map<String, Object> getById(@PathVariable Long id) {
        Pet pet = petService.getById(id);
        if (pet != null) {
            return ok(pet);
        }
        return fail("宠物不存在");
    }

    @GetMapping("/user/{userId}")
    public Map<String, Object> getByUserId(@PathVariable Long userId) {
        List<Pet> pets = petService.getByUserId(userId);
        return ok(pets);
    }
}
