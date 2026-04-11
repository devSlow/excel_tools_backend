# Excel Tools Backend

文本转 Excel 与现场数据采集助手 - 后端服务

## 项目简介

将微信中的零散文字快速转化为结构化数据，并生成可统计、可导出的 Excel 表格。

面向基层政务、社区服务、医疗随访、企业行政等场景，解决手动录入 Excel 耗时费力的问题。

## 技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Java | 11 | JDK 11 |
| Spring Boot | 2.6.13 | 基础框架 |
| MyBatis-Plus | 3.5.3 | ORM 框架 |
| MySQL | 5.7 | 数据库 |
| EasyExcel | 3.3.2 | Excel 处理 |
| Lombok | 1.18.36 | 简化代码 |

## 项目结构

```
src/main/java/com/slow/excel_tools_backend/
├── ExcelToolsBackendApplication.java   # 启动类
├── common/
│   ├── Result.java                     # 统一响应封装
│   └── GlobalExceptionHandler.java     # 全局异常处理
├── config/
│   └── MybatisPlusConfig.java          # MyBatis-Plus 配置
├── controller/
│   ├── AuthController.java             # 登录接口
│   ├── ParseController.java            # 文本解析接口
│   └── TaskController.java             # 任务管理接口
├── entity/
│   ├── User.java                       # 用户实体
│   ├── Task.java                       # 任务实体
│   └── ColumnDefine.java               # 列定义
├── mapper/
│   ├── UserMapper.java
│   └── TaskMapper.java
└── service/
    ├── UserService.java                # 用户服务
    ├── ParseService.java               # 文本解析服务
    └── TaskService.java                # 任务服务
```

## 接口列表

| 方法 | 接口 | 说明 |
|------|------|------|
| POST | `/api/auth/login` | 微信登录 |
| POST | `/api/parse/text` | 文本智能解析 |
| GET | `/api/task` | 任务列表 |
| POST | `/api/task` | 创建任务 |
| GET | `/api/task/{id}` | 任务详情 |
| PUT | `/api/task/{id}` | 更新任务 |
| DELETE | `/api/task/{id}` | 删除任务 |
| GET | `/api/task/{id}/stats` | 数据统计 |
| GET | `/api/task/{id}/export` | 导出 Excel |

## 快速开始

### 环境要求

- JDK 11
- Maven 3.6+
- MySQL 5.7

### 数据库配置

```sql
CREATE DATABASE excel_tools CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
```

### 修改配置

编辑 `src/main/resources/application.yaml`，修改数据库连接信息：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/excel_tools?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: your_password
```

### 编译运行

```bash
mvn compile
mvn spring-boot:run
```

启动后访问 http://localhost:8080/health 验证服务状态。

## License

MIT
