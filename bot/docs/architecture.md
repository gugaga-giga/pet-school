# 基于Transformer和RAG的智能售前客服机器人 — 架构设计文档

> **版本**: v1.0  
> **日期**: 2026-06-04  
> **技术栈**: Vue3 + TypeScript + Element Plus | FastAPI | MySQL | FAISS | Qwen3 | BGE-M3

---

## 目录

1. [系统总体架构图](#1-系统总体架构图)
2. [模块划分](#2-模块划分)
3. [微服务设计](#3-微服务设计)
4. [API设计方案](#4-api设计方案)
5. [数据流图](#5-数据流图)
6. [数据库ER图](#6-数据库er图)
7. [项目目录结构](#7-项目目录结构)

---

## 1. 系统总体架构图

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                              系 统 总 体 架 构                                    │
└─────────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────────┐
│  前端层 (Presentation Layer)                                                     │
│                                                                                 │
│   ┌──────────┐    HTTPS     ┌──────────────┐    ┌───────────────────────────┐   │
│   │  用户浏览器 │ ──────────→ │    Nginx     │ ──→│  Vue3 + TypeScript        │   │
│   │  (Client)  │ ←────────── │  (反向代理/   │ ←──│  Element Plus UI          │   │
│   └──────────┘              │   负载均衡)    │    │  Pinia / Vue Router       │   │
│                             └──────────────┘    └───────────────────────────┘   │
└──────────────────────────────────┬──────────────────────────────────────────────┘
                                   │ HTTP / WebSocket
                                   ▼
┌─────────────────────────────────────────────────────────────────────────────────┐
│  API网关层 (API Gateway Layer)                                                   │
│                                                                                 │
│   ┌─────────────────────────────────────────────────────────────────────────┐   │
│   │                        FastAPI Application                              │   │
│   │  ┌────────────┐  ┌────────────┐  ┌────────────┐  ┌────────────────┐   │   │
│   │  │  JWT认证    │  │  RBAC权限   │  │  请求限流   │  │  CORS中间件    │   │   │
│   │  │  Middleware │  │  Middleware │  │  Middleware │  │  Middleware    │   │   │
│   │  └────────────┘  └────────────┘  └────────────┘  └────────────────┘   │   │
│   └─────────────────────────────────────────────────────────────────────────┘   │
└──────────────────────────────────┬──────────────────────────────────────────────┘
                                   │
                                   ▼
┌─────────────────────────────────────────────────────────────────────────────────┐
│  业务服务层 (Business Service Layer)                                              │
│                                                                                 │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐             │
│  │   auth   │ │   user   │ │   rbac   │ │knowledge │ │document  │             │
│  │ 认证模块  │ │ 用户模块  │ │ 权限模块  │ │ 知识库模块│ │ 文档模块  │             │
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘ └──────────┘             │
│                                                                                 │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐             │
│  │   chat   │ │  intent  │ │recommend │ │   pet    │ │   log    │             │
│  │ 聊天模块  │ │ 意图模块  │ │ 推荐模块  │ │ 宠物模块  │ │ 日志模块  │             │
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘ └──────────┘             │
│                                                                                 │
│  ┌──────────┐ ┌──────────┐                                                     │
│  │  config  │ │  admin   │                                                     │
│  │ 配置模块  │ │ 后台模块  │                                                     │
│  └──────────┘ └──────────┘                                                     │
└──────────────────────────────────┬──────────────────────────────────────────────┘
                                   │
                    ┌──────────────┼──────────────┐
                    ▼              ▼              ▼
┌──────────────────────┐ ┌─────────────────┐ ┌──────────────────────────────────┐
│  AI引擎层             │ │  数据层          │ │  外部服务                         │
│  (AI Engine Layer)    │ │  (Data Layer)   │ │  (External Services)             │
│                      │ │                 │ │                                  │
│ ┌──────────────────┐ │ │ ┌─────────────┐ │ │ ┌──────────────────────────────┐ │
│ │   RAG引擎        │ │ │ │   MySQL     │ │ │ │     Qwen3 LLM               │ │
│ │ ┌──────────────┐ │ │ │ │             │ │ │ │   (本地部署 / API调用)        │ │
│ │ │ 文档解析      │ │ │ │ │  users      │ │ │ └──────────────────────────────┘ │
│ │ │ (PDF/DOCX/   │ │ │ │ │  roles      │ │ │                                  │
│ │ │  MD/TXT)     │ │ │ │ │  pets       │ │ │ ┌──────────────────────────────┐ │
│ │ └──────────────┘ │ │ │ │  chat_*     │ │ │ │     BGE-M3 Embedding         │ │
│ │ ┌──────────────┐ │ │ │ │  knowledge  │ │ │ │   (文本向量化模型)            │ │
│ │ │ 文本切分      │ │ │ │ │  documents  │ │ │ └──────────────────────────────┘ │
│ │ │ (递归/语义)   │ │ │ │ │  products   │ │ │                                  │
│ │ └──────────────┘ │ │ │ │  logs       │ │ │ ┌──────────────────────────────┐ │
│ │ ┌──────────────┐ │ │ │ │  configs    │ │ │ │     FAISS 向量索引           │ │
│ │ │ BGE-M3向量化  │ │ │ │ └─────────────┘ │ │ │   (本地向量数据库)           │ │
│ │ └──────────────┘ │ │ │                 │ │ └──────────────────────────────┘ │
│ │ ┌──────────────┐ │ │ │ ┌─────────────┐ │ │                                  │
│ │ │ FAISS检索     │ │ │ │   FAISS      │ │ │                                  │
│ │ │ (相似度搜索)  │ │ │ │  向量索引文件  │ │ │                                  │
│ │ └──────────────┘ │ │ │ └─────────────┘ │ │                                  │
│ │ ┌──────────────┐ │ │ │                 │ │                                  │
│ │ │ 重排序        │ │ │ │ ┌─────────────┐ │ │                                  │
│ │ │ (MMR/交叉编码)│ │ │ │   Redis      │ │ │                                  │
│ │ └──────────────┘ │ │ │  (缓存/会话)  │ │ │                                  │
│ └──────────────────┘ │ │ └─────────────┘ │ │                                  │
│                      │ │                 │ │                                  │
│ ┌──────────────────┐ │ └─────────────────┘ └──────────────────────────────────┘
│ │   LLM引擎        │ │
│ │ ┌──────────────┐ │ │
│ │ │  Qwen3       │ │ │
│ │ │  (对话生成)   │ │ │
│ │ └──────────────┘ │ │
│ │ ┌──────────────┐ │ │
│ │ │  流式输出     │ │ │
│ │ │  (SSE)       │ │ │
│ │ └──────────────┘ │ │
│ └──────────────────┘ │
│                      │
│ ┌──────────────────┐ │
│ │  意图识别模块     │ │
│ │ (规则+LLM混合)   │ │
│ └──────────────────┘ │
│                      │
│ ┌──────────────────┐ │
│ │  产品推荐引擎     │ │
│ │ (协同+内容过滤)   │ │
│ └──────────────────┘ │
└──────────────────────┘
```

---

## 2. 模块划分

### 2.1 模块总览

| 序号 | 模块名称 | 模块标识 | 职责描述 |
|------|---------|---------|---------|
| 1 | 用户认证模块 | auth | 用户登录、注册、Token签发与刷新、密码加密 |
| 2 | 用户管理模块 | user | 用户CRUD、个人信息维护、头像管理 |
| 3 | 角色权限模块 | rbac | 角色/权限CRUD、角色-权限关联、用户-角色分配 |
| 4 | 知识库管理模块 | knowledge | 知识库CRUD、知识库配置、索引管理 |
| 5 | 文档管理模块 | document | 文档上传/下载、文档解析、切分策略配置 |
| 6 | RAG引擎模块 | rag | 文档向量化、FAISS索引构建、检索、重排序 |
| 7 | 聊天模块 | chat | 会话管理、消息收发、流式输出、上下文维护 |
| 8 | 意图识别模块 | intent | 用户意图分类、槽位提取、对话路由 |
| 9 | 产品推荐模块 | recommend | 产品匹配、推荐策略、推荐结果生成 |
| 10 | 宠物管理模块 | pet | 宠物信息CRUD、宠物画像、偏好分析 |
| 11 | 日志模块 | log | 操作日志记录、查询、统计分析 |
| 12 | 系统配置模块 | config | 系统参数管理、LLM参数配置、热更新 |
| 13 | 后台管理模块 | admin | 仪表盘数据、系统统计、运维管理 |

### 2.2 各模块详细职责

#### 2.2.1 用户认证模块 (auth)

- **用户登录**: 校验用户名/密码，签发JWT Access Token + Refresh Token
- **用户注册**: 新用户注册，密码bcrypt加密存储
- **Token刷新**: Refresh Token轮换机制，防止Token被盗用
- **登出**: 黑名单机制，使已签发的Token失效
- **密码重置**: 通过邮箱/手机验证码重置密码

#### 2.2.2 用户管理模块 (user)

- **用户列表**: 分页查询、条件筛选、排序
- **用户详情**: 查看用户完整信息及关联数据
- **用户更新**: 修改基本信息、状态变更
- **用户删除**: 逻辑删除，保留审计记录
- **个人信息**: 用户自行维护个人资料

#### 2.2.3 角色权限模块 (rbac)

- **角色管理**: 角色CRUD，支持多级角色
- **权限管理**: 权限CRUD，按资源+操作定义细粒度权限
- **角色-权限**: 为角色分配/移除权限
- **用户-角色**: 为用户分配/移除角色，支持多角色
- **权限校验**: 中间件层自动校验当前用户是否具有所需权限

#### 2.2.4 知识库管理模块 (knowledge)

- **知识库CRUD**: 创建、查询、更新、删除知识库
- **知识库配置**: 配置切分策略、Embedding模型、检索参数
- **索引管理**: 构建索引、更新索引、重建索引
- **知识库统计**: 文档数、切片数、索引状态

#### 2.2.5 文档管理模块 (document)

- **文档上传**: 支持PDF、DOCX、MD、TXT等格式
- **文档解析**: 自动识别文件类型，提取纯文本
- **文档切分**: 按配置策略（递归/语义/固定长度）切分
- **文档状态**: 跟踪文档处理进度（上传中→解析中→切分中→向量化中→就绪）
- **文档删除**: 级联删除关联切片和向量

#### 2.2.6 RAG引擎模块 (rag)

- **向量化**: 调用BGE-M3模型将文本切片转为向量
- **索引构建**: 构建FAISS索引（Flat/IVF/HNSW）
- **相似检索**: 根据查询向量检索Top-K相似切片
- **重排序**: MMR去重 / 交叉编码器精排
- **上下文组装**: 将检索结果组装为LLM Prompt

#### 2.2.7 聊天模块 (chat)

- **会话管理**: 创建/查询/删除聊天会话
- **消息发送**: 接收用户消息，触发RAG+LLM处理流水线
- **流式输出**: 基于SSE (Server-Sent Events) 实现逐Token流式响应
- **上下文维护**: 管理多轮对话上下文窗口
- **消息历史**: 持久化存储聊天记录

#### 2.2.8 意图识别模块 (intent)

- **意图分类**: 识别用户输入的意图类型（产品咨询/价格查询/技术支持/闲聊/投诉等）
- **槽位提取**: 从用户输入中提取关键实体（产品名/品牌/价格区间/宠物类型等）
- **对话路由**: 根据意图将请求路由到不同处理流程
- **混合策略**: 规则匹配（关键词/正则）+ LLM语义理解

#### 2.2.9 产品推荐模块 (recommend)

- **产品匹配**: 根据用户意图和槽位信息匹配产品
- **推荐策略**: 基于内容的推荐 + 协同过滤 + LLM生成推荐理由
- **个性化**: 结合用户画像和宠物信息进行个性化推荐
- **推荐解释**: LLM生成自然语言推荐理由

#### 2.2.10 宠物管理模块 (pet)

- **宠物CRUD**: 宠物信息的增删改查
- **宠物画像**: 品种、年龄、体重、健康状况等属性管理
- **偏好分析**: 根据宠物特征推荐适配产品
- **宠物关联**: 宠物与用户、推荐规则的关联

#### 2.2.11 日志模块 (log)

- **操作日志**: 记录用户关键操作（登录/数据修改/配置变更）
- **API日志**: 记录API请求/响应信息
- **AI日志**: 记录LLM调用、检索结果、Token消耗
- **日志查询**: 按时间/用户/类型/级别筛选查询
- **日志统计**: 日志量趋势、异常统计

#### 2.2.12 系统配置模块 (config)

- **参数管理**: 系统运行参数的CRUD
- **LLM配置**: 模型选择、Temperature、Top-P、Max Tokens等参数
- **RAG配置**: 检索Top-K、相似度阈值、切分参数
- **热更新**: 配置变更无需重启服务即可生效

#### 2.2.13 后台管理模块 (admin)

- **仪表盘**: 用户数、会话数、文档数、问答数等核心指标
- **系统统计**: 日活/月活、问答成功率、平均响应时间
- **运维管理**: 系统健康检查、缓存管理、索引状态

---

## 3. 微服务设计

### 3.1 设计原则

本项目采用**模块化单体架构**（Modular Monolith），在单个FastAPI应用内按业务领域划分独立模块。每个模块拥有独立的路由、服务层、数据模型和Schema定义，模块间通过服务接口通信，为未来拆分为微服务预留边界。

### 3.2 模块通信方式

```
┌─────────────────────────────────────────────────────────────────┐
│                    FastAPI Application                           │
│                                                                 │
│  ┌─────────┐   依赖注入    ┌─────────┐   依赖注入    ┌───────┐ │
│  │  Router  │ ───────────→ │ Service │ ───────────→  │ Model │ │
│  │  (路由)  │              │ (业务)   │               │ (ORM) │ │
│  └─────────┘              └────┬────┘               └───────┘ │
│                                │                               │
│                     ┌──────────┼──────────┐                    │
│                     ▼          ▼          ▼                    │
│              ┌──────────┐ ┌────────┐ ┌──────────┐             │
│              │ 同模块内   │ │跨模块   │ │ 外部服务  │             │
│              │ 直接调用   │ │接口调用  │ │ API调用   │             │
│              └──────────┘ └────────┘ └──────────┘             │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

**通信规则**：

| 通信场景 | 方式 | 说明 |
|---------|------|------|
| 同模块内 (Router → Service → Model) | 直接函数调用 | 模块内部通过Python函数直接调用 |
| 跨模块调用 | 服务接口调用 | 通过注入其他模块的Service类调用，禁止直接操作其他模块的Model |
| 外部服务 (LLM/Embedding) | HTTP API / SDK | 通过封装的Client类调用 |
| 前端通信 | REST API + SSE | HTTP请求响应 + Server-Sent Events流式推送 |

### 3.3 各服务职责与边界

```
┌──────────────────────────────────────────────────────────────────────┐
│                        模块依赖关系图                                 │
│                                                                      │
│                        ┌───────┐                                     │
│                        │ auth  │ ← 依赖 user, rbac                   │
│                        └───┬───┘                                     │
│                            │                                         │
│              ┌─────────────┼─────────────┐                           │
│              ▼             ▼             ▼                           │
│         ┌────────┐   ┌────────┐   ┌────────┐                        │
│         │  user  │   │  rbac  │   │  log   │                        │
│         └────────┘   └────────┘   └────────┘                        │
│              │             │                                         │
│              ▼             ▼                                         │
│         ┌────────┐   ┌────────┐                                     │
│         │  pet   │   │ config │                                     │
│         └────────┘   └────────┘                                     │
│                                                                      │
│         ┌────────────┐    ┌───────────┐    ┌──────────┐             │
│         │ knowledge  │ ←─ │ document  │ ──→│   rag    │             │
│         └────────────┘    └───────────┘    └────┬─────┘             │
│                                                  │                   │
│                            ┌───────────┐        │                   │
│                            │   chat    │ ←──────┤                   │
│                            └─────┬─────┘        │                   │
│                                  │              │                   │
│                     ┌────────────┼────────────┐ │                   │
│                     ▼            ▼            ▼ ▼                   │
│               ┌──────────┐ ┌──────────┐ ┌──────────┐              │
│               │  intent  │ │recommend │ │   LLM    │              │
│               └──────────┘ └──────────┘ └──────────┘              │
│                                    │                                │
│                                    ▼                                │
│                               ┌────────┐                           │
│                               │ admin  │ ← 聚合各模块数据            │
│                               └────────┘                           │
└──────────────────────────────────────────────────────────────────────┘
```

### 3.4 模块化结构规范

每个模块遵循统一的分层结构：

```
module_name/
├── router.py      # 路由定义 (FastAPI APIRouter)
├── service.py     # 业务逻辑层
├── model.py       # ORM模型 (SQLAlchemy)
├── schema.py      # 请求/响应模型 (Pydantic)
└── dependencies.py # 模块级依赖注入
```

**分层调用规则**：
- `router.py` → 仅调用 `service.py`
- `service.py` → 可调用 `model.py`、其他模块的 `service.py`、外部Client
- `model.py` → 纯数据定义，不包含业务逻辑
- `schema.py` → 纯数据校验，不包含业务逻辑
- **禁止**：router直接调用model、跨模块直接操作其他模块的model

---

## 4. API设计方案

### 4.1 通用响应格式

```json
// 成功响应
{
  "code": 200,
  "message": "success",
  "data": { ... }
}

// 分页响应
{
  "code": 200,
  "message": "success",
  "data": {
    "items": [ ... ],
    "total": 100,
    "page": 1,
    "page_size": 20
  }
}

// 错误响应
{
  "code": 400,
  "message": "错误描述",
  "detail": "详细错误信息"
}
```

### 4.2 认证API

#### POST /api/v1/auth/login

用户登录，获取Token。

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| username | string | 是 | 用户名 |
| password | string | 是 | 密码 |

**响应**：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "access_token": "eyJhbGciOiJIUzI1NiIs...",
    "refresh_token": "eyJhbGciOiJIUzI1NiIs...",
    "token_type": "bearer",
    "expires_in": 3600
  }
}
```

#### POST /api/v1/auth/register

用户注册。

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| username | string | 是 | 用户名 (4-20字符) |
| password | string | 是 | 密码 (8-32字符) |
| email | string | 是 | 邮箱 |
| phone | string | 否 | 手机号 |

**响应**：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "username": "newuser",
    "email": "user@example.com"
  }
}
```

#### POST /api/v1/auth/refresh

刷新Access Token。

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| refresh_token | string | 是 | Refresh Token |

**响应**：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "access_token": "eyJhbGciOiJIUzI1NiIs...",
    "refresh_token": "eyJhbGciOiJIUzI1NiIs...",
    "token_type": "bearer",
    "expires_in": 3600
  }
}
```

#### POST /api/v1/auth/logout

用户登出，将Token加入黑名单。

**请求头**: `Authorization: Bearer <access_token>`

**响应**：

```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

### 4.3 用户API

#### GET /api/v1/users

获取用户列表（分页）。

| 参数 | 位置 | 类型 | 必填 | 说明 |
|------|------|------|------|------|
| page | query | int | 否 | 页码，默认1 |
| page_size | query | int | 否 | 每页数量，默认20 |
| keyword | query | string | 否 | 搜索关键词（用户名/邮箱） |
| status | query | int | 否 | 状态筛选 (0:禁用 1:启用) |
| role_id | query | int | 否 | 角色筛选 |

**响应**：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "items": [
      {
        "id": 1,
        "username": "admin",
        "email": "admin@example.com",
        "phone": "13800138000",
        "avatar": "/uploads/avatars/1.jpg",
        "status": 1,
        "roles": [{"id": 1, "name": "admin"}],
        "created_at": "2026-01-01T00:00:00",
        "updated_at": "2026-01-01T00:00:00"
      }
    ],
    "total": 50,
    "page": 1,
    "page_size": 20
  }
}
```

#### GET /api/v1/users/{id}

获取用户详情。

**响应**：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "username": "admin",
    "email": "admin@example.com",
    "phone": "13800138000",
    "avatar": "/uploads/avatars/1.jpg",
    "status": 1,
    "roles": [{"id": 1, "name": "admin", "permissions": [...]}],
    "pets": [{"id": 1, "name": "旺财", "species": "dog"}],
    "created_at": "2026-01-01T00:00:00",
    "updated_at": "2026-01-01T00:00:00"
  }
}
```

#### PUT /api/v1/users/{id}

更新用户信息。

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| email | string | 否 | 邮箱 |
| phone | string | 否 | 手机号 |
| avatar | string | 否 | 头像URL |
| status | int | 否 | 状态 (0:禁用 1:启用) |
| role_ids | list[int] | 否 | 角色ID列表 |

**响应**：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "username": "admin",
    "email": "new@example.com",
    "updated_at": "2026-06-04T10:00:00"
  }
}
```

#### DELETE /api/v1/users/{id}

删除用户（逻辑删除）。

**响应**：

```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

