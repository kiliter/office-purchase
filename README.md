# 线上办公用品采购管理系统

论文《基于 SpringBoot 的线上办公用品采购管理系统设计与实现》的可运行代码。演示环境只使用内置 H2 数据库，不需要安装或启动 MySQL。

## 部署演示环境

镜像里已经包含前端页面和演示数据。拉取后直接运行：

```bash
docker pull ghcr.io/kiliter/office-purchase:latest
docker run --rm -p 8080:8080 ghcr.io/kiliter/office-purchase:latest
```

浏览器打开 http://127.0.0.1:8080 。

演示账号的密码都是 `123456`：

| 角色 | 账号 | 用来看什么 |
| --- | --- | --- |
| 管理员 | admin | 用户、商品、公告、全部申请和订单 |
| 审核人员 | audit01 | 审批申请，通过后自动生成订单 |
| 普通员工 | staff01 | 提交申请，查看自己的申请和订单 |

代码说明书是写给零基础同学的：系统跑起来后打开 http://127.0.0.1:8080/guide.html ，也可以直接打开仓库里的 `frontend/public/guide.html`。它先用一次申领把过程讲完，再按类名当词典查。同一页里有用例图、架构图、E-R 图、功能模块图、业务流程图、类图和时序图，图题在图下方，可以截图放进论文。

演示数据在内存里。容器停止或删除后，新增的申请和订单会消失，下次启动恢复成初始演示数据。这是演示环境的预期行为，不是故障。

审批通过不会自动扣库存，库存由管理员在商品管理里修改。这一点和论文里“系统不足”的说明一致。

### 没有现成镜像时，自己构建

在项目根目录执行：

```bash
docker build -t office-purchase:demo .
docker run --rm -p 8080:8080 office-purchase:demo
```

构建过程会先编译 Vue 页面，再打成 Spring Boot 包，最后用只含 Java 17 运行环境的镜像启动。启动参数固定为 H2，不会去连 MySQL。

### GitHub Actions 如何发镜像

工作流文件是 `.github/workflows/docker-image.yml`。推送到 `main` 分支，或在 GitHub 的 Actions 页面手动运行“发布 H2 演示镜像”，都会构建镜像并推到 GitHub Container Registry。

发布后的地址：

- `ghcr.io/kiliter/office-purchase:latest`
- `ghcr.io/kiliter/office-purchase:sha-<提交号>`

仓库是公开的。如果 `docker pull` 提示未授权，打开包设置页把可见性改成 Public：https://github.com/users/kiliter/packages/container/package/office-purchase

`docker-compose.yml` 只给本地 MySQL 开发用。演示部署不要执行它。

## 本地开发

本机默认 JDK 21 不能运行 Spring Boot 2.7。`scripts/start-backend.sh` 会优先选择 JDK 17。

只看效果，仍然不用 MySQL：

```bash
./scripts/start-backend.sh h2
./scripts/start-frontend.sh
```

前端开发地址是 http://127.0.0.1:8081 ，接口代理到 8080。

要用论文里的 MySQL，先自己准备数据库密码，再启动：

```bash
export DB_USERNAME=root
export DB_PASSWORD=你的本地数据库密码
export MYSQL_ROOT_PASSWORD=你的本地数据库密码
docker compose up -d
./scripts/start-backend.sh
./scripts/start-frontend.sh
```

建库脚本是 `sql/purchase_db.sql`。未设置 `PURCHASE_JWT_SECRET` 时，后端会临时生成登录密钥，重启后需要重新登录。

后端测试：

```bash
cd backend && mvn test
```

`PurchaseFlowTest` 使用 H2，覆盖提交、驳回、重提、生成订单和权限隔离。
