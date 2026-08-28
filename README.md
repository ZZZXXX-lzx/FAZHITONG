# 法智通法律服务平台

法智通是一站式法律服务平台，面向个人、律师与企业用户，提供 AI 法律咨询、合同智能审查、法律文书生成、法规检索、企业合规体检、律师服务、案例与知识库等功能。

## 技术栈

| 层级 | 技术 |
|------|------|
| 后端 | Spring Boot 3.2.0、Spring Cloud Gateway 2023.0.0、MyBatis-Plus 3.5.5、JWT (jjwt 0.12.3) |
| 数据库 | MySQL 8.x |
| 前端 | Vue 3.4、Vite 5、Element Plus 2.4、Pinia 2.1、Vue Router 4.2 |
| 文档生成 | docx（前端生成标准 Word 文档） |
| AI 能力 | OpenAI 兼容协议，支持 DeepSeek / 通义千问 / Moonshot Kimi / 智谱 GLM / OpenAI |
| 桌面/移动 | Electron、Capacitor、PWA |

## 目录结构

```
FAZHITONG
├── backend/                     # 后端（Maven 多模块）
│   ├── pom.xml                  # 父工程（com.fazhitong:fazhitong-backend）
│   ├── common/                  # 公共模块（AI 客户端、JWT、统一返回、异常处理）
│   ├── gateway/                 # API 网关（8080）
│   ├── auth-service/            # 认证服务（8081）
│   ├── user-service/            # 用户服务（8082）
│   ├── document-service/        # 文书服务（8083）
│   ├── consultation-service/    # 咨询服务（8084）
│   ├── contract-service/        # 合同服务（8085）
│   ├── case-service/            # 案例/法规服务（8086）
│   ├── payment-service/         # 支付服务（8087）
│   ├── run-all.bat              # 一键打包 + 启动全部后端
│   ├── start-all.bat            # 一键启动（先自动清理端口）
│   ├── stop-all.bat             # 一键停止全部后端
│   └── build.bat                # 一键打包
├── frontend/
│   ├── pc-portal/               # 用户端前端（3000，含 Electron/Capacitor/PWA）
│   └── admin/                   # 管理端前端（3001）
├── docs/                        # 数据库初始化与种子数据 SQL
│   ├── init-database.sql        # 建库建表 + 基础数据
│   ├── seed-data.sql            # 业务示例数据
│   ├── seed-templates.sql       # 文书模板（第一批）
│   ├── seed-templates-2.sql     # 文书模板（第二批）
│   └── seed-regulations.sql     # 法规数据
├── pom.xml                      # 最外层聚合工程（仅用于 IDEA 从项目根识别）
└── .env.example                 # 环境变量示例
```

## 环境要求

| 依赖 | 版本要求 | 说明 |
|------|----------|------|
| JDK | 17 或 21（推荐 21） | **不要用 JDK 22+**，Lombok 不兼容会报 `TypeTag :: UNKNOWN` |
| Maven | 3.8+ | 打包构建使用 |
| MySQL | 8.x | 默认账号 root，密码见下方说明 |
| Node.js | 18+ | 前端开发使用 |
| npm | 9+ | 前端依赖安装 |

## 数据库初始化

1. 启动 MySQL，确保 root 账号可用（默认密码 `123456`，如不同请同步修改 `backend/start-all.bat` 第 8 行的 `DB_PASSWORD`）。
2. 依次执行 `docs/` 下的 SQL：

```bash
mysql -u root -p < docs/init-database.sql
mysql -u root -p fazhitong < docs/seed-data.sql
mysql -u root -p fazhitong < docs/seed-templates.sql
mysql -u root -p fazhitong < docs/seed-templates-2.sql
mysql -u root -p fazhitong < docs/seed-regulations.sql
```

`init-database.sql` 会自动创建 `fazhitong` 库及全部表并写入基础数据，后续 seed 脚本补充业务示例数据、文书模板与法规库。

## 快速启动

### 方式一：脚本一键启动（推荐，无需打开 IDE）

进入 `backend/` 目录，双击即可：

- **首次运行 / 改动后端代码后**：双击 `run-all.bat`，会自动「打包 + 清理端口 + 启动 8 个服务」。
- **日常重启（已打包）**：先双击 `stop-all.bat` 停止，再双击 `start-all.bat` 启动。
- 单独重新打包：双击 `build.bat`。

脚本会自动完成以下处理，无需手动操作：