### 4.4 角色API

#### GET /api/v1/roles

获取角色列表。

| 参数 | 位置 | 类型 | 必填 | 说明 |
|------|------|------|------|------|
| page | query | int | 否 | 页码 |
| page_size | query | int | 否 | 每页数量 |
| keyword | query | string | 否 | 搜索关键词 |

**响应**：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "items": [
      {
        "id": 1,
        "name": "admin",
        "description": "系统管理员",
        "permissions": [{"id": 1, "code": "user:read"}, {"id": 2, "code": "user:write"}],
        "user_count": 2,
        "created_at": "2026-01-01T00:00:00"
      }
    ],
    "total": 5,
    "page": 1,
    "page_size": 20
  }
}
```

#### POST /api/v1/roles

创建角色。

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| name | string | 是 | 角色名称 |
| description | string | 否 | 角色描述 |
| permission_ids | list[int] | 否 | 权限ID列表 |

**响应**：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 3,
    "name": "operator",
    "description": "运营人员",
    "permissions": [...]
  }
}
```

#### PUT /api/v1/roles/{id}

更新角色。

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| name | string | 否 | 角色名称 |
| description | string | 否 | 角色描述 |
| permission_ids | list[int] | 否 | 权限ID列表（全量替换） |

#### DELETE /api/v1/roles/{id}

