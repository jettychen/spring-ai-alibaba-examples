# 硅基流动 (SiliconFlow) 集成说明

## 📋 概述

本项目已初步集成硅基流动平台，支持多种先进的大语言模型。硅基流动提供了 OpenAI 兼容的 API 接口，可以方便地集成各种开源和商业模型。

## 🚀 快速开始

### 1. 获取 API 密钥

1. 访问 [硅基流动官网](https://siliconflow.cn)
2. 注册账号并登录
3. 在控制台中获取您的 API 密钥

### 2. 配置环境变量

```bash
# 设置硅基流动 API 密钥（必需）
export SPRING_AI_SILICONFLOW_API_KEY=sk-your-api-key-here

# 可选：自定义 API 基础 URL（默认使用官方地址）
export SPRING_AI_SILICONFLOW_BASE_URL=https://api.siliconflow.cn/v1

# 可选：设置默认模型
export SPRING_AI_SILICONFLOW_MODEL=deepseek-chat

# 可选：设置默认温度参数
export SPRING_AI_SILICONFLOW_TEMPERATURE=0.7

# 可选：设置最大 Token 数
export SPRING_AI_SILICONFLOW_MAX_TOKENS=2000
```

### 3. 启动应用

```bash
# 重新启动应用程序以加载新的环境变量
./mvnw spring-boot:run
```

## 🤖 支持的模型

### DeepSeek 系列
- **deepseek-chat**: 通用聊天模型，擅长对话、推理和代码生成
- **deepseek-coder**: 专门的代码生成模型，优化了代码理解和生成能力

### 通义千问系列
- **Qwen/Qwen2.5-72B-Instruct**: 72B 大参数模型，强大的理解和生成能力
- **Qwen/Qwen2.5-32B-Instruct**: 32B 模型，平衡性能和效率

### 其他模型
- **THUDM/glm-4-9b-chat**: 智谱 GLM-4 模型，中文理解能力优秀
- **meta-llama/Meta-Llama-3.1-8B-Instruct**: Meta 开源模型
- **microsoft/DialoGPT-medium**: 微软对话模型

## 💬 使用方式

### 1. Web 界面使用

1. 打开浏览器访问 `http://localhost:8080`
2. 在聊天界面中，选择硅基流动支持的模型
3. 开始对话

### 2. API 调用

```bash
# 使用硅基流动模型进行聊天
curl -X POST http://localhost:8080/api/v1/chat/completions \
  -H "Content-Type: application/json" \
  -d '{
    "chatId": "test-chat",
    "model": "deepseek-chat",
    "prompt": "你好，请介绍一下自己"
  }'
```

## 🔧 当前状态

### ✅ 已实现功能
- 硅基流动模型检测和识别
- API 密钥配置验证
- 用户友好的配置提示信息
- 模型列表配置

### 🚧 开发中功能
- 完整的硅基流动 API 调用实现
- 流式响应支持
- 深度思考模式支持
- 错误处理和重试机制

### 📋 计划功能
- 模型参数动态配置
- 成本和使用量统计
- 多模型并行调用
- 智能模型选择

## 🛠️ 技术架构

### 集成方式
- 基于 Spring AI 框架
- 使用 OpenAI 兼容 API
- 响应式编程支持
- 统一的聊天客户端接口

### 配置管理
- 环境变量配置
- 条件化 Bean 创建
- 运行时模型切换

## 📚 模型选择指南

### 任务类型建议

| 任务类型 | 推荐模型 | 说明 |
|---------|---------|------|
| 通用对话 | deepseek-chat | 平衡性能，适合大多数对话场景 |
| 代码生成 | deepseek-coder | 专门优化的代码理解和生成 |
| 复杂推理 | Qwen/Qwen2.5-72B-Instruct | 大参数模型，推理能力强 |
| 中文理解 | THUDM/glm-4-9b-chat | 针对中文优化 |
| 开源替代 | meta-llama/Meta-Llama-3.1-8B-Instruct | 开源高质量模型 |

## ⚠️ 注意事项

1. **API 密钥安全**: 请妥善保管您的 API 密钥，不要在代码中硬编码
2. **成本控制**: 硅基流动按使用量计费，请合理设置 Token 限制
3. **网络要求**: 确保应用能够访问硅基流动的 API 服务
4. **模型可用性**: 不同模型可能有不同的可用性和限制

## 🔍 故障排除

### 常见问题

1. **API 密钥错误**
   ```
   错误: 请求使用硅基流动模型但未配置API密钥
   解决: 设置正确的 SPRING_AI_SILICONFLOW_API_KEY 环境变量
   ```

2. **网络连接问题**
   ```
   错误: 连接超时或网络不可达
   解决: 检查网络连接和防火墙设置
   ```

3. **模型不支持**
   ```
   错误: 模型名称不在支持列表中
   解决: 使用支持的模型名称，参考上面的模型列表
   ```

## 📞 技术支持

- **项目文档**: 查看项目 README 和相关文档
- **硅基流动官方文档**: [https://docs.siliconflow.cn](https://docs.siliconflow.cn)
- **GitHub Issues**: 在项目仓库中提交问题

## 🔄 更新日志

- **v1.0.0** (2025-01-XX): 初步集成硅基流动支持
  - 添加模型检测和配置
  - 实现用户友好的配置提示
  - 添加模型配置文件

---

**注意**: 硅基流动集成功能当前处于开发阶段，完整功能将在后续版本中发布。 