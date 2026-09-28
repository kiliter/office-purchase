# 线上办公用品采购管理系统

按论文《基于 SpringBoot 的线上办公用品采购管理系统设计与实现》落地的可运行项目。后端包名 `com.office.purchase`，前端为 Vue2 + Element-UI。

## 方案

系统采用 B/S 架构，分成表现层、业务层和数据访问层。

- 表现层：Vue2 + Element-UI，开发端口 8081，通过 `/api` 代理访问后端。
- 业务层：Spring Boot 2.7.12，按角色拦截接口。
- 数据访问层：MyBatis-Plus 3.5.3.1。
- 数据库：MySQL 8.0，库名 `purchase_db`。本机没有 MySQL 时可用 H2 内存库演示，业务规则相同。

角色与论文一致：

| 角色 | 账号 | 能做的事 |
| --- | --- | --- |
| 管理员 admin | admin / 123456 | 用户、商品、公告、全部申请、全部订单，并更新订单状态 |
| 审核人员 audit | audit01 / 123456 | 审批申请，查看全部申请和订单，浏览商品与公告 |
| 普通员工 staff | staff01 / 123456 | 浏览商品、提交申请、查看自己的申请和订单 |

主流程：员工选品并填写理由，申请状态为“待审批”。审核驳回时必须填写意见，员工修改后重新提交。审核通过后，同一事务生成状态为“待采购”的订单。管理员可将订单改为“采购中 / 已到货 / 已完成”。

按论文“系统不足”一节，审批通过**不会自动扣减库存**。库存由管理员在商品管理里维护。密码使用 BCrypt 保存，不存明文。未登录不能访问业务接口，角色不匹配返回“没有权限执行此操作”。

## 目录

- `backend`：Spring Boot 后端
- `frontend`：Vue2 前端
- `sql/purchase_db.sql`：论文附录用的 MySQL 建库脚本，含演示数据
- `docker-compose.yml`：本地 MySQL 8.0
- `scripts`：启动脚本

## 启动

本机默认 JDK 21 不能用来跑 Spring Boot 2.7。脚本会优先选择 JDK 17，其次 JDK 8。

### 方式一：没有 MySQL，先看效果

```bash
./scripts/start-backend.sh h2
./scripts/start-frontend.sh
```

浏览器打开 http://127.0.0.1:8081 。H2 数据在内存中，重启后端后恢复成初始演示数据。

数据库密码和登录令牌密钥不写进仓库。使用 MySQL 前先在项目根目录准备环境变量，`.env.example` 里是空模板：

```bash
export DB_USERNAME=root
export DB_PASSWORD=你的本地数据库密码
export MYSQL_ROOT_PASSWORD=你的本地数据库密码
export PURCHASE_JWT_SECRET=一段足够长的随机字符串
```

未设置 `PURCHASE_JWT_SECRET` 时，后端会临时生成密钥，重启后需要重新登录。

### 方式二：按论文使用 MySQL

```bash
docker compose up -d
./scripts/start-backend.sh
./scripts/start-frontend.sh
```

`docker compose` 首次启动会执行 `sql/purchase_db.sql`。如果本机已有 MySQL，请自行导入该脚本，并让 `DB_USERNAME`、`DB_PASSWORD` 与数据库账号一致。

后端单独打包：

```bash
cd frontend && npm install && npm run build
cd ../backend && mvn -DskipTests package
```

构建后的页面会进入后端 `static` 目录，之后只启动后端，用 http://127.0.0.1:8080 访问。

## 测试

```bash
cd backend && mvn test
```

`PurchaseFlowTest` 使用 H2，覆盖登录、员工越权、提交、驳回、重提、通过生成订单、更新订单状态。