删除角色。

### 4.5 权限API

#### GET /api/v1/permissions

获取权限列表。

| 参数 | 位置 | 类型 | 必填 | 说明 |
|------|------|------|------|------|
| resource | query | string | 否 | 按资源筛选 |
| action | query | string | 否 | 按操作筛选 |

**响应**：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "items": [
      {
        "id": 1,
        "code": "user:read",
        "name": "查看用户",
        "resource": "user",
        "action": "read",
        "description": "查看用户信息"
      },
      {
        "id": 2,
        "code": "user:write",
        "name": "编辑用户",
        "resource": "user",
        "action": "write",
        "description": "创建和编辑用户"
      }
    ],
    "total": 30,
    "page": 1,
    "page_size": 100
  }
}
```

#### POST /api/v1/permissions

创建权限。

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| code | string | 是 | 权限编码 (格式: resource:action) |
| name | string | 是 | 权限名称 |
| resource | string | 是 | 资源标识 |
| action | string | 是 | 操作类型 (read/write/delete) |
| description | string | 否 | 权限描述 |

#### PUT /api/v1/permissions/{id}

更新权限。

#### DELETE /api/v1/permissions/{id}

删除权限。

### 4.6 知识库API

#### GET /api/v1/knowledge-bases

获取知识库列表。

| 参数 | 位置 | 类型 | 必填 | 说明 |
|------|------|------|------|------|
| page | query | int | 否 | 页码 |
| page_size | query | int | 否 | 每页数量 |
| keyword | query | string | 否 | 搜索关键词 |

**响应**：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "items": [
      {
        "id": 1,
        "name": "产品手册",
        "description": "公司产品使用手册",
        "document_count": 15,
        "chunk_count": 320,
        "index_status": "ready",
        "embedding_model": "bge-m3",
        "chunk_strategy": "recursive",
        "chunk_size": 512,
        "chunk_overlap": 64,
        "created_at": "2026-01-01T00:00:00",
        "updated_at": "2026-06-01T00:00:00"
      }
    ],
    "total": 3,
    "page": 1,
    "page_size": 20
  }
}
```

