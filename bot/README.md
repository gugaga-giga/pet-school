# 智能售前客服机器人

基于 Transformer 和 RAG 的智能售前客服机器人系统，以宠物用品行业为应用场景。

## 项目简介

本项目是一个全栈智能客服系统，集成了 RAG（检索增强生成）管线、意图识别、个性化推荐引擎等核心模块。用户可以通过自然语言与机器人对话，系统基于知识库文档进行精准检索，结合大语言模型生成专业回答，并智能推荐相关产品。

## 技术栈

### 后端
- **FastAPI** - Web 框架
- **LangChain** - LLM 编排框架
- **FAISS** - 向量检索引擎
- **BGE-M3** - 文本嵌入模型（1024维）
- **通义千问 (Qwen-Plus)** - 大语言模型
- **SQLAlchemy + Alembic** - ORM 与数据库迁移
- **MySQL 8.0** - 关系型数据库
- **Redis** - 缓存
- **jieba** - 中文分词
- **BM25 + RRF + Rerank + MMR** - 混合检索与排序

### 前端
- **Vue 3 + TypeScript** - 前端框架
- **Vite** - 构建工具
- **Element Plus** - UI 组件库
- **Pinia** - 状态管理
- **ECharts** - 数据可视化
- **markdown-it + highlight.js** - Markdown 渲染与代码高亮
- **Sass** - CSS 预处理器

## 核心架构

### RAG 管线
```
用户提问 → 意图识别 → 文档解析 → 分块(Chunker) → BGE-M3 Embedding
→ FAISS向量检索 + BM25关键词检索 → RRF融合 → Rerank精排 → MMR去重
→ Qwen LLM 生成回答 → 个性化产品推荐
```

### 混合检索策略
- **向量检索 (FAISS)**: 基于语义相似度的高维向量检索
- **关键词检索 (BM25)**: 基于 TF-IDF 的精确关键词匹配
- **RRF 融合**: Reciprocal Rank Fusion 综合排序
- **Rerank 精排**: 二次重排序提升相关性
- **MMR 去重**: Maximal Marginal Relevance 保证结果多样性

## 功能模块

- 🔐 **用户认证** - JWT 登录注册、RBAC 权限管理
- 💬 **智能对话** - 流式响应、Markdown 渲染、上下文记忆
- 📚 **知识库管理** - 创建/编辑/删除知识库
- 📄 **文档管理** - 支持 PDF/Word/Excel/TXT 上传与解析
- 🐕 **宠物管理** - 宠物档案信息维护
- 🛍️ **产品管理** - 商品信息与推荐规则
- 📊 **数据仪表盘** - 对话统计与可视化分析
- ⚙️ **后台管理** - 用户管理、日志查看、系统配置

## 快速开始

### 环境要求
- Python 3.10+
- Node.js 18+
- MySQL 8.0+
- Redis

### 数据库初始化
```bash
mysql -u root -p < docs/sql/init.sql
```

### 后端启动
```bash
cd backend
pip install -r requirements.txt
cp .env.example .env  # 编辑配置数据库、API密钥等
uvicorn app.main:app --host 0.0.0.0 --port 8000
```

### 前端启动
```bash
cd frontend
npm install
npm run dev
```

前端开发服务器运行在 `http://localhost:3000`，API 自动代理到后端 `http://localhost:8000`。

## 项目结构

```
bot/
├── backend/                 # 后端服务
│   ├── app/
│   │   ├── api/v1/          # API 路由
│   │   ├── core/            # 核心配置
│   │   ├── intent/          # 意图识别模块
│   │   ├── llm/             # LLM 调用服务
│   │   ├── models/          # ORM 模型
│   │   ├── rag/             # RAG 管线
│   │   ├── recommend/       # 推荐引擎
│   │   ├── schemas/         # 数据模型
│   │   ├── services/        # 业务逻辑层
│   │   └── utils/           # 工具函数
│   ├── migrations/          # 数据库迁移
│   └── requirements.txt     # Python 依赖
├── frontend/                # 前端应用
│   ├── src/
│   │   ├── api/             # API 请求
│   │   ├── components/      # 公共组件
│   │   ├── views/           # 页面视图
│   │   ├── stores/          # 状态管理
│   │   └── styles/          # 全局样式
│   └── package.json
├── docs/                    # 项目文档
│   ├── architecture.md      # 架构设计
│   ├── retrieval_optimization.md  # 检索优化
│   ├── thesis.md            # 毕业论文
│   └── sql/init.sql         # 数据库初始化
└── README.md
```

## 文档

- [架构设计文档](docs/architecture.md)
- [检索优化设计](docs/retrieval_optimization.md)
- [毕业论文](docs/thesis.md)

## 许可证

MIT License
