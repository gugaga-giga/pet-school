# PetSchool 系统架构图

## 整体架构

```mermaid
graph TB
    subgraph 客户端
        Web[PetSchool Vue前端<br/>端口5173]
    end

    subgraph PetSchool后端<br/>Spring Boot 端口8080
        Auth[认证模块<br/>JWT Auth]
        Course[课程模块<br/>CourseController]
        Order[订单模块<br/>PetOrderController]
        Pet[宠物模块<br/>PetController]
        User[用户模块<br/>UserController]

        subgraph AI模块
            AiChat[AI聊天控制器<br/>AiController]
            Intent[意图识别<br/>IntentClassifierImpl]
            Context[上下文构建<br/>ContextServiceImpl]
            Memory[记忆管理<br/>MemoryServiceImpl]
            RAGClient[RAG客户端<br/>RAGServiceImpl]
            Prompt[提示词工程<br/>PromptServiceImpl]
            Router[智能路由<br/>RouterServiceImpl]
            RagSync[RAG同步器<br/>RagSyncServiceImpl]
        end
    end

    subgraph AI引擎服务<br/>FastAPI 端口8000
        SyncText[文档同步<br/>/api/v1/documents/sync-text]
        Retrieval[向量检索<br/>/api/v1/retrieval]
        Embedding[Embedding<br/>BGE-M3]
        FAISS[FAISS向量库]
    end

    subgraph 数据层
        MySQL[(MySQL<br/>pet_school库)]
        LLM[通义千问<br/>qwen-plus]
    end

    Web -->|HTTP/JWT| Auth
    Web -->|HTTP/JWT| Course
    Web -->|HTTP/JWT| Order
    Web -->|HTTP/JWT| Pet
    Web -->|SSE/JWT| AiChat

    AiChat --> Intent
    Intent --> Router
    Router --> Context
    Router --> RAGClient
    Router --> Memory
    Router --> Prompt

    Context -->|MyBatis| MySQL
    Memory -->|MyBatis| MySQL
    AiChat -->|MyBatis| MySQL

    RAGClient -->|HTTP| Retrieval
    RagSync -->|HTTP| SyncText

    Retrieval --> Embedding
    Embedding --> FAISS
    SyncText --> MySQL

    Prompt -->|HTTP| LLM
```

## 数据流架构

```mermaid
sequenceDiagram
    participant U as 用户
    participant F as Vue前端
    participant P as PetSchool后端
    participant AI as AI模块
    participant RAG as FastAPI RAG
    participant DB as MySQL
    participant LLM as 通义千问

    U->>F: 发送消息
    F->>P: POST /ai/chat (SSE, JWT)
    P->>AI: AiChatService.chatStream()
    AI->>AI: IntentClassifier.classify()
    AI->>DB: ContextService.buildContext()<br/>(用户/宠物/订单/课程)
    AI->>RAG: RAGService.retrieve(query)
    RAG-->>AI: 检索结果
    AI->>AI: PromptService.buildPrompt()
    AI->>LLM: qwen-plus API调用
    LLM-->>AI: 流式响应
    AI->>DB: MemoryService.saveMessage()
    AI->>DB: MemoryExtractor.extract()
    AI-->>P: SSE事件流
    P-->>F: text/event-stream
    F-->>U: 逐字显示
```

## RAG同步架构

```mermaid
sequenceDiagram
    participant Admin as 管理员
    participant P as PetSchool后端
    participant Sync as RagSyncService
    participant RAG as FastAPI RAG
    participant DB as MySQL

    Admin->>P: POST /course/admin/sync-rag-all
    P->>Sync: syncAllCourses()
    loop 每个课程
        Sync->>DB: CourseMapper.selectById()
        DB-->>Sync: Course数据
        Sync->>RAG: POST /api/v1/documents/sync-text<br/>{title, content, kb_id, doc_type, ref_id}
        RAG->>DB: 创建/更新Document
        RAG->>DB: 创建DocumentChunks
        RAG-->>Sync: {doc_id, chunk_count}
    end
    Sync-->>P: 同步结果
    P-->>Admin: {total, success}
```

## JWT认证流程

```mermaid
sequenceDiagram
    participant U as 用户
    participant F as Vue前端
    participant J as JwtFilter
    participant C as Controller

    U->>F: 登录(手机号+密码)
    F->>J: POST /auth/login
    J->>J: 白名单放行
    J-->>F: JWT Token
    F->>F: 存储Token到localStorage

    F->>J: 请求 /ai/chat<br/>Authorization: Bearer {token}
    J->>J: 解析Token
    J->>J: 设置userId/role/username到request
    J->>C: 放行
    C->>C: 从request获取userId
```

## 部署架构

```mermaid
graph LR
    subgraph 服务器
        Nginx[Nginx<br/>反向代理]
        Vue[Vue前端<br/>端口5173]
        Spring[Spring Boot<br/>端口8080]
        FastAPI[FastAPI<br/>端口8000]
        DB[(MySQL<br/>端口3306)]
    end

    Client[客户端] --> Nginx
    Nginx --> Vue
    Nginx --> Spring
    Spring --> FastAPI
    Spring --> DB
    FastAPI --> DB
    FastAPI --> Qwen[阿里云DashScope<br/>qwen-plus API]
```