#### POST /api/v1/knowledge-bases

创建知识库。

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| name | string | 是 | 知识库名称 |
| description | string | 否 | 知识库描述 |
| embedding_model | string | 否 | Embedding模型，默认bge-m3 |
| chunk_strategy | string | 否 | 切分策略 (recursive/semantic/fixed)，默认recursive |
| chunk_size | int | 否 | 切片大小，默认512 |
| chunk_overlap | int | 否 | 重叠字符数，默认64 |

#### GET /api/v1/knowledge-bases/{id}

获取知识库详情。

#### PUT /api/v1/knowledge-bases/{id}

更新知识库配置。

#### DELETE /api/v1/knowledge-bases/{id}

删除知识库（级联删除文档、切片、向量索引）。

#### POST /api/v1/knowledge-bases/{id}/build-index

构建/重建知识库向量索引。

**响应**：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "task_id": "build_20260604_001",
    "status": "processing",
    "progress": 0
  }
}
```

### 4.7 文档API

#### POST /api/v1/documents/upload

上传文档到指定知识库。

| 字段 | 位置 | 类型 | 必填 | 说明 |
|------|------|------|------|------|
| file | body | file | 是 | 文档文件 (PDF/DOCX/MD/TXT) |
| knowledge_base_id | body | int | 是 | 知识库ID |
| description | body | string | 否 | 文档描述 |

**响应**：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 10,
    "filename": "产品手册v2.pdf",
    "file_size": 2048576,
    "knowledge_base_id": 1,
    "status": "uploading",
    "created_at": "2026-06-04T10:00:00"
  }
}
```

#### GET /api/v1/documents

获取文档列表。

| 参数 | 位置 | 类型 | 必填 | 说明 |
|------|------|------|------|------|
| page | query | int | 否 | 页码 |
| page_size | query | int | 否 | 每页数量 |
| knowledge_base_id | query | int | 否 | 按知识库筛选 |
| status | query | string | 否 | 按状态筛选 (uploading/parsing/chunking/embedding/ready/error) |

#### GET /api/v1/documents/{id}

获取文档详情（含切片列表）。

#### DELETE /api/v1/documents/{id}

删除文档（级联删除切片和向量）。

### 4.8 聊天API

#### POST /api/v1/chat/sessions

创建聊天会话。

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| title | string | 否 | 会话标题 |
| knowledge_base_id | int | 否 | 关联知识库ID |

**响应**：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": "sess_abc123",
    "title": "新产品咨询",
    "knowledge_base_id": 1,
    "created_at": "2026-06-04T10:00:00"
  }
}
```

#### GET /api/v1/chat/sessions

获取当前用户的会话列表。

| 参数 | 位置 | 类型 | 必填 | 说明 |
|------|------|------|------|------|
| page | query | int | 否 | 页码 |
| page_size | query | int | 否 | 每页数量 |

**响应**：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "items": [
      {
        "id": "sess_abc123",
        "title": "新产品咨询",
        "last_message": "推荐一款适合金毛的狗粮",
        "message_count": 8,
        "created_at": "2026-06-04T10:00:00",
        "updated_at": "2026-06-04T10:30:00"
      }
    ],
    "total": 5,
    "page": 1,
    "page_size": 20
  }
}
```

#### POST /api/v1/chat/sessions/{id}/messages

发送消息（支持流式响应）。

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| content | string | 是 | 消息内容 |
| stream | bool | 否 | 是否流式输出，默认true |

**流式响应 (SSE)**：

```
event: message_start
data: {"message_id": "msg_001", "role": "assistant"}

event: token
data: {"token": "根据"}

event: token
data: {"token": "您的"}

event: token
data: {"token": "需求"}

event: references
data: {"chunks": [{"content": "...", "source": "产品手册v2.pdf", "score": 0.92}]}

event: message_end
data: {"message_id": "msg_001", "token_count": 156}
```

**非流式响应**：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": "msg_001",
    "role": "assistant",
    "content": "根据您的需求，推荐以下产品...",
    "references": [
      {
        "chunk_id": "chunk_101",
        "content": "相关文档片段...",
        "source": "产品手册v2.pdf",
        "page": 12,
        "score": 0.92
      }
    ],
    "intent": "product_inquiry",
    "token_count": 156,
    "created_at": "2026-06-04T10:30:00"
  }
}
```

#### GET /api/v1/chat/sessions/{id}/messages

获取会话消息历史。

| 参数 | 位置 | 类型 | 必填 | 说明 |
|------|------|------|------|------|
| page | query | int | 否 | 页码 |
| page_size | query | int | 否 | 每页数量 |

#### DELETE /api/v1/chat/sessions/{id}

删除会话及其消息。

### 4.9 意图API

#### POST /api/v1/intent/classify

识别用户意图。

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| text | string | 是 | 用户输入文本 |
| context | list[string] | 否 | 上下文消息列表 |

**响应**：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "intent": "product_inquiry",
    "confidence": 0.95,
    "slots": {
      "product_type": "狗粮",
      "pet_breed": "金毛",
      "price_range": "中高端"
    },
    "available_intents": [
      {"intent": "product_inquiry", "confidence": 0.95},
      {"intent": "price_query", "confidence": 0.03},
      {"intent": "chitchat", "confidence": 0.02}
    ]
  }
}
```

### 4.10 推荐API

#### POST /api/v1/recommend

获取产品推荐。

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| query | string | 是 | 用户查询文本 |
| intent | string | 否 | 预识别的意图 |
| slots | object | 否 | 预提取的槽位信息 |
| pet_id | int | 否 | 宠物ID（个性化推荐） |
| top_k | int | 否 | 返回数量，默认5 |

**响应**：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "recommendations": [
      {
        "product_id": 101,
        "name": "皇家金毛专用成犬粮",
        "category": "狗粮",
        "price": 298.00,
        "match_score": 0.96,
        "reason": "专为金毛犬设计，含有Omega-3和Omega-6脂肪酸，有助于维持金毛犬的被毛健康",
        "image_url": "/uploads/products/101.jpg"
      }
    ],
    "total": 5,
    "query_intent": "product_inquiry"
  }
}
```

### 4.11 宠物API

#### GET /api/v1/pets

获取当前用户的宠物列表。

**响应**：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "items": [
      {
        "id": 1,
        "name": "旺财",
        "species": "dog",
        "breed": "金毛寻回犬",
        "age": 3,
        "weight": 28.5,
        "gender": "male",
        "health_status": "healthy",
        "avatar": "/uploads/pets/1.jpg",
        "created_at": "2026-01-15T00:00:00"
      }
    ],
    "total": 2
  }
}
```

#### POST /api/v1/pets

创建宠物。

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| name | string | 是 | 宠物名称 |
| species | string | 是 | 物种 (dog/cat/bird/other) |
| breed | string | 否 | 品种 |
| age | int | 否 | 年龄（岁） |
| weight | float | 否 | 体重（kg） |
| gender | string | 否 | 性别 (male/female) |
| health_status | string | 否 | 健康状态 |

#### GET /api/v1/pets/{id}

获取宠物详情。

#### PUT /api/v1/pets/{id}

更新宠物信息。

#### DELETE /api/v1/pets/{id}

删除宠物。

### 4.12 日志API

#### GET /api/v1/logs

获取日志列表。

| 参数 | 位置 | 类型 | 必填 | 说明 |
|------|------|------|------|------|
| page | query | int | 否 | 页码 |
| page_size | query | int | 否 | 每页数量 |
| log_type | query | string | 否 | 日志类型 (operation/api/ai) |
| level | query | string | 否 | 日志级别 (INFO/WARNING/ERROR) |
| user_id | query | int | 否 | 按用户筛选 |
| start_date | query | string | 否 | 起始日期 (YYYY-MM-DD) |
| end_date | query | string | 否 | 结束日期 (YYYY-MM-DD) |

