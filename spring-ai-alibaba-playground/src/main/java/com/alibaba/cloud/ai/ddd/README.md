# DDD (Domain-Driven Design) 架构重构

## 架构概览

基于领域驱动设计(DDD)的分层架构，将多模态处理业务划分为清晰的边界上下文和分层结构。

## 目录结构

```
src/main/java/com/alibaba/cloud/ai/ddd/
├── shared/                           # 共享内核
│   ├── domain/
│   │   ├── Entity.java              # 实体基类
│   │   ├── ValueObject.java         # 值对象基类
│   │   ├── DomainEvent.java         # 领域事件基类
│   │   └── Repository.java          # 仓储接口基类
│   └── exception/
│       └── DomainException.java     # 领域异常基类
├── multimodal/                      # 多模态处理边界上下文
│   ├── domain/                      # 领域层
│   │   ├── model/                   # 领域模型
│   │   ├── service/                 # 领域服务
│   │   └── repository/              # 仓储接口
│   ├── application/                 # 应用层
│   │   ├── service/                 # 应用服务
│   │   ├── command/                 # 命令对象
│   │   └── handler/                 # 命令处理器
│   ├── infrastructure/              # 基础设施层
│   │   ├── repository/              # 仓储实现
│   │   └── external/                # 外部服务适配器
│   └── interfaces/                  # 接口层
│       ├── web/                     # Web控制器
│       └── dto/                     # 数据传输对象
├── book/                            # 图书管理边界上下文
│   ├── domain/                      # 领域层
│   │   ├── model/                   # 领域模型 (Book, BorrowRecord等)
│   │   ├── service/                 # 领域服务
│   │   └── repository/              # 仓储接口
│   ├── application/                 # 应用层
│   │   └── service/                 # 应用服务
│   ├── infrastructure/              # 基础设施层
│   │   ├── repository/              # 仓储实现
│   │   └── external/                # 外部服务适配器
│   └── interfaces/                  # 接口层
│       └── web/                     # Web控制器
└── processing/                      # 处理引擎边界上下文
```

## 设计原则

1. **分层隔离**: 严格按照DDD分层，依赖关系单向向下
2. **边界清晰**: 不同边界上下文通过接口通信
3. **领域优先**: 业务逻辑集中在领域层
4. **依赖倒置**: 高层模块不依赖低层模块的具体实现

## 图书管理模块

图书管理模块实现了完整的图书借阅系统，包括以下功能：

### 领域模型
- **Book**: 图书实体，包含书名、作者、ISBN、描述、类别、标签等属性
- **BookId**: 图书ID值对象
- **BorrowRecord**: 借阅记录实体，记录借阅详情
- **BorrowRecordId**: 借阅记录ID值对象

### 核心功能
1. **图书搜索**: 支持按标题、作者、类别等多种方式搜索图书
2. **图书借阅**: 学生可以借阅可借阅的图书
3. **图书归还**: 学生可以归还已借阅的图书
4. **可借阅图书查看**: 查看当前所有可借阅的图书

### API接口
- `GET /api/v1/books/search`: 搜索图书
- `GET /api/v1/books/category/{category}`: 根据类别搜索图书
- `GET /api/v1/books/available`: 查看可借阅图书
- `POST /api/v1/books/{bookId}/borrow`: 借阅图书
- `POST /api/v1/books/{bookId}/return`: 归还图书
- `GET /api/v1/books/{bookId}`: 获取图书详情

### 多模态支持
图书管理模块还集成了多模态处理能力，可以通过自然语言进行交互：
- 智能意图识别：自动识别用户是要查看可借书籍、搜索特定书籍还是借阅书籍
- 自然语言处理：支持通过自然语言进行图书搜索和借阅操作
- 多模态处理器：专门的图书管理多模态处理器(BookMultiModalProcessor)