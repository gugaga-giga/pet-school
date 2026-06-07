# PetSchool AI模块接口文档

## 1. 概述

- AI模块作为PetSchool内部模块，统一使用PetSchool JWT认证
- 基础路径: `/ai`
- 认证方式: Bearer Token (JWT)
- 所有接口需在Header中携带 `Authorization: Bearer {token}`

## 2. 统一聊天接口

### POST /ai/chat (SSE流式)

- **描述**: AI聊天主接口，支持SSE流式输出
- **Content-Type**: `application/json`
- **Response Content-Type**: `text/event-stream`
- **请求体**:

```json
{
  "message": "有什么训练课程？",
  "sessionId": 1
}
```

> `sessionId` 可选，不传则创建新会话

- **响应**: SSE事件流，每个事件为JSON字符串
  - `event: message` — 聊天内容片段
  - `event: done` — 结束标记
- **认证**: JWT Bearer Token（也支持query参数 `?token=xxx` 用于SSE连接）
- **意图类型**: FAQ / Course / PetKnowledge / Order / PetProfile / UserInfo / Greeting / Other

### POST /ai/chat/sync (同步)

- **描述**: AI聊天同步接口，等待完整响应
- **请求体**: 同上
- **响应**:

```json
{
  "code": 200,
  "data": {
    "content": "回复内容",
    "sessionId": 1,
    "intentType": "Course"
  }
}
```

## 3. 会话管理接口

### GET /ai/sessions

- **描述**: 获取当前用户的会话列表
- **响应**:

```json
{
  "code": 200,
  "data": [
    {
      "id": 1,
      "userId": 1,
      "title": "课程咨询",
      "createdAt": "2026-06-06T20:00:00",
      "updatedAt": "2026-06-06T20:00:00"
    }
  ]
}
```

### GET /ai/sessions/{id}/messages

- **描述**: 获取指定会话的消息列表
- **路径参数**: `id` (会话ID)
- **响应**:

```json
{
  "code": 200,
  "data": [
    {
      "id": 1,
      "sessionId": 1,
      "role": "user",
      "content": "有什么训练课程？",
      "intentType": null,
      "createdAt": "2026-06-06T20:00:00"
    },
    {
      "id": 2,
      "sessionId": 1,
      "role": "assistant",
      "content": "PetSchool提供以下课程...",
      "intentType": "Course",
      "sources": [],
      "createdAt": "2026-06-06T20:00:01"
    }
  ]
}
```

### DELETE /ai/sessions/{id}

- **描述**: 删除指定会话及其所有消息
- **路径参数**: `id` (会话ID)
- **响应**:

```json
{
  "code": 200,
  "message": "删除成功"
}
```

## 4. 记忆管理接口

### GET /ai/memory

- **描述**: 获取当前用户的长期记忆档案
- **响应**:

```json
{
  "code": 200,
  "data": [
    {
      "id": 1,
      "userId": 1,
      "key": "pet_preference",
      "value": "喜欢大型犬",
      "source": "chat",
      "createdAt": "2026-06-06T20:00:00",
      "updatedAt": "2026-06-06T20:00:00"
    }
  ]
}
```

## 5. RAG同步接口

### POST /course/admin/sync-rag/{id}

- **描述**: 同步指定课程到RAG知识库（管理员）
- **认证**: JWT Bearer Token（需要管理员角色）
- **路径参数**: `id` (课程ID)
- **响应**:

```json
{
  "code": 200,
  "message": "同步成功"
}
```

### POST /course/admin/sync-rag-all

- **描述**: 同步所有课程到RAG知识库（管理员）
- **认证**: JWT Bearer Token（需要管理员角色）
- **响应**:

```json
{
  "code": 200,
  "data": {
    "total": 6,
    "success": 6
  },
  "message": "同步完成: 6/6 成功"
}
```

### POST /api/v1/documents/sync-text (FastAPI侧)

- **描述**: 同步文本内容到RAG知识库（供PetSchool内部调用）
- **基础URL**: `http://localhost:8000`
- **请求体**:

```json
{
  "title": "课程-基础服从训练",
  "content": "课程名称：基础服从训练\n课程描述：...",
  "knowledge_base_id": 1,
  "doc_type": "course",
  "ref_id": 1
}
```

- **响应**:

```json
{
  "code": 0,
  "message": "同步成功",
  "data": {
    "doc_id": 1,
    "chunk_count": 3
  }
}
```

## 6. 错误码

| 错误码 | 描述 |
|--------|------|
| 200 | 成功 |
| 401 | 未登录或token已过期 |
| 500 | 服务器内部错误 |

## 7. JWT认证说明

- **算法**: HS256
- **SecretKey**: `PetSchoolSecretKey2026ForJwtTokenGeneration!!`
- **有效期**: 24小时
- **Token载荷**:

```json
{
  "sub": "userId",
  "username": "admin",
  "role": 2,
  "iat": "...",
  "exp": "..."
}
```

- SSE连接支持query参数传token: `/ai/chat?token=xxx`
