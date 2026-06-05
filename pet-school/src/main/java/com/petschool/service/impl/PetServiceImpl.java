package com.petschool.service.impl;

import com.petschool.entity.Pet;
import com.petschool.mapper.PetMapper;
import com.petschool.service.PetService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PetServiceImpl implements PetService {

    private final PetMapper petMapper;
    private final JdbcTemplate jdbcTemplate;

    public PetServiceImpl(PetMapper petMapper, JdbcTemplate jdbcTemplate) {
        this.petMapper = petMapper;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public int add(Pet pet) {
        if (pet.getStatus() == null) {
            pet.setStatus(0);
        }
        if (pet.getSterilized() == null) {
            pet.setSterilized(0);
        }
        if (pet.getGender() == null) {
            pet.setGender(1);
        }
        return petMapper.insert(pet);
    }

    @Override
    public Pet getById(Long id) {
        return petMapper.selectById(id);
    }

    @Override
    public List<Pet> getByUserId(Long userId) {
        return petMapper.selectByUserId(userId);
    }

    @Override
    public int countByUserId(Long userId) {
        return petMapper.countByUserId(userId);
    }

    @Override
    public int update(Pet pet) {
        return petMapper.update(pet);
    }

    @Override
    public int deleteById(Long id) {
        return petMapper.deleteById(id);
    }

    @Override
    public Map<String, Object> page(String keyword, String petType, Integer status, int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        List<Pet> list = petMapper.selectPage(keyword, petType, status, offset, pageSize);
        int total = petMapper.selectPageCount(keyword, petType, status);
        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);
        result.put("page", page);
        result.put("pageSize", pageSize);
        return result;
    }

    @Override
    public Pet getDetailById(Long id) {
        return petMapper.selectDetailById(id);
    }

    @Override
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        int total = petMapper.countByPetType("dog") + petMapper.countByPetType("cat")
                + petMapper.countByPetType("other") + petMapper.countByPetType(null);
        // Count all including null pet_type
        Integer allCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM pet WHERE status = 0", Integer.class);
        stats.put("total", allCount != null ? allCount : 0);
        stats.put("dogCount", petMapper.countByPetType("dog"));
        stats.put("catCount", petMapper.countByPetType("cat"));
        stats.put("todayNew", petMapper.countTodayNew());

        List<Map<String, Object>> breedStats = jdbcTemplate.queryForList(
                "SELECT breed, COUNT(*) AS count FROM pet WHERE status = 0 AND breed IS NOT NULL GROUP BY breed ORDER BY count DESC LIMIT 10");
        stats.put("hotBreeds", breedStats);

        List<Map<String, Object>> typeStats = petMapper.selectStats();
        stats.put("typeStats", typeStats);
        return stats;
    }

    @Override
    public void checkCanDelete(Long id) {
        // Check 7 related tables for business records
        String[] checks = {
                "SELECT COUNT(*) FROM pet_order WHERE pet_id = " + id,
                "SELECT COUNT(*) FROM boarding_order WHERE pet_id = " + id,
                "SELECT COUNT(*) FROM medical_order WHERE pet_id = " + id,
                "SELECT COUNT(*) FROM pet_health_record WHERE pet_id = " + id,
                "SELECT COUNT(*) FROM certificate WHERE pet_id = " + id,
                "SELECT COUNT(*) FROM vaccine_record WHERE pet_id = " + id,
                "SELECT COUNT(*) FROM deworming_record WHERE pet_id = " + id
        };
        String[] names = {"课程订单", "寄宿订单", "医疗订单", "健康记录", "毕业证书", "疫苗记录", "驱虫记录"};

        StringBuilder related = new StringBuilder();
        for (int i = 0; i < checks.length; i++) {
            try {
                Integer count = jdbcTemplate.queryForObject(checks[i], Integer.class);
                if (count != null && count > 0) {
                    if (related.length() > 0) related.append("、");
                    related.append(names[i]).append("(").append(count).append(")");
                }
            } catch (Exception ignored) {}
        }

        if (related.length() > 0) {
            throw new RuntimeException("该宠物已有业务记录：" + related + "，无法删除");
        }
    }
}
