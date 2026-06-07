package com.petschool.ai.service.impl;

import com.petschool.ai.config.AiConfig;
import com.petschool.ai.service.RAGService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * RAG检索服务实现，通过HTTP调用FastAPI RAG服务
 */
@Service
public class RAGServiceImpl implements RAGService {

    private static final Logger log = LoggerFactory.getLogger(RAGServiceImpl.class);

    private final AiConfig aiConfig;
    private final RestTemplate ragRestTemplate;
    private final ObjectMapper objectMapper;

    public RAGServiceImpl(AiConfig aiConfig, RestTemplate ragRestTemplate) {
        this.aiConfig = aiConfig;
        this.ragRestTemplate = ragRestTemplate;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public List<Map<String, Object>> retrieve(String query, List<Integer> kbIds, int topK) {
        try {
            String url = aiConfig.getRagBaseUrl() + "/api/v1/retrieval";

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("query", query);
            requestBody.put("kb_ids", kbIds);
            requestBody.put("top_k", topK);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response = ragRestTemplate.exchange(url, HttpMethod.POST, entity, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                Object data = body.get("data");
                if (data instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> results = (List<Map<String, Object>>) data;
                    return results;
                }
            }
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("RAG检索失败: query={}, error={}", query, e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public List<Map<String, Object>> getKnowledgeBases() {
        try {
            String url = aiConfig.getRagBaseUrl() + "/api/v1/knowledge-bases";

            ResponseEntity<Map> response = ragRestTemplate.getForEntity(url, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                Object data = body.get("data");
                if (data instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> results = (List<Map<String, Object>>) data;
                    return results;
                }
                // 分页格式
                Object items = body.get("items");
                if (items instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> results = (List<Map<String, Object>>) items;
                    return results;
                }
            }
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("获取知识库列表失败: error={}", e.getMessage());
            return Collections.emptyList();
        }
    }
}
