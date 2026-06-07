package com.petschool.ai.service;

import com.petschool.ai.context.CourseContext;
import com.petschool.ai.context.PetContext;
import com.petschool.ai.context.UserContext;
import com.petschool.ai.entity.MemoryProfile;

import java.util.List;

/**
 * Prompt模板管理和构建服务接口
 */
public interface PromptService {

    /**
     * 构建系统提示词
     */
    String buildSystemPrompt(UserContext userContext, List<PetContext> petContexts,
                             List<CourseContext> courseContexts, List<MemoryProfile> memories);

    /**
     * 构建RAG增强提示词
     */
    String buildRAGPrompt(String query, String ragContext);

    /**
     * 构建意图分类提示词
     */
    String buildIntentPrompt(String query);
}
