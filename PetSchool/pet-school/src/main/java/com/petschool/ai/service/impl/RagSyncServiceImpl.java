package com.petschool.ai.service.impl;

import com.petschool.ai.config.AiConfig;
import com.petschool.ai.service.RagSyncService;
import com.petschool.entity.Course;
import com.petschool.entity.Pet;
import com.petschool.mapper.CourseMapper;
import com.petschool.mapper.PetMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * RAG数据同步服务实现，将数据库变更同步到向量库
 */
@Service
public class RagSyncServiceImpl implements RagSyncService {

    private static final Logger log = LoggerFactory.getLogger(RagSyncServiceImpl.class);

    private final AiConfig aiConfig;
    private final RestTemplate ragRestTemplate;
    private final CourseMapper courseMapper;
    private final PetMapper petMapper;

    public RagSyncServiceImpl(AiConfig aiConfig, RestTemplate ragRestTemplate,
                              CourseMapper courseMapper, PetMapper petMapper) {
        this.aiConfig = aiConfig;
        this.ragRestTemplate = ragRestTemplate;
        this.courseMapper = courseMapper;
        this.petMapper = petMapper;
    }

    @Override
    public boolean syncCourse(Long courseId) {
        try {
            Course course = courseMapper.selectById(courseId);
            if (course == null) {
                log.warn("课程不存在: courseId={}", courseId);
                return false;
            }

            // 构建文档内容
            String content = String.format("课程名称：%s\n课程描述：%s\n课程时长：%d个月\n月费：%s",
                    course.getName(),
                    course.getDescription() != null ? course.getDescription() : "暂无描述",
                    course.getDuration() != null ? course.getDuration() : 0,
                    course.getMonthlyPrice() != null ? course.getMonthlyPrice().toString() : "面议");

            // 调用FastAPI文档上传接口（通过文本内容同步）
            String url = aiConfig.getRagBaseUrl() + "/api/v1/documents/sync-text";
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("title", "课程-" + course.getName());
            requestBody.put("content", content);
            requestBody.put("knowledge_base_id", 1); // 课程知识库ID
            requestBody.put("doc_type", "course");
            requestBody.put("ref_id", courseId);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response = ragRestTemplate.exchange(url, HttpMethod.POST, entity, Map.class);
            boolean success = response.getStatusCode().is2xxSuccessful();
            if (success) {
                log.info("课程同步到RAG成功: courseId={}, name={}", courseId, course.getName());
            } else {
                log.warn("课程同步到RAG失败: courseId={}", courseId);
            }
            return success;
        } catch (Exception e) {
            log.error("课程同步到RAG异常: courseId={}, error={}", courseId, e.getMessage());
            return false;
        }
    }

    @Override
    public boolean syncPetKnowledge(Long petId) {
        try {
            Pet pet = petMapper.selectById(petId);
            if (pet == null) {
                log.warn("宠物不存在: petId={}", petId);
                return false;
            }

            // 构建宠物知识文档
            StringBuilder content = new StringBuilder();
            content.append("宠物名称：").append(pet.getName()).append("\n");
            if (pet.getBreed() != null) content.append("品种：").append(pet.getBreed()).append("\n");
            if (pet.getPetType() != null) content.append("类型：").append(pet.getPetType()).append("\n");
            if (pet.getAllergyInfo() != null && !pet.getAllergyInfo().isEmpty()) {
                content.append("过敏信息：").append(pet.getAllergyInfo()).append("\n");
            }

            String url = aiConfig.getRagBaseUrl() + "/api/v1/documents/sync-text";
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("title", "宠物-" + pet.getName());
            requestBody.put("content", content.toString());
            requestBody.put("knowledge_base_id", 2); // 宠物知识库ID
            requestBody.put("doc_type", "pet");
            requestBody.put("ref_id", petId);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response = ragRestTemplate.exchange(url, HttpMethod.POST, entity, Map.class);
            boolean success = response.getStatusCode().is2xxSuccessful();
            if (success) {
                log.info("宠物知识同步到RAG成功: petId={}, name={}", petId, pet.getName());
            } else {
                log.warn("宠物知识同步到RAG失败: petId={}", petId);
            }
            return success;
        } catch (Exception e) {
            log.error("宠物知识同步到RAG异常: petId={}, error={}", petId, e.getMessage());
            return false;
        }
    }

    @Override
    public boolean deleteFromRAG(Long docId) {
        try {
            String url = aiConfig.getRagBaseUrl() + "/api/v1/documents/" + docId;
            ResponseEntity<Map> response = ragRestTemplate.exchange(url, HttpMethod.DELETE, null, Map.class);
            boolean success = response.getStatusCode().is2xxSuccessful();
            if (success) {
                log.info("从RAG删除文档成功: docId={}", docId);
            } else {
                log.warn("从RAG删除文档失败: docId={}", docId);
            }
            return success;
        } catch (Exception e) {
            log.error("从RAG删除文档异常: docId={}, error={}", docId, e.getMessage());
            return false;
        }
    }
}