**响应**：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "items": [
      {
        "id": 1001,
        "log_type": "operation",
        "level": "INFO",
        "user_id": 1,
        "username": "admin",
        "action": "document_upload",
        "detail": "上传文档: 产品手册v2.pdf",
        "ip": "192.168.1.100",
        "created_at": "2026-06-04T10:00:00"
      }
    ],
    "total": 500,
    "page": 1,
    "page_size": 20
  }
}
```

### 4.13 系统配置API

#### GET /api/v1/configs

获取系统配置列表。

**响应**：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "items": [
      {
        "id": 1,
        "key": "llm_model",
        "value": "qwen3-8b",
        "group": "llm",
        "description": "LLM模型名称",
        "updated_at": "2026-06-01T00:00:00"
      },
      {
        "id": 2,
        "key": "llm_temperature",
        "value": "0.7",
        "group": "llm",
        "description": "LLM温度参数",
        "updated_at": "2026-06-01T00:00:00"
      },
      {
        "id": 3,
        "key": "rag_top_k",
        "value": "5",
        "group": "rag",
        "description": "RAG检索返回数量",
        "updated_at": "2026-06-01T00:00:00"
      }
    ]
  }
}
```

#### POST /api/v1/configs

创建配置项。

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| key | string | 是 | 配置键 |
| value | string | 是 | 配置值 |
| group | string | 否 | 配置分组 |
| description | string | 否 | 配置描述 |

#### PUT /api/v1/configs/{id}

更新配置项。

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| value | string | 是 | 配置值 |
| description | string | 否 | 配置描述 |

#### DELETE /api/v1/configs/{id}

删除配置项。

### 4.14 后台管理API

#### GET /api/v1/admin/dashboard

获取仪表盘数据。

**响应**：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "user_count": 1250,
    "session_count_today": 380,
    "message_count_today": 1520,
    "document_count": 86,
    "knowledge_base_count": 5,
    "avg_response_time": 1.2,
    "qa_success_rate": 0.92,
    "recent_sessions": [
      {
        "id": "sess_abc123",
        "user": "张三",
        "query": "推荐适合金毛的狗粮",
        "created_at": "2026-06-04T10:30:00"
      }
    ]
  }
}
```

#### GET /api/v1/admin/stats

获取系统统计数据。

| 参数 | 位置 | 类型 | 必填 | 说明 |
|------|------|------|------|------|
| period | query | string | 否 | 统计周期 (daily/weekly/monthly)，默认daily |
| start_date | query | string | 否 | 起始日期 |
| end_date | query | string | 否 | 结束日期 |

**响应**：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "period": "daily",
    "user_stats": {
      "new_users": 15,
      "active_users": 230,
      "total_users": 1250
    },
    "chat_stats": {
      "total_sessions": 380,
      "total_messages": 1520,
      "avg_messages_per_session": 4.0
    },
    "ai_stats": {
      "total_llm_calls": 1520,
      "total_tokens": 256000,
      "avg_response_time": 1.2,
      "success_rate": 0.92
    },
    "trend": [
      {"date": "2026-06-01", "sessions": 350, "messages": 1400},
      {"date": "2026-06-02", "sessions": 380, "messages": 1520}
    ]
  }
}
```

---

## 5. 数据流图

### 5.1 文档上传处理流

```
┌──────────────────────────────────────────────────────────────────────────────┐
│                        文档上传处理流                                          │
│                                                                              │
│  用户上传文档                                                                 │
│      │                                                                       │
│      ▼                                                                       │
│  ┌──────────┐    文件存储     ┌──────────────┐                               │
│  │  前端上传  │ ────────────→ │  FastAPI接收   │                               │
│  │  (Vue3)  │   multipart   │  /upload API  │                               │
│  └──────────┘               └──────┬───────┘                               │
│                                     │                                        │
│                                     ▼                                        │
│                             ┌──────────────┐                                 │
│                             │  1.文件校验    │  文件类型/大小/格式校验            │
│                             └──────┬───────┘                                 │
│                                    │ 校验通过                                  │
│                                    ▼                                         │
│                             ┌──────────────┐                                 │
│                             │  2.文件存储    │  保存至本地/对象存储              │
│                             └──────┬───────┘                                 │
│                                    │                                         │
│                                    ▼                                         │
│                             ┌──────────────┐                                 │
│                             │  3.文档解析    │  PDF→文本 / DOCX→文本 / MD→文本  │
│                             │  (Parser)    │  提取纯文本+元数据(页码/标题)      │
│                             └──────┬───────┘                                 │
│                                    │                                         │
│                                    ▼                                         │
│                             ┌──────────────┐                                 │
│                             │  4.文本切分    │  递归切分 / 语义切分 / 固定长度   │
│                             │  (Splitter)  │  生成document_chunks             │
│                             └──────┬───────┘                                 │
│                                    │                                         │
│                                    ▼                                         │
│                             ┌──────────────┐                                 │
│                             │  5.向量化      │  BGE-M3 Embedding               │
│                             │  (Embedding) │  chunk文本 → 1024维向量          │
│                             └──────┬───────┘                                 │
│                                    │                                         │
│                                    ▼                                         │
│                             ┌──────────────┐                                 │
│                             │  6.索引存储    │  向量写入FAISS索引               │
│                             │  (FAISS)     │  元数据写入MySQL                  │
│                             └──────┬───────┘                                 │
│                                    │                                         │
│                                    ▼                                         │
│                             ┌──────────────┐                                 │
│                             │  7.状态更新    │  文档状态: ready                 │
│                             │  (MySQL)     │  可用于检索                      │
│                             └──────────────┘                                 │
│                                                                              │
└──────────────────────────────────────────────────────────────────────────────┘
```

### 5.2 问答处理流

```
┌──────────────────────────────────────────────────────────────────────────────┐
│                        问答处理流                                              │
│                                                                              │
│  用户提问: "推荐一款适合金毛的狗粮"                                            │
│      │                                                                       │
│      ▼                                                                       │
│  ┌──────────┐    SSE/HTTP    ┌──────────────┐                               │
│  │  前端发送  │ ────────────→ │  Chat API    │                               │
│  │  (Vue3)  │ ←──────────── │  接收消息      │                               │
│  └──────────┘   流式Token    └──────┬───────┘                               │
│                                     │                                        │
│                                     ▼                                        │
│                             ┌──────────────┐                                 │
│                             │  1.意图识别    │  规则匹配 + LLM分类              │
│                             │  (Intent)    │  → product_inquiry               │
│                             │              │  → slots: {pet_breed:金毛,        │
│                             │              │           product_type:狗粮}      │
│                             └──────┬───────┘                                 │
│                                    │                                         │
│                          ┌─────────┴─────────┐                               │
│                          ▼                   ▼                                │
│                   ┌────────────┐      ┌────────────┐                         │
│                   │ 产品咨询    │      │ 其他意图    │                         │
│                   │ → 触发RAG  │      │ → 直接LLM  │                         │
│                   └─────┬──────┘      └─────┬──────┘                         │
│                         │                   │                                 │
│                         ▼                   │                                 │
│                  ┌──────────────┐           │                                 │
│                  │  2.查询向量化  │           │                                 │
│                  │  BGE-M3      │           │                                 │
│                  │  问题→向量    │           │                                 │
│                  └──────┬───────┘           │                                 │
│                         │                   │                                 │
│                         ▼                   │                                 │
│                  ┌──────────────┐           │                                 │
│                  │  3.FAISS检索  │           │                                 │
│                  │  Top-K=5     │           │                                 │
│                  │  相似度>0.6   │           │                                 │
│                  └──────┬───────┘           │                                 │
│                         │                   │                                 │
│                         ▼                   │                                 │
│                  ┌──────────────┐           │                                 │
│                  │  4.重排序     │           │                                 │
│                  │  MMR去重     │           │                                 │
│                  │  交叉编码精排  │           │                                 │
│                  └──────┬───────┘           │                                 │
│                         │                   │                                 │
│                         ▼                   │                                 │
│                  ┌──────────────┐           │                                 │
│                  │  5.上下文组装  │           │                                 │
│                  │  System Prompt│          │                                 │
│                  │  + 检索结果   │           │                                 │
│                  │  + 对话历史   │           │                                 │
│                  │  + 用户问题   │           │                                 │
│                  └──────┬───────┘           │                                 │
│                         │                   │                                 │
│                         └─────────┬─────────┘                                 │
│                                   ▼                                           │
│                           ┌──────────────┐                                    │
│                           │  6.LLM生成    │  Qwen3 模型推理                    │
│                           │  (Qwen3)     │  流式输出Token                      │
│                           └──────┬───────┘                                    │
│                                  │                                            │
│                                  ▼                                            │
│                           ┌──────────────┐                                    │
│                           │  7.结果返回    │  SSE流式推送至前端                  │
│                           │              │  附带引用来源                        │
│                           └──────┬───────┘                                    │
│                                  │                                            │
│                                  ▼                                            │
│                           ┌──────────────┐                                    │
│                           │  8.日志记录    │  问答日志、Token消耗、              │
│                           │              │  检索质量、响应时间                   │
│                           └──────────────┘                                    │
│                                                                              │
└──────────────────────────────────────────────────────────────────────────────┘
```

