# Spring AI Alibaba Playground 项目核心调用图

## 1. 整体架构概览

本项目采用领域驱动设计（DDD）架构，主要分为两个边界上下文：
1. **多模态处理上下文** - 负责处理用户的自然语言输入
2. **图书管理上下文** - 负责图书管理的核心业务逻辑

```mermaid
graph TD
    A[用户请求] --> B[Web接口层]
    B --> C[应用层]
    C --> D[领域层]
    D --> E[基础设施层]
    E --> F[外部服务]
```

## 2. 核心组件调用关系

### 2.1 Web接口层调用关系

```mermaid
graph TD
    A[DddMultiModalController] --> B[MultiModalApplicationService]
    B --> C[ProcessTaskHandler]
    C --> D[ProcessingOrchestrator]
```

### 2.2 多模态处理核心调用链

```mermaid
graph TD
    A[ProcessingOrchestrator.processTask] --> B[IntentRecognizer.recognizeIntentWithParameters]
    B --> C[SpacyNlpEngine.recognizeIntentWithParameters]
    C --> D[IntentPromptTemplate.getIntentPrompt]
    C --> E[ParameterExtractorManager.extractParameters]
    E --> F[BookParameterExtractor.extractParameters]
    E --> G[GeneralParameterExtractor.extractParameters]
    A --> H[mergeParameters]
    A --> I[selectEngine]
    I --> J{是否为图书相关意图}
    J -->|是| K[BookMultiModalProcessor]
    J -->|否| L[其他处理器]
    K --> M[BookServiceAdapter]
```

### 2.3 图书管理调用链

```mermaid
graph TD
    A[BookMultiModalProcessor] --> B{意图类型}
    B --> C[handleBorrowRequest]
    B --> D[handleReturnRequest]
    B --> E[handleViewAvailableBooks]
    B --> F[handleSearchRequest]
    C --> G[BookServiceAdapter.borrowBook]
    D --> H[BookServiceAdapter.returnBook]
    E --> I[BookServiceAdapter.getAvailableBooks]
    F --> J[BookServiceAdapter.searchBooks]
    G --> K[BookRepository]
    H --> L[BorrowRecordRepository]
```

## 3. 详细调用图

### 3.1 请求处理流程

```mermaid
sequenceDiagram
    participant U as 用户
    participant C as DddMultiModalController
    participant A as MultiModalApplicationService
    participant H as ProcessTaskHandler
    participant O as ProcessingOrchestrator
    participant I as IntentRecognizer
    participant N as SpacyNlpEngine
    participant P as BookMultiModalProcessor
    participant S as BookServiceAdapter
    
    U->>C: 发送请求
    C->>A: 调用处理服务
    A->>H: 处理任务
    H->>O: 编排处理
    O->>I: 识别意图
    I->>N: 使用NLP引擎
    N-->>O: 返回意图和参数
    O->>P: 调用图书处理器
    P->>S: 调用图书服务
    S-->>P: 返回结果
    P-->>O: 返回处理结果
    O-->>H: 返回结果
    H-->>A: 返回结果
    A-->>C: 返回结果
    C-->>U: 返回响应
```

### 3.2 意图识别流程

```mermaid
sequenceDiagram
    participant O as ProcessingOrchestrator
    participant I as IntentRecognizer
    participant N as SpacyNlpEngine
    participant T as ParameterExtractorManager
    participant B as BookParameterExtractor
    participant G as GeneralParameterExtractor
    
    O->>I: recognizeIntentWithParameters
    I->>I: 检查任务参数中是否有意图
    alt 参数中有意图
        I-->>O: 直接返回意图和参数
    else 参数中无意图
        I->>N: 使用NLP引擎识别
        N->>N: 构造提示
        N->>N: 调用AI模型
        N-->>I: 返回AI识别结果
        I->>T: 提取补充参数
        T->>B: 提取图书参数
        T->>G: 提取通用参数
        T-->>I: 返回所有参数
        I-->>O: 返回意图和参数
    end
```

### 3.3 图书借阅处理流程

```mermaid
sequenceDiagram
    participant P as BookMultiModalProcessor
    participant S as BookServiceAdapter
    participant R as BookRepository
    participant BR as BorrowRecordRepository
    
    P->>P: handleBorrowRequest
    P->>P: 从任务参数提取信息
    P->>S: borrowBook
    S->>S: 检查图书是否存在
    S->>S: 检查图书是否可借
    S->>R: 更新图书状态
    S->>BR: 创建借阅记录
    S-->>P: 返回借阅记录
    P-->>ProcessingOrchestrator: 返回处理结果
```

## 4. 关键类和方法调用关系

### 4.1 ProcessingOrchestrator 核心调用

```mermaid
graph TD
    A[ProcessingOrchestrator.processTask] --> B[IntentRecognizer.recognizeIntentWithParameters]
    A --> C[mergeParameters]
    A --> D[selectEngine]
    A --> E{intentBasedEngine是否存在}
    E -->|是| F[intentBasedEngine.get]
    E -->|否| G[fallbackEngine查找]
    G --> H{fallbackEngine是否存在}
    H -->|是| I[fallbackEngine.get]
    H -->|否| J[抛出异常]
    F --> K[处理器.process]
    I --> K
```

