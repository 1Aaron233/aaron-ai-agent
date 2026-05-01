# AI 算命大师

基于 **Spring AI + 阿里云百炼** 构建的 AI 算命应用，支持多轮对话、对话记忆、RAG 知识库问答、结构化输出等核心特性，同时提供多种 AI 调用方式的 Demo 示例。当前项目已补充一版 **前后端分离 RBAC 权限体系**，包含登录认证、JWT 鉴权、角色权限控制和前端路由守卫。

---

## 项目功能

- **多轮对话**：基于 `MessageChatMemoryAdvisor` 实现上下文记忆，支持连续追问
- **命理报告**：结构化输出，自动生成包含标题和建议列表的命理分析报告
- **RAG 知识库问答**：加载本地算命知识文档，结合向量检索进行精准问答
- **查询重写**：自动改写用户问题，提升检索质量
- **多查询扩展**：将一个问题扩展为多个相关查询，提高检索覆盖率
- **关键词增强**：启动时自动为文档补充关键词元数据，优化检索效果
- **RBAC 权限体系**：支持登录、角色、权限、菜单和接口级鉴权
- **前后端分离鉴权**：前端基于 Token 维持登录态，后端基于 Spring Security + JWT 校验身份

---

## 技术栈

| 分类 | 技术 |
|------|------|
| 语言 / 框架 | Java 21 + Spring Boot 3.4.5 |
| AI 框架 | Spring AI 1.0.0 + LangChain4j 1.0.0-beta2 |
| 大模型 | 阿里云百炼 DashScope（qwen-turbo-2025-04-28） |
| 本地模型 | Ollama（deepseek-r1:1.5b） |
| 向量数据库 | SimpleVectorStore（内存）/ PgVector（PostgreSQL） |
| 序列化 | Kryo 5.6.2（对话记忆持久化） |
| 工具库 | Hutool、Lombok |
| API 文档 | Knife4j 4.4.0 |
| 权限认证 | Spring Security + JWT |
| 前端 | Vue 3 + Vite + Vue Router + Axios |

---

## 目录结构

```
src/main/java/com/aaron/aaronaiagent/
├── AaronAiAgentApplication.java      # 启动类
├── app/
│   └── FortuneApp.java               # 核心业务：算命大师应用
├── controller/
│   └── HealthController.java         # 健康检查接口
├── rag/                              # RAG 知识库模块
│   ├── FortuneAppDocumentLoader.java        # 文档加载器
│   ├── FortuneAppVectorStoreConfig.java     # 内存向量库配置
│   ├── FortuneAppRagCloudAdvisorConfig.java # 云知识库配置
│   ├── FortuneAppRagCustomAdvisorFactory.java  # 自定义 RAG Advisor
│   ├── FortuneAppContextualQueryAugmenterFactory.java # 上下文增强器
│   ├── PgVectorVectorStoreConfig.java       # PgVector 配置（pgvector profile）
│   ├── QueryRewriter.java            # 查询重写
│   ├── MyTokenTextSplitter.java      # 文本切分
│   └── MyKeywordEnricher.java        # 关键词增强
├── advisor/
│   ├── MyLoggerAdvisor.java          # 日志 Advisor
│   └── ReReadingAdvisor.java         # Re2 推理增强 Advisor
├── chatmemory/
│   └── FileBasedChatMemory.java      # 文件持久化对话记忆
├── common/
│   └── ApiResponse.java              # 统一响应结构
├── security/
│   ├── SecurityConfig.java           # Spring Security 配置
│   ├── JwtTokenProvider.java         # JWT 生成与解析
│   ├── JwtAuthenticationFilter.java  # JWT 认证过滤器
│   └── SecurityUser.java             # 登录用户模型
├── rbac/
│   ├── model/                        # 用户、角色、权限模型
│   ├── dto/                          # 认证与管理接口 DTO
│   └── service/                      # RBAC 业务服务与内存用户仓库
└── demo/
    ├── invoke/                       # 5 种 AI 调用方式 Demo
    │   ├── SdkAiInvoke.java          # 阿里云 SDK 调用
    │   ├── HttpAiInvoke.java         # HTTP 方式调用
    │   ├── SpringAiAiInvoke.java     # Spring AI 调用
    │   ├── LangChainAiInvoke.java    # LangChain4j 调用
    │   └── OllamaAiInvoke.java       # Ollama 本地模型调用
    └── rag/
        └── MultiQueryExpanderDemo.java  # 多查询扩展 Demo

src/main/resources/
├── application.yml
└── document/                         # 算命知识库文档
    ├── 算命常见问题和回答 - 命理篇.md
    ├── 手相常见问题和回答 - 手相篇.md
    ├── 风水常见问题和回答 - 风水篇.md
    └── 流年运势常见问题和回答 - 流年运势篇.md

fortune-master-frontend/src/
├── api/index.js                      # 前端 API 和 SSE 请求封装
├── auth.js                           # Token、用户态、菜单状态管理
├── router/index.js                   # 路由与权限守卫
└── views/
    ├── Home.vue                      # 首页
    ├── Login.vue                     # 登录页
    ├── Dashboard.vue                 # RBAC 工作台
    └── FortuneMaster.vue             # AI 对话页
```

