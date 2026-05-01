# AI 算命大师

基于 **Spring AI + 阿里云百炼** 构建的 AI 算命应用，支持多轮对话、对话记忆、RAG 知识库问答、结构化输出等核心特性，同时提供多种 AI 调用方式的 Demo 示例。当前项目已补充一版 **前后端分离 RBAC 权限体系**，包含登录认证、JWT 鉴权、角色权限控制、菜单控制和基础后台管理能力。

---

## 架构概览

项目当前由两部分组成：

- 后端：`Spring Boot + Spring Security + Spring AI + PostgreSQL`
- 前端：`Vue 3 + Vite + Vue Router + Axios`

系统包含两条主线能力：

- AI 能力：多轮对话、命理报告、RAG、查询改写、多查询扩展
- RBAC 能力：登录、JWT 鉴权、菜单权限、用户角色权限管理

### 整体链路

```text
前端登录页
  → /api/auth/login
  → 返回 JWT token
  → 前端保存 token 到 localStorage
  → 前端请求 /api/auth/profile 和 /api/auth/menus
  → 后端 JwtAuthenticationFilter 解析 Bearer Token
  → Spring Security 完成认证
  → RBAC 数据库查询用户、角色、权限、菜单
  → 前端工作台按权限展示菜单与管理页面
```

---

## 项目功能

### AI 能力

- **多轮对话**：基于 `MessageChatMemoryAdvisor` 实现上下文记忆，支持连续追问
- **命理报告**：结构化输出，自动生成包含标题和建议列表的命理分析报告
- **RAG 知识库问答**：加载本地算命知识文档，结合向量检索进行精准问答
- **查询重写**：自动改写用户问题，提升检索质量
- **多查询扩展**：将一个问题扩展为多个相关查询，提高检索覆盖率
- **关键词增强**：启动时自动为文档补充关键词元数据，优化检索效果

### RBAC 能力

- **登录认证**：用户名密码登录，返回 JWT
- **接口鉴权**：后端基于 `Spring Security + JWT`
- **菜单控制**：前端工作台菜单按权限返回
- **角色权限控制**：页面权限和接口权限统一按 `permission code` 控制
- **基础后台管理**：
  - 用户列表查询
  - 新增用户
  - 编辑用户昵称、状态、密码
  - 给用户重新分配角色
  - 角色列表查询
  - 新增角色
  - 编辑角色名称
  - 给角色重新分配权限
  - 权限列表查询

---

## 技术栈

| 分类 | 技术 |
|------|------|
| 语言 / 框架 | Java 21 + Spring Boot 3.4.5 |
| AI 框架 | Spring AI 1.0.0 + LangChain4j 1.0.0-beta2 |
| 大模型 | 阿里云百炼 DashScope（qwen-turbo-2025-04-28） |
| 本地模型 | Ollama（deepseek-r1:1.5b） |
| 向量数据库 | SimpleVectorStore（内存）/ PgVector（PostgreSQL） |
| 权限认证 | Spring Security + JWT |
| 关系数据库 | PostgreSQL 16 |
| 序列化 | Kryo 5.6.2 |
| 工具库 | Hutool、Lombok |
| API 文档 | Knife4j 4.4.0 |
| 前端 | Vue 3 + Vite + Vue Router + Axios |

---

## 目录结构

```text
src/main/java/com/aaron/aaronaiagent/
├── AaronAiAgentApplication.java      # 启动类
├── app/
│   └── FortuneApp.java               # 核心业务：算命大师应用
├── advisor/
│   ├── MyLoggerAdvisor.java          # 日志 Advisor
│   └── ReReadingAdvisor.java         # 推理增强 Advisor
├── chatmemory/
│   └── FileBasedChatMemory.java      # 文件持久化对话记忆
├── common/
│   ├── ApiResponse.java              # 统一响应结构
│   └── GlobalExceptionHandler.java   # 全局业务异常处理
├── controller/
│   ├── HealthController.java         # 健康检查接口
│   ├── AuthController.java           # 登录、用户信息、菜单接口
│   └── AdminController.java          # 用户、角色、权限管理接口
├── rbac/
│   ├── dto/                          # 认证与管理接口 DTO
│   ├── model/                        # 用户、角色、权限、菜单模型
│   └── service/                      # RBAC 业务服务、JDBC 仓库、数据库初始化
├── rag/
│   ├── FortuneAppDocumentLoader.java
│   ├── FortuneAppVectorStoreConfig.java
│   ├── FortuneAppRagCloudAdvisorConfig.java
│   ├── FortuneAppRagCustomAdvisorFactory.java
│   ├── FortuneAppContextualQueryAugmenterFactory.java
│   ├── PgVectorVectorStoreConfig.java
│   ├── QueryRewriter.java
│   ├── MyTokenTextSplitter.java
│   └── MyKeywordEnricher.java
├── security/
│   ├── SecurityConfig.java
│   ├── JwtTokenProvider.java
│   ├── JwtAuthenticationFilter.java
│   └── SecurityUser.java
└── demo/
    ├── invoke/
    └── rag/

src/main/resources/
├── application.yml                   # 主配置
├── schema.sql                        # RBAC 表结构初始化
└── document/                         # 算命知识库文档

fortune-master-frontend/src/
├── api/index.js                      # 前端 API 和 SSE 请求封装
├── auth.js                           # Token、用户态、菜单状态管理
├── router/index.js                   # 路由与权限守卫
└── views/
    ├── Home.vue
    ├── Login.vue
    ├── Dashboard.vue
    └── FortuneMaster.vue
```

