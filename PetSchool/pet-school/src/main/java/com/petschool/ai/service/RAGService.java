package com.petschool.ai.service;

import java.util.List;
import java.util.Map;

/**
 * RAG检索服务接口，通过HTTP调用FastAPI RAG服务
 */
public interface RAGService {

    /**
     * 检索相关文档
     * @param query 查询文本
     * @param kbIds 知识库ID列表
     * @param topK 返回结果数
     * @return 检索结果列表
     */
    List<Map<String, Object>> retrieve(String query, List<Integer> kbIds, int topK);

    /**
     * 获取知识库列表
     * @return 知识库列表
     */
    List<Map<String, Object>> getKnowledgeBases();
}
