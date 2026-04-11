<div align="center">

# 📊 Excel Tools Backend

**文本转 Excel 与现场数据采集助手 — 后端服务**

将微信中的零散文字快速转化为结构化数据，并生成可统计、可导出的 Excel 表格

[![Java](https://img.shields.io/badge/Java-11-orange?logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.6.13-6DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![MySQL](https://img.shields.io/badge/MySQL-5.7-4479A1?logo=mysql&logoColor=white)](https://www.mysql.com/)
[![MyBatis-Plus](https://img.shields.io/badge/MyBatis%20Plus-3.5.3-red?logo=mybatis)](https://baomidou.com/)
[![EasyExcel](https://img.shields.io/badge/EasyExcel-3.3.2-blue?logo=apache)](https://easyexcel.opensource.alibaba.com/)
[![MinIO](https://img.shields.io/badge/MinIO-Latest-C72C48?logo=minio&logoColor=white)](https://min.io/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

</div>

---

## ✨ 功能特性

- 📝 **文本智能解析** — 自动识别分隔符（空格/Tab/逗号/换行），一键转为结构化表格
- 📂 **Excel 多Sheet导入** — 支持 .xlsx/.xls 文件上传，自动解析所有Sheet
- 📊 **多Sheet分组导出** — 按指定列值分组，每个分组生成独立Sheet
- 📈 **数据统计** — 自动统计总行数，支持按列值分类统计
- 🔐 **用户隔离** — 微信小程序登录，数据按用户完全隔离
- ☁️ **文件存储** — MinIO 对象存储，文件自动备份

## 🎯 适用场景

| 场景 | 说明 |
|:---:|------|
| 🏛️ 基层政务 | 残联、民政入户登记、办证统计 |
| 🏘️ 社区服务 | 人员信息采集、走访核查 |
| 🏥 医疗随访 | 公共卫生随访记录 |
| 🏫 教育管理 | 学生信息统计、签到 |
| 🏢 企业行政 | 考勤、培训管理 |

## 🏗️ 技术架构

<table>
  <tr>
    <td align="center"><strong>层次</strong></td>
    <td align="center"><strong>技术</strong></td>
    <td align="center"><strong>版本</strong></td>
  </tr>
  <tr>
    <td align="center">🚀 基础框架</td>
    <td>Spring Boot</td>
    <td>2.6.13</td>
  </tr>
  <tr>
    <td align="center">💾 ORM</td>
    <td>MyBatis-Plus</td>
    <td>3.5.3</td>
  </tr>
  <tr>
    <td align="center">📂 Excel</td>
    <td>EasyExcel</td>
    <td>3.3.2</td>
  </tr>
  <tr>
    <td align="center">🗄️ 数据库</td>
    <td>MySQL</td>
    <td>5.7</td>
  </tr>
  <tr>
    <td align="center">☁️ 对象存储</td>
    <td>MinIO</td>
    <td>Latest</td>
  </tr>
  <tr>
    <td align="center">☕ 运行环境</td>
    <td>JDK</td>
    <td>11</td>
  </tr>
</table>

## 📁 项目结构

```
src/main/java/com/slow/excel_tools_backend/
├── 🚀 ExcelToolsBackendApplication.java
├── 📦 common/
│   ├── Result.java                     # 统一响应封装
│   ├── BusinessException.java          # 自定义业务异常
│   └── GlobalExceptionHandler.java     # 全局异常处理
├── ⚙️ config/
│   ├── MybatisPlusConfig.java          # MyBatis-Plus 分页配置
│   └── MinioConfig.java                # MinIO 连接配置
├── 🎮 controller/
│   ├── AuthController.java             # 认证 /api/auth
│   ├── ParseController.java            # 解析 /api/parse
│   ├── TaskController.java             # 任务 /api/task
│   └── HealthController.java           # 健康检查 /health
├── 📋 entity/
│   ├── User.java                       # 用户实体
│   ├── Task.java                       # 任务实体（含JSON字段）
│   ├── ColumnDefine.java               # 列定义
│   ├── SheetData.java                  # Sheet数据
│   └── ExcelParseResult.java           # Excel解析结果
├── 🗃️ mapper/
│   ├── UserMapper.java
│   └── TaskMapper.java
└── 🔧 service/
    ├── UserService.java / impl/        # 用户登录注册
    ├── ParseService.java / impl/       # 文本智能解析
    ├── ExcelParseService.java          # Excel多Sheet解析
    ├── TaskService.java / impl/        # 任务CRUD + 统计 + 导出
    └── MinioService.java               # MinIO文件操作
```

## 🔌 API 接口

<details>
<summary><strong>🔐 认证模块</strong></summary>

| 方法 | 接口 | 说明 |
|:---:|------|------|
| `POST` | `/api/auth/login` | 微信小程序登录 |

</details>

<details>
<summary><strong>📝 解析模块</strong></summary>

| 方法 | 接口 | 说明 |
|:---:|------|------|
| `POST` | `/api/parse/text` | 文本智能解析（自动分列） |
| `POST` | `/api/parse/excel` | Excel文件导入（支持多Sheet） |

</details>

<details>
<summary><strong>📋 任务模块</strong></summary>

| 方法 | 接口 | 说明 |
|:---:|------|------|
| `GET` | `/api/task` | 我的任务列表 |
| `POST` | `/api/task` | 创建任务 |
| `GET` | `/api/task/{id}` | 任务详情 |
| `PUT` | `/api/task/{id}` | 更新任务 |
| `DELETE` | `/api/task/{id}` | 删除任务 |
| `GET` | `/api/task/{id}/stats` | 数据统计 |
| `GET` | `/api/task/{id}/export` | 导出 Excel（单Sheet） |
| `GET` | `/api/task/{id}/export/group?groupByField=xxx` | 按列分组导出（多Sheet） |

</details>

## 🚀 快速开始

### 环境要求

- ☕ JDK 11
- 📦 Maven 3.6+
- 🗄️ MySQL 5.7
- ☁️ MinIO（可选，用于文件存储）

### 1️⃣ 创建数据库

```sql
CREATE DATABASE excel_tools CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
```

### 2️⃣ 修改配置

编辑 `src/main/resources/application.yaml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/excel_tools?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: your_password

# MinIO 配置
minio:
  endpoint: http://localhost:9000
  access-key: admin
  secret-key: your_secret
  bucket: excel-tools
```

### 3️⃣ 启动 MinIO（Docker）

```bash
docker run -d --name minio \
  -p 9000:9000 -p 9001:9001 \
  -e MINIO_ROOT_USER=admin \
  -e MINIO_ROOT_PASSWORD=admin123456 \
  -v minio_data:/data \
  minio/minio server /data --console-address ":9001"
```

### 4️⃣ 编译运行

```bash
mvn compile
mvn spring-boot:run
```

启动后访问 http://localhost:8080/health 验证服务状态。

## ⚠️ 错误码说明

| 错误码段 | 模块 | 示例 |
|:---:|:---:|------|
| `1xxx` | 📝 文本解析 | 1001 文本为空 / 1002 无有效数据 |
| `2xxx` | 📋 任务管理 | 2001 任务不存在 / 2002 无权访问 |
| `3xxx` | 🔐 登录认证 | 3001 code为空 |
| `4xxx` | 📂 Excel导入 | 4001 文件为空 / 4002 格式不对 / 4004 解析失败 |

## 📄 License

[MIT](LICENSE) © 2026 Excel Tools