---

## RBAC 设计

### 数据库存储

当前 RBAC 已从内存仓库改为 **基于 PostgreSQL 的数据库实现**。

数据库表如下：

- `rbac_user`：用户表
- `rbac_role`：角色表
- `rbac_permission`：权限表
- `rbac_user_role`：用户角色关联表
- `rbac_role_permission`：角色权限关联表
- `rbac_menu`：菜单表

应用启动时会自动执行 [schema.sql](/Users/a1234/person/aaron-ai-agent/src/main/resources/schema.sql:1) 创建表，并初始化一份演示数据。

### 当前内置角色

| 角色编码 | 角色名称 | 权限 |
|------|------|------|
| `ADMIN` | 超级管理员 | `dashboard:view`、`ai:chat`、`system:user:list`、`system:role:list` |
| `CONSULTANT` | 命理顾问 | `dashboard:view`、`ai:chat` |

### 当前内置账号

| 用户名 | 密码 | 角色 | 说明 |
|------|------|------|------|
| `admin` | `admin123` | `ADMIN` | 可访问工作台、AI 对话、用户管理、角色管理 |
| `fortune` | `fortune123` | `CONSULTANT` | 可访问工作台和 AI 对话，不可访问管理页 |

### 受保护页面

| 页面 | 权限 |
|------|------|
| `/dashboard` | `dashboard:view` |
| `/dashboard/users` | `system:user:list` |
| `/dashboard/roles` | `system:role:list` |
| `/fortune-master` | `ai:chat` |

### AI 接口权限

以下 AI 接口要求用户具备 `ai:chat` 权限：

- `/api/ai/fortune_app/chat/sync`
- `/api/ai/fortune_app/chat/sse`
- `/api/ai/fortune_app/report`
- `/api/ai/fortune_app/chat/rag`

---

## API 概览

### 认证接口

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/auth/login` | `POST` | 登录并获取 JWT |
| `/api/auth/profile` | `GET` | 获取当前登录用户信息 |
| `/api/auth/menus` | `GET` | 获取当前用户菜单 |

### 管理接口

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/admin/users` | `GET` | 获取用户列表 |
| `/api/admin/users` | `POST` | 新增用户并分配角色 |
| `/api/admin/users/{userId}` | `PUT` | 修改用户信息、状态、角色 |
| `/api/admin/roles` | `GET` | 获取角色列表 |
| `/api/admin/roles` | `POST` | 新增角色并分配权限 |
| `/api/admin/roles/{roleCode}` | `PUT` | 修改角色名称和权限 |
| `/api/admin/permissions` | `GET` | 获取权限列表 |

### JWT 使用方式

- 登录接口 `/api/auth/login` 是匿名接口
- 登录成功后，后续受保护接口必须携带：

```http
Authorization: Bearer <token>
```

- 普通接口通过请求头传递 JWT
- SSE 对话接口通过查询参数 `accessToken` 传递 JWT

---

## 页面说明

### 前端访问地址

- 首页：`http://localhost:5173`
- 登录页：`http://localhost:5173/login`
- RBAC 工作台：`http://localhost:5173/dashboard`
- AI 对话页：`http://localhost:5173/fortune-master`

### 工作台说明

- `/dashboard`：查看当前登录用户角色和权限
- `/dashboard/users`：管理用户、分配角色
- `/dashboard/roles`：管理角色、分配权限

登录成功后：

- 管理员可以进入所有后台页面
- 顾问账号只能进入工作台首页和 AI 对话页
- 未登录访问受保护页面时，会被前端路由守卫跳转到 `/login`

---

## 本地开发

### 1. 配置 DashScope API Key

建议通过环境变量注入：

```bash
export DASHSCOPE_API_KEY=你的阿里云DashScope密钥
```

### 2. 准备 PostgreSQL

默认数据库连接通过以下环境变量配置：

```bash
export RBAC_DB_URL=jdbc:postgresql://localhost:5432/postgres
export RBAC_DB_USERNAME=postgres
export RBAC_DB_PASSWORD=postgres
```

#### 方案 A：使用 Docker

```bash
docker run -d --name aaron-ai-agent-postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  pgvector/pgvector:pg16
```

#### 方案 B：使用本机 Homebrew PostgreSQL

```bash
brew services list
brew services start postgresql@16
```

确认端口监听：

```bash
lsof -nP -iTCP:5432 -sTCP:LISTEN
```

确认 `postgres` 用户可登录：

```bash
PGPASSWORD=postgres psql -h localhost -U postgres -d postgres -c "select current_user, current_database();"
```

### 3. 启动后端