### 5.3 推荐处理流

```
┌──────────────────────────────────────────────────────────────────────────────┐
│                        推荐处理流                                              │
│                                                                              │
│  用户提问: "我家金毛3岁了，想换一款狗粮"                                       │
│      │                                                                       │
│      ▼                                                                       │
│  ┌──────────────┐                                                            │
│  │  1.意图分类    │  → product_inquiry (产品咨询)                               │
│  │     + 槽位提取 │  → slots: {pet_breed:金毛, pet_age:3, product_type:狗粮}   │
│  └──────┬───────┘                                                            │
│         │                                                                     │
│         ▼                                                                     │
│  ┌──────────────┐                                                            │
│  │  2.宠物画像    │  查询用户宠物信息                                           │
│  │     匹配      │  金毛/3岁/28kg/健康 → 偏好标签                               │
│  └──────┬───────┘                                                            │
│         │                                                                     │
│         ▼                                                                     │
│  ┌──────────────────────────────────────────────────────────────┐            │
│  │  3.产品匹配                                                   │            │
│  │                                                              │            │
│  │  ┌────────────┐  ┌────────────┐  ┌────────────┐             │            │
│  │  │ 规则匹配    │  │ 向量匹配    │  │ 协同过滤    │             │            │
│  │  │ 品种→产品   │  │ 需求→产品   │  │ 相似用户→   │             │            │
│  │  │ 年龄→产品   │  │ 描述相似度  │  │ 相似产品    │             │            │
│  │  └─────┬──────┘  └─────┬──────┘  └─────┬──────┘             │            │
│  │        └────────────────┼───────────────┘                    │            │
│  │                         ▼                                    │            │
│  │                  ┌────────────┐                              │            │
│  │                  │ 融合排序    │  加权融合 + 去重               │            │
│  │                  └────────────┘                              │            │
│  └──────────────────────────┬───────────────────────────────────┘            │
│                             │                                                │
│                             ▼                                                │
│  ┌──────────────┐                                                           │
│  │  4.推荐生成    │  LLM生成推荐理由                                          │
│  │  (Qwen3)     │  结合产品特性+宠物画像                                      │
│  └──────┬───────┘                                                           │
│         │                                                                    │
│         ▼                                                                    │
│  ┌──────────────┐                                                           │
│  │  5.结果返回    │  推荐列表 + 匹配分数 + 推荐理由                             │
│  └──────────────┘                                                           │
│                                                                              │
└──────────────────────────────────────────────────────────────────────────────┘
```

---

## 6. 数据库ER图

```
┌──────────────────────────────────────────────────────────────────────────────────┐
│                            数据库 ER 图                                           │
│                                                                                  │
│                                                                                  │
│  ┌─────────────────┐          ┌─────────────────┐          ┌─────────────────┐   │
│  │     users        │     N:N │      roles       │     N:N │   permissions    │   │
│  ├─────────────────┤◄───────►├─────────────────┤◄───────►├─────────────────┤   │
│  │ id (PK)         │          │ id (PK)         │          │ id (PK)         │   │
│  │ username        │          │ name            │          │ code            │   │
│  │ password_hash   │          │ description     │          │ name            │   │
│  │ email           │          │ created_at      │          │ resource        │   │
│  │ phone           │          │ updated_at      │          │ action          │   │
│  │ avatar          │          └────────┬────────┘          │ description     │   │
│  │ status          │                   │                   │ created_at      │   │
│  │ created_at      │                   │                   └─────────────────┘   │
│  │ updated_at      │                   │                                         │
│  │ deleted_at      │                   │                   ┌─────────────────┐   │
│  └────────┬────────┘                   │                   │  user_roles     │   │
│           │                            └──────────────────►├─────────────────┤   │
│           │                                                │ user_id (FK)    │   │
│           │                                                │ role_id (FK)    │   │
│           │                                                └─────────────────┘   │
│           │                                                                      │
│           │                                                ┌─────────────────┐   │
│           │                                                │ role_permissions │   │
│           │                                                ├─────────────────┤   │
│           │                                                │ role_id (FK)    │   │
│           │                                                │ permission_id   │   │
│           │                                                └─────────────────┘   │
│           │                                                                      │
│     ┌─────┴──────┐                                                             │
│     │            │                                                              │
│     ▼            ▼                                                              │
│  ┌──────────────┐  ┌────────────────────┐                                       │
│  │    pets      │  │   chat_sessions     │                                      │
│  ├──────────────┤  ├────────────────────┤                                       │
│  │ id (PK)      │  │ id (PK)            │                                       │
│  │ user_id (FK) │  │ user_id (FK)       │                                       │
│  │ name         │  │ title              │                                       │
│  │ species      │  │ knowledge_base_id  │───┐                                   │
│  │ breed        │  │ created_at         │   │                                   │
│  │ age          │  │ updated_at         │   │                                   │
│  │ weight       │  └────────┬───────────┘   │                                   │
│  │ gender       │           │ 1:N           │                                   │
│  │ health_status│           ▼               │                                   │
│  │ avatar       │  ┌────────────────────┐   │                                   │
│  │ created_at   │  │   chat_messages     │   │                                   │
│  │ updated_at   │  ├────────────────────┤   │                                   │
│  └──────────────┘  │ id (PK)            │   │                                   │
│                    │ session_id (FK)    │   │                                   │
│                    │ role (user/assistant)│  │                                   │
│                    │ content            │   │                                   │
│                    │ intent             │   │                                   │
│                    │ references (JSON)  │   │                                   │
│                    │ token_count        │   │                                   │
│                    │ created_at         │   │                                   │
│                    └────────────────────┘   │                                   │
│                                             │                                   │
│  ┌──────────────────────┐                   │                                   │
│  │   knowledge_bases    │◄──────────────────┘                                   │
│  ├──────────────────────┤                                                       │
│  │ id (PK)              │     1:N                                              │
│  │ name                 │◄──────────────────────────────────────┐               │
│  │ description          │                                      │               │
│  │ embedding_model      │                                      │               │
│  │ chunk_strategy       │                                      │               │
│  │ chunk_size           │                                      │               │
│  │ chunk_overlap        │                                      │               │
│  │ index_status         │                                      │               │
│  │ created_at           │                                      │               │
│  │ updated_at           │                                      │               │
│  └──────────┬───────────┘                                      │               │
│             │ 1:N                                               │               │
│             ▼                                                   │               │
│  ┌──────────────────────┐                                      │               │
│  │     documents        │                                      │               │
│  ├──────────────────────┤                                      │               │
│  │ id (PK)              │                                      │               │
│  │ knowledge_base_id(FK)│                                      │               │
│  │ filename             │                                      │               │
│  │ file_path            │                                      │               │
│  │ file_size            │                                      │               │
│  │ file_type            │                                      │               │
│  │ status               │                                      │               │
│  │ chunk_count          │                                      │               │
│  │ created_at           │                                      │               │
│  │ updated_at           │                                      │               │
│  └──────────┬───────────┘                                      │               │
│             │ 1:N                                               │               │
│             ▼                                                   │               │
│  ┌──────────────────────┐                                      │               │
│  │   document_chunks    │                                      │               │
│  ├──────────────────────┤                                      │               │
│  │ id (PK)              │                                      │               │
│  │ document_id (FK)     │                                      │               │
│  │ knowledge_base_id(FK)│──────────────────────────────────────┘               │
│  │ chunk_index          │                                                       │
│  │ content              │                                                       │
│  │ vector_id            │  (FAISS索引中的向量ID)                                 │
│  │ metadata (JSON)      │  (页码/标题/来源等)                                    │
│  │ token_count          │                                                       │
│  │ created_at           │                                                       │
│  └──────────────────────┘                                                       │
│                                                                                  │
│  ┌──────────────────────┐     ┌──────────────────────┐                          │
│  │      products        │     │   recommend_rules     │                         │
│  ├──────────────────────┤     ├──────────────────────┤                          │
│  │ id (PK)              │◄────│ id (PK)              │                          │
│  │ name                 │  N:N│ product_id (FK)      │                          │
│  │ category             │     │ pet_species          │                          │
│  │ brand                │     │ pet_breed            │                          │
│  │ price                │     │ pet_age_range        │                          │
│  │ description          │     │ pet_weight_range     │                          │
│  │ specifications (JSON)│     │ priority             │                          │
│  │ image_url            │     │ created_at           │                          │
│  │ status               │     └──────────────────────┘                          │
│  │ created_at           │                                                       │
│  │ updated_at           │                                                       │
│  └──────────────────────┘                                                       │
│                                                                                  │
│  ┌──────────────────────┐     ┌──────────────────────┐                          │
│  │      qa_logs         │     │   system_configs      │                         │
│  ├──────────────────────┤     ├──────────────────────┤                          │
│  │ id (PK)              │     │ id (PK)              │                          │
│  │ user_id (FK)         │     │ key                  │                          │
│  │ session_id (FK)      │     │ value                │                          │
│  │ question             │     │ group                │                          │
│  │ answer               │     │ description          │                          │
│  │ intent               │     │ created_at           │                          │
│  │ retrieved_chunks(JSON)│    │ updated_at           │                          │
│  │ llm_model            │     └──────────────────────┘                          │
│  │ prompt_tokens        │                                                       │
│  │ completion_tokens    │     ┌──────────────────────┐                          │
│  │ response_time_ms     │     │    operation_logs     │                         │
│  │ feedback_score       │     ├──────────────────────┤                          │
│  │ created_at           │     │ id (PK)              │                          │
│  └──────────────────────┘     │ user_id (FK)         │                          │
│                               │ log_type             │                          │
│                               │ level                │                          │
│                               │ action               │                          │
│                               │ detail               │                          │
│                               │ ip_address           │                          │
│                               │ created_at           │                          │
│                               └──────────────────────┘                          │
│                                                                                  │
└──────────────────────────────────────────────────────────────────────────────────┘
```

