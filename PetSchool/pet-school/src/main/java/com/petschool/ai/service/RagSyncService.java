package com.petschool.ai.service;

import java.util.Map;

/**
 * RAG数据同步服务接口，将数据库变更同步到向量库
 */
public interface RagSyncService {

    /**
     * 同步课程到RAG向量库
     */
    boolean syncCourse(Long courseId);

    /**
     * 同步宠物知识到RAG向量库
     */
    boolean syncPetKnowledge(Long petId);

    /**
     * 从RAG向量库删除文档
     */
    boolean deleteFromRAG(Long docId);
}
