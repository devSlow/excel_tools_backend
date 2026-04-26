# 项目部署踩坑记录

## 问题1：系统异常 - UnknownHostException: mysql57

### 原因
后端容器无法解析 MySQL 容器主机名 `mysql57`

### 错误日志
```
Caused by: java.net.UnknownHostException: mysql57: Name or service not known
```

### 解决方案
使用 `--link` 将后端容器连接到 MySQL 容器

```bash
docker run -d -p 8080:8080 --name excel-backend --link mysql57 excel-tools-backend
```

---

## 问题2：API路径错误 - 参数类型错误

### 原因
使用了错误的API路径 `/api/task/list`

### 错误日志
```
{"code":1,"msg":"参数类型错误: id","data":null}
```

### 正确的API路径
```
GET /api/task?page=1&size=10
```

---

## 正确的启动命令

```bash
# 1. 构建项目
mvn clean package -DskipTests

# 2. 构建Docker镜像
docker build -t excel-tools-backend .

# 3. 停止并启动新容器(带--link)
docker stop excel-backend && docker rm excel-backend
docker run -d -p 8080:8080 --name excel-backend --link mysql57 excel-tools-backend

# 4. 查看日志
docker logs -f excel-backend
```

---

## 相关文件位置

- Dockerfile: `/root/excel_tools_backend/Dockerfile`
- pom.xml: `/root/excel_tools_backend/pom.xml`
- API Controller: `src/main/java/com/slow/excel_tools_backend/controller/TaskController.java`