# 运输管理后端交接包

本包是可独立交接的后端工程，技术栈为 Java 17+、Spring Boot、MyBatis、MySQL/MariaDB。

## 目录

- `backend/`：源码、Maven Wrapper、配置、数据库映射。
- `backend/traffic_management-0.0.1-SNAPSHOT.jar`：已构建的可执行后端。
- `scripts/start-traffic-backend.ps1`：Windows 启动脚本。
- `trans_groupwork_fixed.sql`：数据库结构参考。
- `trans_groupwork_integration_test.sql`：隔离联调数据库脚本，只用于测试库。

## 安装

1. 安装 Java 17 或更高版本。
2. 创建数据库 `transport_groupwork`。
3. 首次部署导入 `trans_groupwork_fixed.sql`。
4. 已有数据时先备份，再执行 `backend/src/main/resources/adapter-migration.sql`。该迁移会补齐工厂/需求/车辆/POI 软删除字段、车辆版本与路线字段、任务派单字段和动作定义表。不要在已有数据库上重复导入包含 `DROP TABLE` 的完整 SQL。

联调时推荐导入 `trans_groupwork_integration_test.sql`，并将 `DB_URL` 指向 `transport_groupwork_api_test`。该脚本只重建隔离测试库中的表。

## 启动

在本包根目录打开 PowerShell，设置数据库配置：

```powershell
$env:DB_URL='jdbc:mysql://127.0.0.1:3306/transport_groupwork?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=UTF-8&allowPublicKeyRetrieval=true'
$env:DB_USERNAME='root'
$env:DB_PASSWORD=''
```

直接运行已构建版本：

```powershell
java -jar .\backend\traffic_management-0.0.1-SNAPSHOT.jar --server.port=8080
```

或重新构建后运行：

```powershell
.\scripts\start-traffic-backend.ps1 -Port 8080 -Build
```

服务地址为 `http://127.0.0.1:8080`。这是 JSON API 服务，根路径没有网页首页。

## 功能

- 工厂、需求、车辆和动作定义的查询与维护。
- 基于工厂生成运输需求。
- 需求、车辆和 POI 的软删除。
- 运输任务生成与车辆指派，防止重复占用。
- 车辆位置更新和版本校验。
- 车辆状态流转和版本校验。
- 运输收益分项计算。
- 需求与车辆匹配度计算及原因说明。
- POI 新增、查询、软删除；路线引用中的 POI 返回冲突错误。
- 后端仿真循环启动、暂停、单步和状态查询；推进派单车辆并保存位置与版本。

## 主要接口

- `/factories`
- `/needs`
- `/vehicles`
- `/tasks`
- `/tasks/{id}/revenue`
- `/matching/score?needId=...&vehicleId=...`
- `/pois`
- `/actions`
- `/simulation/status`、`/simulation/start`、`/simulation/pause`、`/simulation/step`

## 联调验证

前端开发服务器将 API 请求代理到 `http://127.0.0.1:8080`。工作区中的 `前后端联合调用与逐步测试.md` 包含测试库初始化、前后端启动、页面操作顺序和故障排查；前端目录 `scripts/联合调用冒烟测试.ps1` 可自动经 Vite 代理执行端到端检查。测试脚本应连接专门的测试库。

删除操作默认保留历史记录。接口字段和示例以源码 Controller、实体类及 Mapper XML 为准。
