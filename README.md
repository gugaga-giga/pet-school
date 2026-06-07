# Pet School 宠物学校管理系统

[![Version](https://img.shields.io/badge/version-v1.2.0-blue)](https://github.com/gugaga-giga/pet-school)

## 项目简介

Pet School 是一个基于 Spring Boot + Vue 3 的宠物学校综合管理系统，集成 AI 售前客服机器人，提供智能对话、知识库检索、个性化推荐等 AI 能力。

## 技术栈

### 宠物学校系统
- **后端**：Spring Boot 3.4.1 + MyBatis + MySQL + JWT
- **前端**：Vue 3 + Vite + ECharts + Axios

### AI 售前客服系统
- **后端**：FastAPI + LangChain + FAISS + BGE-M3 + Qwen
- **前端**：Vue 3 + TypeScript + Element Plus
- **检索**：向量检索(FAISS) + BM25关键词检索 + RRF融合 + Rerank精排 + MMR去重
- **LLM**：通义千问 (Qwen-Plus)

## 功能模块

### v1.2.0 新增
- 🤖 **AI 售前客服系统** - 基于 Transformer 和 RAG 的智能客服机器人
  - 智能对话：流式响应、Markdown 渲染、上下文记忆
  - 知识库管理：创建/编辑/删除知识库
  - 文档管理：支持 PDF/Word/Excel/TXT 上传与解析
  - 意图识别：自动识别用户咨询意图
  - 个性化推荐：基于用户偏好推荐相关产品
  - 混合检索：向量检索 + BM25 + RRF融合 + Rerank精排

### v1.1.0
- 宠物系统：完整的宠物档案管理

### 现有功能
- 用户认证、宠物医疗、寄养、课程、优惠券、钱包、证书、直播等

## 快速开始

### 宠物学校系统

#### 后端启动（JDK 17）
\\ash
cd PetSchool/pet-school
mvn spring-boot:run
\
#### 前端启动
\\ash
cd PetSchool/pet-school-ui
npm install
npm run dev
\
### AI 售前客服系统

#### 后端启动（Python 3.10+）
\\ash
cd bot/backend
pip install -r requirements.txt
uvicorn app.main:app --host 0.0.0.0 --port 8000
\
#### 前端启动
\\ash
cd bot/frontend
npm install
npm run dev
\
## 项目结构

\pet-school/
├── PetSchool/              # 宠物学校系统
│   ├── pet-school/         # 后端（Spring Boot）
│   └── pet-school-ui/      # 前端（Vue 3）
├── bot/                    # AI 售前客服系统
│   ├── backend/            # 后端（FastAPI + RAG）
│   └── frontend/           # 前端（Vue 3 + TypeScript）
└── README.md
\
## 服务端口

| 服务 | 端口 | 地址 |
|------|------|------|
| 宠物学校后端 | 8080 | http://localhost:8080 |
| 宠物学校前端 | 5173 | http://localhost:5173 |
| AI 客服后端 | 8000 | http://localhost:8000 |
| AI 客服前端 | 3000 | http://localhost:3000 |

## 更新日志

### v1.2.0 (2026-06-07)
- 🤖 新增 AI 售前客服系统（FastAPI + RAG + FAISS + BGE-M3 + Qwen）
- 📚 支持知识库管理与文档解析（PDF/Word/Excel/TXT）
- 🔍 混合检索管线：向量检索 + BM25 + RRF融合 + Rerank精排 + MMR去重
- 💡 意图识别与个性化产品推荐
- 🎨 AI 客服前端界面（Vue 3 + TypeScript + Element Plus）

### v1.1.0 (2026-06-05)
- ✨ 新增宠物系统

### v1.0.0 (2026-06-02)
- 🎉 项目初始版本发布

## 许可证

MIT License