```bash
mvn spring-boot:run
```

启动时会自动：

- 加载 4 篇知识库文档
- 执行文档向量化
- 创建 RBAC 表结构
- 初始化管理员、顾问、角色、权限、菜单数据

可访问：

- 健康检查：`http://localhost:8123/api/health`
- API 文档：`http://localhost:8123/api/swagger-ui.html`

### 4. 启动前端

```bash
cd fortune-master-frontend
npm install
npm run dev
```

前端默认请求后端：

```text
http://localhost:8123/api
```

### 5. 演示账号登录

```text
admin / admin123
fortune / fortune123
```

---

## 核心流程

### RBAC 登录鉴权流程

```text
用户访问前端受保护页面
  → Vue Router 路由守卫检查本地 Token
  → 未登录则跳转 /login
  → 登录成功后调用 /api/auth/login 获取 JWT
  → 前端保存 Token 到 localStorage
  → 前端调用 /api/auth/profile 和 /api/auth/menus 获取用户信息和菜单
  → 后续请求在 Authorization 头中携带 Bearer Token
  → 后端 JwtAuthenticationFilter 解析 Token
  → Spring Security 根据权限码进行接口授权
```

### 启动初始化（向量库）

```text
加载 4 篇 Markdown 知识库文档
  → MyKeywordEnricher（AI 为每个文档提取关键词，每次间隔 1s）
  → DashScope Embedding 向量化
  → 存入 SimpleVectorStore（内存）
```

### RAG 问答流程

```text
用户输入
  → QueryRewriter（改写查询）
  → MessageChatMemoryAdvisor（注入历史对话）
  → MyLoggerAdvisor（日志记录）
  → QuestionAnswerAdvisor
      → VectorStore 相似度检索（Top-3）
      → 检索结果注入 Prompt
  → DashScope 大模型（qwen-turbo）
  → 返回回答
```

---

## Demo 与测试

### Demo 示例

`demo/invoke/` 目录提供了 5 种调用 AI 大模型的方式对比：

| 文件 | 方式 | 说明 |
|------|------|------|
| `SdkAiInvoke` | 阿里云 SDK | 直接使用 DashScope SDK |
| `HttpAiInvoke` | HTTP 请求 | 使用 Hutool 直接发 HTTP 请求 |
| `SpringAiAiInvoke` | Spring AI | 框架封装，最简洁 |
| `LangChainAiInvoke` | LangChain4j | 另一种主流 AI 框架 |
| `OllamaAiInvoke` | Ollama | 调用本地部署模型 |

### 推荐测试顺序

| 测试类 | 功能 |
|--------|------|
| `SpringAiAiInvokeTest` | 验证 API Key 是否可用 |
| `FortuneAppTest#testChat` | 多轮对话 + 记忆 |
| `FortuneAppTest#doChatWithReport` | 结构化命理报告输出 |
| `FortuneAppTest#doChatWithRag` | RAG 知识库问答 |
| `MultiQueryExpanderDemoTest` | 多查询扩展 |

---

## 常见问题排查

### 1. `/api/auth/menus` 返回 `401`

先确认请求头里是否携带：

```http
Authorization: Bearer <token>
```

如果是普通接口，没有这个请求头就一定会返回 `401`。

### 2. 登录成功了，但 `/api/auth/menus` 仍报错

如果不是 `401`，而是后端日志里出现 PostgreSQL SQL 错误，优先检查：

- 菜单查询 SQL 是否兼容 PostgreSQL
- `SELECT DISTINCT ... ORDER BY ...` 中排序字段是否出现在 select list 中

### 3. 本机 PostgreSQL 启动了，但应用连不上 `localhost:5432`

检查：

```bash
lsof -nP -iTCP:5432 -sTCP:LISTEN
```

如果没有监听，查看 `postgresql.conf` 是否配置了：

```conf
listen_addresses = 'localhost'
```

### 4. PostgreSQL 报 `role "postgres" does not exist`

说明数据库服务存在，但默认登录角色缺失。需要先补齐 `postgres` 登录角色，再启动应用。

### 5. SSE 对话接口无法携带 Authorization 头

这是浏览器 `EventSource` 的限制。当前实现通过查询参数：

```text
accessToken=<jwt>
```

来完成 SSE 鉴权。

---

## 注意事项

- 项目启动较慢（约 30 秒），因为启动时会调用 AI 接口为文档生成关键词
- 关键词增强调用频率已限制为每秒 1 次，避免触发 429 限流
- 对话记忆默认使用内存存储，如需持久化可切换为 `FileBasedChatMemory`
- `PgVectorVectorStoreConfig` 仅在 `pgvector` profile 下激活，默认不加载
- 当前 RBAC 用户、角色、权限、菜单已落 PostgreSQL，默认由启动初始化脚本写入一份演示数据
- PostgreSQL 对部分 SQL 语法比 MySQL 更严格，开发时建议优先按 PostgreSQL 语法验证
- 如需把权限体系用于生产，建议继续补齐删除能力、审计日志、密码修改、刷新 Token 和数据范围控制
