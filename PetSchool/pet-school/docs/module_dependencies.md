# PetSchool 模块依赖图

## AI模块内部依赖

```mermaid
graph TB
    subgraph AI模块 com.petschool.ai
        subgraph controller
            AiController[AiController<br/>/ai/chat, /ai/sessions, /ai/memory]
        end

        subgraph service
            AiChatService[AiChatService<br/>聊天核心流程]
            IntentClassifier[IntentClassifierImpl<br/>8种意图分类]
            ContextService[ContextServiceImpl<br/>用户上下文构建]
            MemoryService[MemoryServiceImpl<br/>会话记忆管理]
            RAGService[RAGServiceImpl<br/>RAG检索客户端]
            PromptService[PromptServiceImpl<br/>提示词模板]
            RouterService[RouterServiceImpl<br/>智能路由]
            RagSyncService[RagSyncServiceImpl<br/>数据同步向量库]
        end

        subgraph entity
            ChatSession[ChatSession]
            ChatMessage[ChatMessage]
            MemoryProfile[MemoryProfile]
        end

        subgraph mapper
            ChatSessionMapper[ChatSessionMapper]
            ChatMessageMapper[ChatMessageMapper]
            MemoryProfileMapper[MemoryProfileMapper]
        end

        subgraph context
            UserContext[UserContext]
            PetContext[PetContext]
            CourseContext[CourseContext]
        end

        subgraph memory
            ContextCompressor[ContextCompressor]
            MemoryExtractor[MemoryExtractor]
        end

        subgraph recommendation
            CourseRecommender[CourseRecommender]
        end

        subgraph config
            AiConfig[AiConfig<br/>api-key/base-url/model-name/rag-base-url]
        end
    end

    AiController --> AiChatService
    AiController --> MemoryService

    AiChatService --> IntentClassifier
    AiChatService --> RouterService
    AiChatService --> MemoryService
    AiChatService --> MemoryExtractor

    RouterService --> ContextService
    RouterService --> RAGService
    RouterService --> PromptService
    RouterService --> CourseRecommender

    ContextService --> UserContext
    ContextService --> PetContext
    ContextService --> CourseContext
    ContextService --> ContextCompressor

    MemoryService --> ChatSessionMapper
    MemoryService --> ChatMessageMapper
    MemoryService --> MemoryProfileMapper

    AiChatService --> ChatSessionMapper
    AiChatService --> ChatMessageMapper
    MemoryExtractor --> MemoryProfileMapper

    RAGService --> AiConfig
    RagSyncService --> AiConfig
```

## AI模块与PetSchool核心模块的依赖

```mermaid
graph TB
    subgraph AI模块
        AiChatService
        ContextService
        RagSyncService
    end

    subgraph PetSchool核心模块
        UserMapper[UserMapper<br/>用户表]
        PetMapper[PetMapper<br/>宠物表]
        CourseMapper[CourseMapper<br/>课程表]
        PetOrderMapper[PetOrderMapper<br/>订单表]
        HealthRecordMapper[HealthRecordMapper<br/>健康记录表]
        VaccineRecordMapper[VaccineRecordMapper<br/>疫苗记录表]
        CourseService[CourseService<br/>课程服务]
        CourseController[CourseController<br/>课程控制器]
    end

    subgraph PetSchool基础设施
        JwtUtil[JwtUtil<br/>JWT工具类]
        JwtFilter[JwtFilter<br/>JWT过滤器]
    end

    subgraph 外部服务
        FastAPI[FastAPI RAG服务<br/>端口8000]
        DashScope[阿里云DashScope<br/>qwen-plus]
    end

    AiChatService --> JwtFilter
    ContextService --> UserMapper
    ContextService --> PetMapper
    ContextService --> CourseMapper
    ContextService --> PetOrderMapper
    ContextService --> HealthRecordMapper
    ContextService --> VaccineRecordMapper
    RagSyncService --> CourseMapper
    RagSyncService --> PetMapper
    RagSyncService --> FastAPI
    CourseController --> RagSyncService
    AiChatService --> DashScope
```

## 渐进式改造说明

```mermaid
graph LR
    subgraph 阶段1-数据统一
        S1A[删除AI项目用户体系] --> S1B[统一PetSchool JWT]
        S1C[删除AI项目课程数据] --> S1D[统一PetSchool课程表]
        S1E[删除AI项目订单数据] --> S1F[统一PetSchool订单表]
    end

    subgraph 阶段2-AI模块接入
        S2A[新增AI数据表] --> S2B[实现AI模块Java代码]
        S2B --> S2C[JWT身份共享]
        S2C --> S2D[用户上下文构建]
    end

    subgraph 阶段3-RAG同步
        S3A[实现RagSyncService] --> S3B[课程变更钩子]
        S3B --> S3C[FastAPI sync-text接口]
        S3C --> S3D[向量索引更新]
    end

    subgraph 阶段4-统一接口
        S4A[POST /ai/chat SSE] --> S4B[POST /ai/chat/sync]
        S4B --> S4C[会话管理接口]
        S4C --> S4D[记忆管理接口]
    end

    阶段1-数据统一 --> 阶段2-AI模块接入 --> 阶段3-RAG同步 --> 阶段4-统一接口
```

## 改造原则

1. **不影响现有业务**: AI模块作为独立包(com.petschool.ai)，不修改任何已有Controller/Service
2. **渐进式接入**: 通过Spring依赖注入，AI模块按需加载
3. **JWT统一认证**: 复用PetSchool JwtFilter，AI接口自动受保护
4. **数据源统一**: AI模块直接使用PetSchool Mapper，不创建独立数据源
5. **RAG异步同步**: 课程变更通过HTTP调用FastAPI，不阻塞主流程