---

## 7. 项目目录结构

```
project-root/
│
├── backend/                          # 后端项目根目录
│   ├── app/                          # 应用主目录
│   │   ├── __init__.py
│   │   ├── main.py                   # FastAPI应用入口
│   │   │
│   │   ├── api/                      # API路由层
│   │   │   ├── __init__.py
│   │   │   ├── deps.py               # 全局依赖注入
│   │   │   └── v1/                   # API版本v1
│   │   │       ├── __init__.py
│   │   │       ├── router.py         # v1总路由注册
│   │   │       ├── auth.py           # 认证路由
│   │   │       ├── user.py           # 用户路由
│   │   │       ├── rbac.py           # 角色权限路由
│   │   │       ├── knowledge.py      # 知识库路由
│   │   │       ├── document.py       # 文档路由
│   │   │       ├── chat.py           # 聊天路由
│   │   │       ├── intent.py         # 意图识别路由
│   │   │       ├── recommend.py      # 推荐路由
│   │   │       ├── pet.py            # 宠物路由
│   │   │       ├── log.py            # 日志路由
│   │   │       ├── config.py         # 系统配置路由
│   │   │       └── admin.py          # 后台管理路由
│   │   │
│   │   ├── services/                 # 业务逻辑层
│   │   │   ├── __init__.py
│   │   │   ├── auth_service.py
│   │   │   ├── user_service.py
│   │   │   ├── rbac_service.py
│   │   │   ├── knowledge_service.py
│   │   │   ├── document_service.py
│   │   │   ├── chat_service.py
│   │   │   ├── intent_service.py
│   │   │   ├── recommend_service.py
│   │   │   ├── pet_service.py
│   │   │   ├── log_service.py
│   │   │   ├── config_service.py
│   │   │   └── admin_service.py
│   │   │
│   │   ├── models/                   # ORM模型层
│   │   │   ├── __init__.py
│   │   │   ├── base.py               # SQLAlchemy Base
│   │   │   ├── user.py               # User / UserRole
│   │   │   ├── role.py               # Role / RolePermission
│   │   │   ├── permission.py         # Permission
│   │   │   ├── knowledge.py          # KnowledgeBase
│   │   │   ├── document.py           # Document / DocumentChunk
│   │   │   ├── chat.py               # ChatSession / ChatMessage
│   │   │   ├── product.py            # Product / RecommendRule
│   │   │   ├── pet.py                # Pet
│   │   │   ├── log.py                # QALog / OperationLog
│   │   │   └── config.py             # SystemConfig
│   │   │
│   │   ├── schemas/                  # Pydantic模型层
│   │   │   ├── __init__.py
│   │   │   ├── base.py               # 通用响应/分页模型
│   │   │   ├── auth.py               # LoginRequest / TokenResponse
│   │   │   ├── user.py               # UserCreate / UserUpdate / UserResponse
│   │   │   ├── role.py               # RoleCreate / RoleResponse
│   │   │   ├── permission.py         # PermissionCreate / PermissionResponse
│   │   │   ├── knowledge.py          # KnowledgeBaseCreate / KnowledgeBaseResponse
│   │   │   ├── document.py           # DocumentUpload / DocumentResponse
│   │   │   ├── chat.py               # ChatMessageCreate / ChatMessageResponse
│   │   │   ├── intent.py             # IntentClassifyRequest / IntentResponse
│   │   │   ├── recommend.py          # RecommendRequest / RecommendResponse
│   │   │   ├── pet.py                # PetCreate / PetResponse
│   │   │   ├── log.py                # LogQuery / LogResponse
│   │   │   ├── config.py             # ConfigCreate / ConfigResponse
│   │   │   └── admin.py              # DashboardResponse / StatsResponse
│   │   │
│   │   ├── core/                     # 核心配置
│   │   │   ├── __init__.py
│   │   │   ├── config.py             # 应用配置 (Pydantic Settings)
│   │   │   ├── security.py           # JWT生成/验证/密码哈希
│   │   │   ├── database.py           # 数据库连接/会话管理
│   │   │   └── dependencies.py       # 全局依赖 (get_db, get_current_user)
│   │   │
│   │   ├── middleware/               # 中间件
│   │   │   ├── __init__.py
│   │   │   ├── auth.py               # JWT认证中间件
│   │   │   ├── rbac.py               # RBAC权限校验中间件
│   │   │   ├── rate_limit.py         # 请求限流中间件
│   │   │   ├── logging.py            # 请求日志中间件
│   │   │   └── cors.py               # CORS配置
│   │   │
│   │   ├── utils/                    # 工具函数
│   │   │   ├── __init__.py
│   │   │   ├── file.py               # 文件操作工具
│   │   │   ├── text.py               # 文本处理工具
│   │   │   ├── pagination.py         # 分页工具
│   │   │   └── exceptions.py         # 自定义异常
│   │   │
│   │   ├── rag/                      # RAG引擎
│   │   │   ├── __init__.py
│   │   │   ├── engine.py             # RAG引擎主类
│   │   │   ├── parser.py             # 文档解析器 (PDF/DOCX/MD/TXT)
│   │   │   ├── splitter.py           # 文本切分器 (递归/语义/固定)
│   │   │   ├── embedder.py           # BGE-M3向量化封装
│   │   │   ├── retriever.py          # FAISS检索器
│   │   │   ├── reranker.py           # 重排序器 (MMR/交叉编码)
│   │   │   └── prompt.py             # Prompt模板管理
│   │   │
│   │   ├── llm/                      # LLM封装
│   │   │   ├── __init__.py
│   │   │   ├── base.py               # LLM基类
│   │   │   ├── qwen.py               # Qwen3模型封装
│   │   │   └── stream.py             # SSE流式输出
│   │   │
│   │   ├── recommend/                # 推荐引擎
│   │   │   ├── __init__.py
│   │   │   ├── engine.py             # 推荐引擎主类
│   │   │   ├── rule_matcher.py       # 规则匹配器
│   │   │   ├── vector_matcher.py     # 向量匹配器
│   │   │   └── collaborator.py       # 协同过滤
│   │   │
│   │   └── intent/                   # 意图识别
│   │       ├── __init__.py
│   │       ├── classifier.py         # 意图分类器
│   │       ├── rule_engine.py        # 规则引擎
│   │       ├── slot_extractor.py     # 槽位提取器
│   │       └── intents.py            # 意图定义与配置
│   │
│   ├── migrations/                   # Alembic数据库迁移
│   │   ├── env.py
│   │   ├── alembic.ini
│   │   └── versions/                 # 迁移版本文件
│   │
│   ├── tests/                        # 测试
│   │   ├── conftest.py               # 测试配置
│   │   ├── test_auth.py
│   │   ├── test_chat.py
│   │   ├── test_rag.py
│   │   └── test_recommend.py
│   │
│   ├── alembic.ini                   # Alembic配置
│   ├── requirements.txt              # Python依赖
│   ├── .env                          # 环境变量
│   └── Dockerfile                    # Docker构建文件
│
├── frontend/                         # 前端项目根目录
│   ├── src/
│   │   ├── api/                      # API调用
│   │   │   ├── index.ts              # Axios实例配置
│   │   │   ├── auth.ts               # 认证API
│   │   │   ├── user.ts               # 用户API
│   │   │   ├── rbac.ts               # 角色权限API
│   │   │   ├── knowledge.ts          # 知识库API
│   │   │   ├── document.ts           # 文档API
│   │   │   ├── chat.ts               # 聊天API
│   │   │   ├── intent.ts             # 意图API
│   │   │   ├── recommend.ts          # 推荐API
│   │   │   ├── pet.ts                # 宠物API
│   │   │   ├── log.ts                # 日志API
│   │   │   ├── config.ts             # 系统配置API
│   │   │   └── admin.ts              # 后台管理API
│   │   │
│   │   ├── components/               # 公共组件
│   │   │   ├── ChatBubble.vue        # 聊天气泡组件
│   │   │   ├── ChatInput.vue         # 聊天输入组件
│   │   │   ├── DocumentUploader.vue  # 文档上传组件
│   │   │   ├── KnowledgeCard.vue     # 知识库卡片组件
│   │   │   ├── PetCard.vue           # 宠物卡片组件
│   │   │   ├── ProductCard.vue       # 产品卡片组件
│   │   │   ├── ReferenceList.vue     # 引用来源组件
│   │   │   └── SearchBar.vue         # 搜索栏组件
│   │   │
│   │   ├── composables/              # 组合式函数
│   │   │   ├── useAuth.ts            # 认证逻辑
│   │   │   ├── useChat.ts            # 聊天逻辑 (含SSE)
│   │   │   ├── usePagination.ts      # 分页逻辑
│   │   │   └── usePermission.ts      # 权限判断逻辑
│   │   │
│   │   ├── layouts/                  # 布局
│   │   │   ├── DefaultLayout.vue     # 默认布局
│   │   │   ├── AdminLayout.vue       # 后台管理布局
│   │   │   └── ChatLayout.vue        # 聊天页面布局
│   │   │
│   │   ├── router/                   # 路由
│   │   │   ├── index.ts              # 路由配置
│   │   │   ├── guards.ts             # 路由守卫
│   │   │   └── routes/               # 路由模块
│   │   │       ├── auth.ts
│   │   │       ├── chat.ts
│   │   │       ├── admin.ts
│   │   │       └── pet.ts
│   │   │
│   │   ├── stores/                   # Pinia状态管理
│   │   │   ├── index.ts              # Store入口
│   │   │   ├── auth.ts               # 认证状态
│   │   │   ├── user.ts               # 用户状态
│   │   │   ├── chat.ts               # 聊天状态
│   │   │   ├── knowledge.ts          # 知识库状态
│   │   │   └── app.ts                # 应用全局状态
│   │   │
│   │   ├── styles/                   # 样式
│   │   │   ├── index.scss            # 全局样式入口
│   │   │   ├── variables.scss        # SCSS变量
│   │   │   ├── mixins.scss           # SCSS混入
│   │   │   └── element-overrides.scss # Element Plus样式覆盖
│   │   │
│   │   ├── types/                    # TypeScript类型定义
│   │   │   ├── api.ts                # API响应类型
│   │   │   ├── auth.ts               # 认证相关类型
│   │   │   ├── user.ts               # 用户相关类型
│   │   │   ├── chat.ts               # 聊天相关类型
│   │   │   ├── knowledge.ts          # 知识库相关类型
│   │   │   ├── pet.ts                # 宠物相关类型
│   │   │   └── product.ts            # 产品相关类型
│   │   │
│   │   ├── utils/                    # 工具函数
│   │   │   ├── request.ts            # HTTP请求封装
│   │   │   ├── auth.ts               # Token管理
│   │   │   ├── format.ts             # 格式化工具
│   │   │   └── sse.ts                # SSE客户端封装
│   │   │
│   │   ├── views/                    # 页面
│   │   │   ├── auth/                 # 认证页面
│   │   │   │   ├── LoginView.vue
│   │   │   │   └── RegisterView.vue
│   │   │   ├── chat/                 # 聊天页面
│   │   │   │   ├── ChatView.vue
│   │   │   │   └── SessionList.vue
│   │   │   ├── knowledge/            # 知识库页面
│   │   │   │   ├── KnowledgeListView.vue
│   │   │   │   ├── KnowledgeDetailView.vue
│   │   │   │   └── DocumentManageView.vue
│   │   │   ├── pet/                  # 宠物页面
│   │   │   │   ├── PetListView.vue
│   │   │   │   └── PetDetailView.vue
│   │   │   └── admin/                # 后台管理页面
│   │   │       ├── DashboardView.vue
│   │   │       ├── UserManageView.vue
│   │   │       ├── RoleManageView.vue
│   │   │       ├── LogView.vue
│   │   │       └── ConfigView.vue
│   │   │
│   │   ├── App.vue                   # 根组件
│   │   └── main.ts                   # 应用入口
│   │
│   ├── public/                       # 静态资源
│   ├── index.html                    # HTML模板
│   ├── vite.config.ts                # Vite配置
│   ├── tsconfig.json                 # TypeScript配置
│   ├── package.json                  # 依赖配置
│   └── Dockerfile                    # Docker构建文件
│
├── docs/                             # 文档
│   ├── architecture.md               # 架构设计文档 (本文档)
│   ├── api.md                        # API接口文档
│   ├── deployment.md                 # 部署文档
│   └── development.md                # 开发指南
│
├── docker-compose.yml                # Docker Compose编排
├── nginx.conf                        # Nginx配置
├── .gitignore                        # Git忽略配置
└── README.md                         # 项目说明
```

---

> **文档结束** — 本架构设计文档描述了基于Transformer和RAG的智能售前客服机器人的完整系统架构，涵盖系统分层、模块划分、API设计、数据流转、数据模型及项目结构。各模块边界清晰，为后续开发、测试和部署提供完整的技术指导。