---

## RBAC 权限体系

当前项目新增的是一版轻量的、前后端分离风格的 RBAC 骨架，重点先打通登录、鉴权、菜单和接口权限控制。

### 设计说明

- 后端使用 `Spring Security + JWT`
- 前端使用 `Vue Router + localStorage Token`
- 页面权限和接口权限统一按 `permission code` 控制
- 当前用户数据为 **内存种子数据**，方便快速演示，不依赖数据库

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

### 当前主要鉴权接口

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/auth/login` | `POST` | 登录并获取 JWT |
| `/api/auth/profile` | `GET` | 获取当前登录用户信息 |
| `/api/auth/menus` | `GET` | 获取当前用户菜单 |
| `/api/admin/users` | `GET` | 获取用户列表，需要 `system:user:list` |
| `/api/admin/roles` | `GET` | 获取角色列表，需要 `system:role:list` |

### 当前受保护页面

| 页面 | 权限 |
|------|------|
| `/dashboard` | `dashboard:view` |
| `/dashboard/users` | `system:user:list` |
| `/dashboard/roles` | `system:role:list` |
| `/fortune-master` | `ai:chat` |

### AI 接口权限

以下 AI 接口已纳入登录鉴权，要求用户具备 `ai:chat` 权限：

- `/api/ai/fortune_app/chat/sync`
- `/api/ai/fortune_app/chat/sse`
- `/api/ai/fortune_app/report`
- `/api/ai/fortune_app/chat/rag`

---

## 快速开始

### 1. 配置 API Key

编辑 `src/main/resources/application.yml`：

```yaml
spring:
  ai:
    dashscope:
      api-key: 你的阿里云DashScope密钥
```

阿里云 DashScope API Key 在 [百炼控制台](https://bailian.console.aliyun.com/) 申请，新用户有免费额度。

建议通过环境变量注入，不要把密钥写进代码：

```bash
export DASHSCOPE_API_KEY=你的阿里云DashScope密钥
```

### 2. 启动项目

```bash
mvn spring-boot:run
```

启动时会自动加载 4 篇知识库文档并完成向量化（约需 30 秒）。

- 健康检查：`http://localhost:8123/api/health`
- API 文档：`http://localhost:8123/api/swagger-ui.html`

### 3. 启动前端（新增）

前端目录：`fortune-master-frontend`

```bash
cd fortune-master-frontend
npm install
npm run dev
```

默认访问地址：

- 前端首页：`http://localhost:5173`
- 登录页：`http://localhost:5173/login`
- RBAC 工作台：`http://localhost:5173/dashboard`
- 算命大师对话页：`http://localhost:5173/fortune-master`