1. 释放 `8080 ~ 8087` 全部端口，避免「Port already in use」。
2. 使用系统 Maven 打包（已规避 `maven-wrapper.jar` 缺失导致的报错）。
3. 校验编译产物，缺失时提示先打包。

### 方式二：IntelliJ IDEA 启动（适合调试）

1. `File → Open` 选择 `backend/pom.xml`（或项目根 `pom.xml`），等待 Maven 加载完成。
2. 关键配置（务必都指向 JDK 21，参考「常见问题」第 1 条）：
   - `File → Project Structure → Project`：SDK 选 `21`。
   - `Settings → Build Tools → Maven → Runner`：JRE 选 `Use Project SDK`。
   - `Settings → Compiler → Java Compiler`：Target bytecode version 设为 `17`。
3. 右键各模块的 Application 主类运行，或建一个 Compound 组合配置一键启动全部。各服务主类如下：

| 服务 | 主类 |
|------|------|
| gateway | `com.fazhitong.gateway.GatewayApplication` |
| auth | `com.fazhitong.auth.AuthApplication` |
| user | `com.fazhitong.user.UserApplication` |
| document | `com.fazhitong.document.DocumentApplication` |
| consultation | `com.fazhitong.consultation.ConsultationApplication` |
| contract | `com.fazhitong.contract.ContractApplication` |
| case | `com.fazhitong.casemgt.CaseApplication` |
| payment | `com.fazhitong.payment.PaymentApplication` |

Compound 配置步骤：`Run → Edit Configurations → + → Compound`，把上述 8 个 Spring Boot 配置加入即可。

### 方式三：启动前端

后端启动后，再启动前端：

```bash
cd frontend/pc-portal
npm install   # 首次运行
npm run dev   # 用户端，http://localhost:3000
```

```bash
cd frontend/admin
npm install   # 首次运行
npm run dev   # 管理端，http://localhost:3001
```

## 端口说明

| 服务 | 端口 | 说明 |
|------|------|------|
| gateway | 8080 | 统一入口，路由前缀 `/api/**` |
| auth-service | 8081 | 登录注册、JWT 签发 |
| user-service | 8082 | 用户、企业、收藏、反馈、通知、评价 |
| document-service | 8083 | 文书模板、文书生成、文件上传 |
| consultation-service | 8084 | AI 咨询、律师服务、法律援助 |
| contract-service | 8085 | 合同模板、合同智能审查 |
| case-service | 8086 | 案例检索、法规检索、知识库 |
| payment-service | 8087 | 会员、订单、支付 |
| pc-portal | 3000 | 用户端前端 |
| admin | 3001 | 管理端前端 |
| MySQL | 3306 | 数据库 |

## AI 模型配置

AI 功能（法律咨询、合同审查、文书起草）使用 OpenAI 兼容协议。未配置密钥时自动降级为规则引擎，不影响系统运行。

复制 `.env.example` 为 `.env`，填入以下变量：

```env
AI_API_KEY=sk-xxxxxxxx
AI_BASE_URL=https://api.deepseek.com/v1
AI_MODEL=deepseek-chat
```

支持的厂商：DeepSeek、通义千问（兼容模式）、Moonshot Kimi、智谱 GLM、OpenAI 等。

## 常见问题

1. **编译报 `com.sun.tools.javac.code.TypeTag :: UNKNOWN`**：Lombok 与 JDK 版本不兼容，把项目 SDK 从 JDK 26 切换到 JDK 21 即可（`Project Structure → Project → SDK`）。

2. **启动报 `Port 80xx was already in use`**：端口被残留进程占用，双击 `backend/stop-all.bat` 释放端口后重新启动；IDEA 里则先在 Run 面板停掉同名旧实例。

3. **运行 `mvnw.cmd` 报找不到 `maven-wrapper.jar`**：本项目 wrapper 不完整，请改用系统 Maven（直接运行 `mvn` 命令），`build.bat` 已做此处理。

4. **IDEA 里看不到根 pom / 模块**：IDEA 若以项目根 `FAZHITONG` 打开，请 `File → Open` 直接选择 `backend/pom.xml`，或选择根目录的聚合 `pom.xml`。

5. **服务启动后连不上数据库**：检查 MySQL 是否启动、`fazhitong` 库是否已初始化、密码是否与 `backend/start-all.bat` 中 `DB_PASSWORD` 一致。

6. **数据库密码不是 `123456`**：修改 `backend/start-all.bat` 第 8 行的 `DB_PASSWORD`，或在 IDEA 运行配置的环境变量中设置 `DB_PASSWORD`。