### 4.2 BookMultiModalProcessor 核心调用

```mermaid
graph TD
    A[BookMultiModalProcessor.process] --> B[getParameter]
    A --> C[determineIntentFromPrompt]
    A --> D[processWithIntent]
    D --> E{意图类型}
    E --> F[handleBorrowRequest]
    E --> G[handleReturnRequest]
    E --> H[handleViewAvailableBooks]
    E --> I[handleSearchRequest]
    F --> J[extractBookIdFromPrompt]
    F --> K[extractStudentIdFromPrompt]
    F --> L[extractStudentNameFromPrompt]
    F --> M[bookServiceAdapter.borrowBook]
    G --> N[bookServiceAdapter.returnBook]
    H --> O[bookServiceAdapter.getAvailableBooks]
    I --> P[bookServiceAdapter.searchBooks]
```

### 4.3 SpacyNlpEngine 核心调用

```mermaid
graph TD
    A[SpacyNlpEngine.recognizeIntentWithParameters] --> B[chatClient.prompt]
    B --> C[替换用户输入到提示模板]
    B --> D[调用AI模型]
    D --> E[parseIntentWithParameters]
    E --> F{响应是否为JSON格式}
    F -->|是| G[解析JSON获取意图和参数]
    F -->|否| H[简单意图解析]
    G --> I[parameterExtractorManager.extractParameters]
    H --> I
    I --> J[返回意图识别结果]
```

## 5. 数据流向图

### 5.1 用户请求数据流向

```mermaid
graph LR
    A[用户输入] --> B[ProcessingTask]
    B --> C[ProcessingOrchestrator]
    C --> D[IntentRecognizer]
    D --> E[NLP引擎处理]
    E --> F[意图和参数识别]
    F --> G[参数合并到任务]
    G --> H[选择处理器]
    H --> I[BookMultiModalProcessor]
    I --> J[BookServiceAdapter]
    J --> K[领域模型操作]
    K --> L[返回结果]
```

### 5.2 图书借阅数据流向

```mermaid
graph LR
    A[借阅请求] --> B[BookMultiModalProcessor]
    B --> C[提取参数]
    C --> D[BookServiceAdapter]
    D --> E[验证图书]
    E --> F[更新图书状态]
    F --> G[创建借阅记录]
    G --> H[返回借阅结果]
```

## 6. 异常处理流程

```mermaid
graph TD
    A[处理过程] --> B{是否发生异常}
    B -->|是| C[记录错误日志]
    C --> D[设置任务为失败状态]
    D --> E[保存任务状态]
    E --> F[返回错误信息]
    B -->|否| G[正常处理]
    G --> H[设置任务为完成状态]
    H --> I[保存任务状态]
    I --> J[返回处理结果]
```

## 7. 系统组件依赖关系

```mermaid
graph LR
    A[Web层] --> B[应用层]
    B --> C[领域层]
    C --> D[基础设施层]
    D --> E[外部服务]
    
    subgraph Web层
        F[DddMultiModalController]
    end
    
    subgraph 应用层
        G[MultiModalApplicationService]
        H[ProcessTaskHandler]
    end
    
    subgraph 领域层
        I[ProcessingOrchestrator]
        J[IntentRecognizer]
        K[ProcessingEngine]
        L[BookDomainService]
    end
    
    subgraph 基础设施层
        M[BookMultiModalProcessor]
        N[SpacyNlpEngine]
        O[BookServiceAdapter]
        P[ParameterExtractorManager]
        Q[Repository实现]
    end
    
    subgraph 外部服务
        R[AI大语言模型]
        S[数据库]
    end
    
    F --> G
    G --> H
    H --> I
    I --> J
    I --> K
    J --> N
    K --> M
    M --> O
    O --> L
    L --> Q
    N --> P
    P --> Q
    N --> R
    Q --> S
```

## 8. 关键设计模式应用

### 8.1 策略模式

```mermaid
graph TD
    A[ProcessingEngine] --> B[BookMultiModalProcessor]
    A --> C[其他处理器]
    D[ProcessingOrchestrator] --> A
```

### 8.2 工厂模式

```mermaid
graph TD
    A[ParameterExtractorManager] --> B[BookParameterExtractor]
    A --> C[GeneralParameterExtractor]
    D[ProcessingOrchestrator] --> A
```

### 8.3 装饰器模式

```mermaid
graph TD
    A[ProcessingTask] --> B[添加参数]
    B --> C[任务处理]
    C --> D[返回结果]
```

## 9. 性能优化点

1. **意图缓存** - 优先从任务参数中获取意图，避免重复的NLP处理
2. **参数复用** - 意图识别阶段提取的参数直接传递给处理器使用
3. **回退机制** - AI识别失败时使用规则识别作为回退方案
4. **响应式处理** - 使用Reactor框架实现非阻塞异步处理

## 10. 可扩展性设计

1. **插件化处理器** - 通过ProcessingEngine接口支持添加新的处理器
2. **参数提取器链** - 通过ParameterExtractorManager管理参数提取器链
3. **意图支持策略** - 通过IntentSupportStrategy支持不同领域的意图处理
4. **NLP引擎抽象** - 通过NlpEngine接口支持不同的NLP实现