前端会默认请求后端 `http://localhost:8123/api`，项目已补充跨域配置、JWT 登录态和 SSE 流式聊天接口。

### 4. 登录系统

启动前后端后，先访问登录页：

```text
http://localhost:5173/login
```

可使用以下演示账号：

```text
admin / admin123
fortune / fortune123
```

登录成功后：

- 管理员进入 `/dashboard` 后可看到用户管理、角色管理和 AI 对话入口
- 顾问账号进入 `/dashboard` 后只能看到工作台和 AI 对话入口
- 未登录访问受保护页面时，会被前端路由守卫跳转到 `/login`
- 未携带 Token 访问后端受保护接口时，会返回 `401`
- 无权限访问管理接口时，会返回 `403`

### 5. 运行测试

在 IDE 中直接运行测试类，推荐顺序：

| 测试类 | 功能 |
|--------|------|
| `SpringAiAiInvokeTest` | 验证 API Key 是否可用 |
| `FortuneAppTest#testChat` | 多轮对话 + 记忆 |
| `FortuneAppTest#doChatWithReport` | 结构化命理报告输出 |
| `FortuneAppTest#doChatWithRag` | RAG 知识库问答 |
| `MultiQueryExpanderDemoTest` | 多查询扩展 |

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

```
加载 4 篇 Markdown 知识库文档
  → MyKeywordEnricher（AI 为每个文档提取关键词，每次间隔 1s）
  → DashScope Embedding 向量化
  → 存入 SimpleVectorStore（内存）
```

### RAG 问答流程

```
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

## 配置说明

### 使用 Ollama 本地模型

确保本地已安装 Ollama 并拉取模型：

```bash
ollama pull deepseek-r1:1.5b
ollama serve
```

`application.yml` 中已默认配置：

```yaml
spring:
  ai:
    ollama:
      base-url: http://localhost:11434
      chat:
        model: deepseek-r1:1.5b
```

### 启用 PgVector（可选）

默认使用内存向量库，如需持久化向量数据，启动 PostgreSQL 并激活 `pgvector` profile：

```bash
docker run -d -e POSTGRES_PASSWORD=postgres -p 5432:5432 pgvector/pgvector:pg16
```

修改 `application.yml`：

```yaml
spring:
  profiles:
    active: pgvector  # 替换 local
  autoconfigure:
    exclude: []       # 去掉 DataSourceAutoConfiguration 的排除
  datasource:
    url: jdbc:postgresql://localhost:5432/postgres
    username: postgres
    password: postgres
```

---

## Demo 示例

`demo/invoke/` 目录提供了 5 种调用 AI 大模型的方式对比：

| 文件 | 方式 | 说明 |
|------|------|------|
| `SdkAiInvoke` | 阿里云 SDK | 直接使用 DashScope SDK，原始调用 |
| `HttpAiInvoke` | HTTP 请求 | 使用 Hutool 直接发 HTTP 请求 |
| `SpringAiAiInvoke` | Spring AI | 框架封装，最简洁 |
| `LangChainAiInvoke` | LangChain4j | 另一种主流 AI 框架 |
| `OllamaAiInvoke` | Ollama | 调用本地部署的模型 |

---

## 注意事项

- 项目启动较慢（约 30s）：启动时会调用 AI 接口为文档生成关键词，属于正常现象
- 关键词增强调用频率已限制为每秒 1 次，避免触发 429 限流
- 对话记忆默认使用内存存储（重启后清空），如需持久化可切换为 `FileBasedChatMemory`
- `PgVectorVectorStoreConfig` 仅在 `pgvector` profile 下激活，默认不加载
- 当前 RBAC 用户、角色、权限使用内存种子数据，重启后不会丢失，但也不支持后台动态新增
- SSE 对话接口因浏览器 `EventSource` 限制，当前通过查询参数 `accessToken` 传递 JWT
- 如需把权限体系用于生产，建议下一步接入数据库表：`user / role / permission / user_role / role_permission`
