# 小伴 XiaoBan

<div align="center">

**AI 智能养老陪伴 App — 小伴常在，家人安心**

[![Android](https://img.shields.io/badge/Android-26%2B-green.svg)](https://developer.android.com)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![Config](https://img.shields.io/badge/Config-config.yaml-blue.svg)](config.yaml.example)

</div>

---

## 项目概述

**小伴** 是面向老年人的 AI 语音陪伴 Android 应用，采用「老人端 + 子女端」双端架构。

- **老人端**：语音与 AI 助手「小伴」对话，获取日常咨询、健康提醒与情感陪伴
- **子女端**：远程查看老人状态、智能日报、核实 AI 回复、设置远程提醒

## 项目结构

```
AIGC/
├── config.yaml              # 【核心】外部 API 与基础设施配置（本地，不提交 Git）
├── config.yaml.example      # 配置模板（提交 Git，新同事复制使用）
├── requirements.txt         # 运行环境清单与自检说明
├── README.md                # 本文件 — 版本日志与开发指南
├── database/                # MySQL 初始化与迁移脚本
├── xiaoban-server/          # Spring Boot 后端
│   ├── run.sh / stop.sh     # 启动 / 停止脚本
│   └── src/main/resources/application.yml  # 框架配置（导入 config.yaml）
└── XiaoBan/                 # Android 客户端
```

---

## 新同事快速上手（5 步）

### 1. 克隆代码并准备配置

```bash
git clone <repo-url>
cd AIGC
cp config.yaml.example config.yaml    # Windows: copy config.yaml.example config.yaml
```

编辑 `config.yaml`，填写数据库密码与各外部 API Key（节点均有中文说明）。

完整环境清单见 **[requirements.txt](requirements.txt)**。

### 2. 初始化数据库

Navicat 或命令行执行：

```bash
mysql -u root -p < database/init.sql
```

> 旧库升级还需执行 `add_user_profile_fields.sql`、`add_emergency_contact_field.sql`。

### 3. 启动后端

**IDEA**：运行 `com.xiaoban.server.XiaobanServerApplication`（工作目录建议 `xiaoban-server` 或项目根目录）

**脚本**（Git Bash / WSL）：

```bash
cd xiaoban-server
./run.sh
```

验证：http://localhost:8080/swagger-ui.html

启动日志会打印 `config.yaml` 加载摘要及各外部服务状态。

### 4. 配置 Android 端

| 配置项 | 文件 | 说明 |
|--------|------|------|
| 后端地址 | `XiaoBan/app/build.gradle` | 模拟器 `http://10.0.2.2:8080/`；真机改 WLAN IP |
| 讯飞 / 极光 / 高德 | `Constants.java` + `AndroidManifest.xml` | 对照 `config.yaml` → `client-sdk-reference` |

### 5. 运行 App

Android Studio 打开 `XiaoBan` → Sync Gradle → Run。

---

## 配置管理（config.yaml）

> **设计原则**：外部 API 集中管理（SRP），业务代码通过 `@ConfigurationProperties` 注入（DIP），新增服务只需扩展配置类（OCP）。

### 后端外部服务（修改 config.yaml 后重启后端）

| config.yaml 节点 | 服务 | 用途 |
|------------------|------|------|
| `external-services.ai-dashscope` | 阿里云通义千问 | AI 对话 |
| `external-services.jpush` | 极光推送 | 健康告警、绑定、消息推送 |
| `external-services.amap-weather` | 高德天气 API | 老人端首页天气 |
| `external-services.jwt-auth` | JWT | 登录 Token |
| `infrastructure.database` | MySQL | 数据库连接 |
| `infrastructure.server` | 内置 | 端口 8080 |

### 客户端 SDK（对照 config.yaml → client-sdk-reference）

| 节点 | 服务 | 配置文件 |
|------|------|----------|
| `iflytek-voice` | 科大讯飞语音 | `Constants.java` |
| `jpush-android` | 极光 Android SDK | `Constants.java` / `build.gradle` |
| `amap-location` | 高德定位 SDK | `Constants.java` / `AndroidManifest.xml` |
| `android-backend` | 后端 API 地址 | `build.gradle` |

### 环境变量覆盖（优先级高于 config.yaml 默认值）

```bash
# Windows PowerShell
$env:DB_PASSWORD="你的MySQL密码"
$env:AI_API_KEY="你的DashScope密钥"

# Linux / macOS
export DB_PASSWORD=你的MySQL密码
export AI_API_KEY=你的DashScope密钥
```

详见 [requirements.txt](requirements.txt) `[ENV_VARS]` 节。

### 自定义 config.yaml 路径

```bash
export XIAOBAN_CONFIG_PATH=/path/to/config.yaml
```

---

## 版本变更日志 (CHANGELOG)

### v1.1.0 — 2026-06-30（配置标准化 & 功能融合）

**配置架构**
- 新增项目根目录 `config.yaml` 统一管理所有后端外部 API
- 新增 `config.yaml.example` 模板与 `requirements.txt` 环境清单
- 后端采用 `@ConfigurationProperties` + `ExternalServiceConfig` 接口（SOLID）
- 启动时输出配置摘要（`ConfigurationStartupLogger`）
- `application.yml` 仅保留框架配置，通过 `spring.config.import` 加载 `config.yaml`

**功能（benjamin-dev + yjh-dev 融合）**
- 老人/子女绑定流程（绑定码、TTS 播报、推送通知）
- 老人端：资料页、消息详情、紧急联系人、绑定家人
- 子女端：真实老人列表、发送消息
- 后端：上传、天气、资料更新 API
- 数据库：`emergency_contact` 等 profile 字段

**Bug 修复 / 开发体验**
- 修复登录 500：`user` 表缺少 `emergency_contact` 字段
- 修复 Android 模拟器网络：debug `BASE_URL` 改为 `10.0.2.2`
- 修复 JPush 编译：移除不兼容的 `XiaoBanJPushReceiver`
- 新增 `xiaoban-server/run.sh`、`stop.sh` 运维脚本

**接口变更**
- 绑定 API 增强：`BindingRelationVO`、`GenerateCodeVO`、绑定成功 JPush
- `/api/auth/profile` 需 JWT 认证

---

### v1.0.0 — 初始 MVP

- 用户注册/登录（老人/子女双角色）
- AI 语音对话（DashScope + 讯飞 SDK）
- 关键词健康告警 + 极光推送
- 家属消息、提醒、智能日报基础能力
- Swagger API 文档

---

## 技术栈

| 层级 | 技术 |
|------|------|
| 后端 | Spring Boot 3.2.5、MyBatis-Plus、MySQL 8、JWT、SpringDoc |
| AI | 阿里云通义千问 DashScope |
| 推送 | 极光 JPush |
| Android | Java 17、Retrofit、讯飞语音 SDK、高德定位 |
| 配置 | config.yaml + Spring ConfigurationProperties |

---

## API 文档

后端启动后访问：http://localhost:8080/swagger-ui.html

主要接口：
- `POST /api/auth/register` — 注册
- `POST /api/auth/login` — 登录
- `POST /api/voice/chat` — AI 对话
- `POST /api/bind/*` — 绑定相关
- `GET /api/weather` — 天气

---

## 常见问题

### 登录/注册「网络异常」
- 模拟器确认 `build.gradle` 为 `http://10.0.2.2:8080/`
- 真机改为电脑 WLAN IP，与后端同一网络
- Logcat 过滤 `OkHttp` 查看实际请求 URL

### 登录「服务器内部错误」
- 检查 `user` 表是否有 `emergency_contact` 字段
- 执行 `database/add_emergency_contact_field.sql`

### 后端启动找不到配置
```bash
cp config.yaml.example config.yaml
# 填写 config.yaml 后重启
```

### AI / 推送 / 天气不可用
- 检查 `config.yaml` 对应节点 `enabled: true` 且 API Key 已填写
- 查看启动日志中的 `[服务名] 可能未正确配置` 警告

---

## 开发规范

- **配置**：后端外部 API 只改 `config.yaml`，不在代码中硬编码 Key
- **提交**：`config.yaml` 不提交 Git；提交 `config.yaml.example` 变更
- **版本**：功能变更更新本文件 CHANGELOG 与 `meta.config-version`
- **原则**：SOLID — 配置类单一职责，Service 依赖配置抽象注入

---

## 团队与联系

- **项目负责人**：[@BenjaminSHI4008](https://github.com/BenjaminSHI4008)
- **Issues**：GitHub Issues
- **License**：MIT

---

<div align="center">

**用科技连接亲情，用 AI 陪伴老人** ❤️

</div>
