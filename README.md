# AI 算命大师

基于 **Spring AI + 阿里云百炼** 构建的 AI 算命应用，支持多轮对话、对话记忆、RAG 知识库问答、结构化输出等核心特性，同时提供多种 AI 调用方式的 Demo 示例，适合作为 Spring AI 入门实战项目。

---

## 项目功能

- **多轮对话**：基于 `MessageChatMemoryAdvisor` 实现上下文记忆，支持连续追问
- **命理报告**：结构化输出，自动生成包含标题和建议列表的命理分析报告
- **RAG 知识库问答**：加载本地算命知识文档，结合向量检索进行精准问答
- **查询重写**：自动改写用户问题，提升检索质量
- **多查询扩展**：将一个问题扩展为多个相关查询，提高检索覆盖率
- **关键词增强**：启动时自动为文档补充关键词元数据，优化检索效果

---

## 技术栈

| 分类 | 技术 |
|------|------|
| 语言 / 框架 | Java 21 + Spring Boot 3.4.4 |
| AI 框架 | Spring AI 1.0.0-M6 + LangChain4j 1.0.0-beta2 |
| 大模型 | 阿里云百炼 DashScope（qwen-turbo） |
| 本地模型 | Ollama（deepseek-r1:1.5b） |
| 向量数据库 | SimpleVectorStore（内存）/ PgVector（PostgreSQL） |
| 序列化 | Kryo 5.6.2（对话记忆持久化） |
| 工具库 | Hutool、Lombok |
| API 文档 | Knife4j 4.4.0 |

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
```

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
./mvnw spring-boot:run
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
- 算命大师对话页：`http://localhost:5173/fortune-master`

前端会默认请求后端 `http://localhost:8123/api`，项目已补充跨域配置和 SSE 流式聊天接口。

### 4. 运行测试

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
