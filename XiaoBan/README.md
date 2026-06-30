# 「小伴」智能陪伴系统 - Android客户端

<div align="center">

**一款面向老年人的AI语音陪伴应用**

[![Android](https://img.shields.io/badge/Android-26%2B-green.svg)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Language-Java-orange.svg)](https://www.java.com)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

</div>

---

## 📖 项目简介

「小伴」是一款专为老年人设计的智能语音陪伴应用，通过AI对话技术为老人提供日常陪伴、健康关注和家庭互动功能。

### 核心功能

- 🎤 **语音交互**：支持语音识别和TTS语音播报，老人无需打字即可使用
- 🤖 **AI陪伴**：智能对话系统，回答日常问题并提供情感陪伴
- 👨‍👩‍👧‍👦 **家庭互动**：子女端可远程查看老人对话记录和健康状况
- 🏥 **健康关注**：AI自动识别健康相关问题并提醒子女关注
- ⏰ **远程提醒**：子女可为老人设置服药、运动等提醒事项
- 📊 **智能日报**：每日生成老人活跃度和健康状况报告

---

## 🚀 快速开始

### 环境要求

- **操作系统**：Windows 10/11, macOS 10.14+, Linux
- **JDK**：JDK 17（必须）
- **Android Studio**：Hedgehog (2023.1.1) 或更高版本
- **Android SDK**：
  - SDK Platform: Android 13.0 (API 33) 或更高
  - Build Tools: 34.0.0 或更高
  - NDK: 建议 25.0+（用于.so库）
- **Gradle**：8.0+（项目自带Gradle Wrapper）

### 安装步骤

#### 1. 安装Android Studio

访问 [Android Studio官网](https://developer.android.com/studio) 下载并安装最新版本。

安装时确保勾选以下组件：
- Android SDK
- Android SDK Platform
- Android Virtual Device (AVD)

#### 2. 配置JDK 17

在Android Studio中：
1. 打开 `File` → `Project Structure` → `SDK Location`
2. 设置 `JDK location` 为 JDK 17 路径
3. 或使用Android Studio内置的JDK 17

#### 3. 克隆项目

```bash
# 克隆仓库
git clone https://github.com/your-username/xiaoban-android.git
cd xiaoban-android

# 或直接下载ZIP解压
```

#### 4. 配置项目

**修改后端API地址：**

编辑 `app/src/main/java/com/xiaoban/app/base/Constants.java`：

```java
public static final String BASE_URL = "http://YOUR_SERVER_IP:8080/";
```

将 `YOUR_SERVER_IP` 替换为实际的后端服务器地址。

**配置讯飞语音SDK（可选）：**

如需使用自己的讯飞AppID，修改 `Constants.java`：

```java
public static final String IFLYTEK_APP_ID = "your_iflytek_app_id";
```

#### 5. 下载讯飞SDK .so文件

将讯飞语音SDK的native库文件放置在以下目录：

```
app/libs/
├── Msc.jar                 (已包含在项目中)
├── arm64-v8a/
│   └── libmsc.so          (需要下载)
└── armeabi-v7a/
    └── libmsc.so          (需要下载)
```

从 [讯飞开放平台](https://www.xfyun.cn/) 下载SDK并提取.so文件。

#### 6. 同步项目

在Android Studio中：
1. 点击 `File` → `Sync Project with Gradle Files`
2. 等待依赖下载完成

#### 7. 运行项目

**在真机上运行（推荐）：**

1. 在Android手机上启用"开发者选项"和"USB调试"
2. 通过USB连接手机到电脑
3. 在Android Studio中选择你的设备
4. 点击运行按钮 ▶️

**在模拟器上运行：**

1. 打开 `Tools` → `Device Manager`
2. 创建新虚拟设备（推荐：Pixel 6, API 33）
3. 启动模拟器
4. 点击运行按钮 ▶️

---

## 📁 项目结构

```
XiaoBan/
├── app/                                    # 应用模块
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/xiaoban/app/
│   │   │   │   ├── adapter/                # RecyclerView适配器
│   │   │   │   ├── auth/                   # 认证模块（登录/注册）
│   │   │   │   ├── base/                   # 基础类（Application、BaseActivity、Constants）
│   │   │   │   ├── child/                  # 子女端界面
│   │   │   │   ├── elder/                  # 老人端界面
│   │   │   │   │   └── adapter/            # 老人端专用适配器
│   │   │   │   ├── model/                  # 数据模型（API和UI模型）
│   │   │   │   ├── network/                # 网络层（Retrofit、ApiService）
│   │   │   │   ├── voice/                  # 语音模块（识别和合成）
│   │   │   │   ├── push/                   # 推送模块
│   │   │   │   ├── util/                   # 工具类
│   │   │   │   └── widget/                 # 自定义控件
│   │   │   │
│   │   │   ├── res/                        # 资源文件
│   │   │   │   ├── layout/                 # 布局文件
│   │   │   │   ├── drawable/               # 图片和矢量图
│   │   │   │   ├── values/                 # 字符串、颜色、主题
│   │   │   │   └── xml/                    # 网络安全配置等
│   │   │   │
│   │   │   └── AndroidManifest.xml         # 应用清单
│   │   │
│   │   └── test/                           # 单元测试
│   │
│   ├── libs/                               # 第三方库（讯飞SDK）
│   │   ├── Msc.jar
│   │   ├── arm64-v8a/
│   │   └── armeabi-v7a/
│   │
│   └── build.gradle                        # 应用级Gradle配置
│
├── gradle/                                 # Gradle Wrapper
├── build.gradle                            # 项目级Gradle配置
├── gradle.properties                       # Gradle属性配置
├── settings.gradle                         # 项目设置
├── debug.md                                # Bug修复清单
├── BUG_FIX_SUMMARY.md                      # Bug修复总结
└── README.md                               # 本文件
```

---

## 🔧 核心技术栈

### Android核心
- **最低SDK版本**：Android 8.0 (API 26)
- **目标SDK版本**：Android 13 (API 33)
- **编程语言**：Java 17
- **构建工具**：Gradle 8.0

### 第三方库

| 库名称 | 版本 | 用途 |
|--------|------|------|
| AndroidX AppCompat | 1.6.1 | 兼容性支持 |
| Material Components | 1.11.0 | Material Design组件 |
| RecyclerView | 1.3.2 | 列表展示 |
| CardView | 1.0.0 | 卡片布局 |
| ConstraintLayout | 2.1.4 | 约束布局 |
| Retrofit | 2.9.0 | 网络请求 |
| Gson | 2.9.0 | JSON解析 |
| OkHttp | 4.12.0 | HTTP客户端 |
| Glide | 4.16.0 | 图片加载 |
| 讯飞语音SDK | 最新版 | 语音识别和合成 |
| 极光推送 | 5.2.0 | 消息推送 |
| Room | 2.6.1 | 本地数据库 |

---

## ⚙️ 配置说明

### 必需配置

#### 1. 后端API地址

编辑 `Constants.java`：

```java
public static final String BASE_URL = "http://192.168.1.14:8080/";
```

#### 2. 讯飞AppID

在 [讯飞开放平台](https://www.xfyun.cn/) 注册应用并获取AppID：

```java
public static final String IFLYTEK_APP_ID = "c3ca7abf";
```

#### 3. 极光推送AppKey

在 [极光推送控制台](https://www.jiguang.cn/) 创建应用并获取AppKey：

```java
public static final String JPUSH_APP_KEY = "your_jpush_appkey";
```

### 可选配置

#### 网络安全配置

如需允许HTTP明文传输（仅开发环境），保持 `res/xml/network_security_config.xml` 的默认配置：

```xml
<network-security-config>
    <base-config cleartextTrafficPermitted="true" />
</network-security-config>
```

**⚠️ 生产环境请使用HTTPS！**

#### 语音参数调整

在 `VoiceSynthesizer.java` 中可调整TTS参数：

```java
synthesizer.setParameter(SpeechConstant.VOICE_NAME, "xiaoyan");  // 发音人
synthesizer.setParameter(SpeechConstant.SPEED, "40");             // 语速（30-100）
synthesizer.setParameter(SpeechConstant.VOLUME, "80");            // 音量（0-100）
synthesizer.setParameter(SpeechConstant.PITCH, "50");             // 音调（0-100）
```

---

## 📱 功能模块详解

### 老人端

#### 1. 首页（ElderHomeActivity）
- 显示日期、天气信息
- 语音按钮：按住说话，松开识别
- 消息提醒卡片
- 紧急呼叫按钮

#### 2. AI对话（ElderChatActivity）
- 实时语音识别，无需打字
- AI智能回答，语音播报
- 健康问题自动标记和提醒
- 对话历史记录

#### 3. 家庭消息（ElderMessageActivity）
- 接收子女发送的消息
- 语音播报新消息
- 查看历史消息

### 子女端

#### 1. 首页（ChildHomeActivity）
- 父母设备在线状态
- 今日对话统计
- 待确认对话提醒
- 智能日报预览

#### 2. 对话确认（ChildCorrectActivity）
- 查看AI回答的对话记录
- 确认或纠正AI的回答
- 纠正内容推送给老人

#### 3. 智能日报（ChildDailyReportActivity）
- 每日对话次数统计
- 话题分布分析
- 健康关注点汇总
- 情绪趋势分析

#### 4. 远程提醒（ChildReminderActivity）
- 创建定时提醒（服药、运动等）
- 设置重复规则（每天、每周等）
- 开启/关闭提醒
- 编辑和删除提醒

#### 5. 设备管理（ChildBindActivity）
- 蓝牙扫描绑定设备
- 设备码绑定
- 查看设备详情
- 解绑设备

---

## 🐛 常见问题

### 编译问题

**Q: 编译报错 "SDK location not found"**

A: 在项目根目录创建 `local.properties` 文件：

```properties
sdk.dir=C\:\\Users\\YourName\\AppData\\Local\\Android\\Sdk
```

将路径替换为你的Android SDK实际路径。

**Q: 编译报错 "Unsupported Java version"**

A: 确保使用JDK 17：
1. `File` → `Project Structure` → `SDK Location`
2. 设置JDK为17版本

**Q: Gradle同步失败**

A: 尝试以下方法：
1. 检查网络连接
2. 在 `gradle.properties` 中添加代理配置（如果需要）
3. 使用 `File` → `Invalidate Caches / Restart`

### 运行问题

**Q: App安装后闪退**

A: 检查以下几点：
1. 讯飞SDK的.so文件是否正确放置在 `app/libs/` 下
2. 后端API地址是否可访问
3. 查看Logcat日志定位具体错误

**Q: 语音识别无反应**

A: 确认：
1. 麦克风权限已授权
2. 讯飞AppID配置正确
3. .so文件存在于 `libs/arm64-v8a/` 和 `libs/armeabi-v7a/`
4. 检查Logcat中讯飞SDK的日志

**Q: 网络请求失败**

A: 检查：
1. 后端服务是否正常运行
2. 手机/模拟器能否访问后端API地址
3. `network_security_config.xml` 配置是否正确

### 权限问题

**Q: 应用需要哪些权限？**

A: 必需权限：
- `INTERNET` - 网络访问
- `RECORD_AUDIO` - 麦克风（语音识别）
- `CALL_PHONE` - 紧急呼叫

可选权限：
- `BLUETOOTH_CONNECT` - 蓝牙连接
- `BLUETOOTH_SCAN` - 蓝牙扫描
- `ACCESS_FINE_LOCATION` - 位置（蓝牙扫描需要）

所有权限已在 `AndroidManifest.xml` 中声明，危险权限会在运行时申请。

---

## 🧪 测试

### 单元测试

```bash
# 运行所有单元测试
./gradlew test

# 运行特定测试
./gradlew test --tests com.xiaoban.app.ExampleUnitTest
```

### UI测试

```bash
# 运行所有UI测试（需要连接设备）
./gradlew connectedAndroidTest
```

### 手动测试场景

#### 老人端测试
1. 启动App → 注册老人账号
2. 按住语音按钮 → 说"今天天气怎么样"
3. 检查语音识别准确性
4. 等待AI回答并播报
5. 说"我头有点疼" → 检查是否出现健康提醒标记

#### 子女端测试
1. 启动App → 注册子女账号
2. 绑定老人设备（使用蓝牙或设备码）
3. 查看智能日报 → 验证数据展示
4. 创建远程提醒 → 验证老人端收到推送
5. 在待确认对话中纠正AI回答 → 验证老人端收到更新

---

## 📦 打包发布

### Debug版本

```bash
# 生成Debug APK
./gradlew assembleDebug

# APK位置：app/build/outputs/apk/debug/app-debug.apk
```

### Release版本

1. **生成签名密钥**：

```bash
keytool -genkey -v -keystore xiaoban.jks -keyalg RSA -keysize 2048 -validity 10000 -alias xiaoban
```

2. **配置签名**：

在 `app/build.gradle` 中添加：

```groovy
android {
    signingConfigs {
        release {
            storeFile file("path/to/xiaoban.jks")
            storePassword "your_store_password"
            keyAlias "xiaoban"
            keyPassword "your_key_password"
        }
    }
    buildTypes {
        release {
            signingConfig signingConfigs.release
            minifyEnabled true
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
    }
}
```

3. **生成Release APK**：

```bash
./gradlew assembleRelease

# APK位置：app/build/outputs/apk/release/app-release.apk
```

---

## 🤝 贡献指南

我们欢迎所有形式的贡献！

### 如何贡献

1. **Fork** 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启 **Pull Request**

### 代码规范

- 遵循 [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)
- 类名使用大驼峰命名（PascalCase）
- 方法和变量使用小驼峰命名（camelCase）
- 常量使用全大写加下划线（UPPER_SNAKE_CASE）
- 每个类和公共方法需要添加JavaDoc注释

### 提交信息规范

```
<type>(<scope>): <subject>

<body>

<footer>
```

类型（type）：
- `feat`: 新功能
- `fix`: Bug修复
- `docs`: 文档更新
- `style`: 代码格式调整
- `refactor`: 重构
- `test`: 测试相关
- `chore`: 构建/工具链更新

示例：
```
feat(voice): 添加离线语音识别功能

- 集成Kaldi离线识别引擎
- 添加语音模型下载功能
- 优化识别准确率

Closes #123
```

---

## 📄 开源协议

本项目采用 [MIT License](LICENSE) 开源协议。

---

## 👥 团队成员

- **项目负责人**：[@your-name](https://github.com/your-name)
- **Android开发**：[@developer1](https://github.com/developer1)
- **后端开发**：[@developer2](https://github.com/developer2)
- **UI设计**：[@designer](https://github.com/designer)

---

## 📞 联系我们

- **问题反馈**：[提交Issue](https://github.com/your-username/xiaoban-android/issues)
- **功能建议**：[发起Discussion](https://github.com/your-username/xiaoban-android/discussions)
- **邮箱**：support@xiaoban.com

---

## 🙏 致谢

感谢以下开源项目和服务：

- [Android Open Source Project](https://source.android.com/)
- [Retrofit](https://square.github.io/retrofit/)
- [Glide](https://github.com/bumptech/glide)
- [讯飞开放平台](https://www.xfyun.cn/)
- [极光推送](https://www.jiguang.cn/)

---

## 📊 项目状态

![GitHub stars](https://img.shields.io/github/stars/your-username/xiaoban-android?style=social)
![GitHub forks](https://img.shields.io/github/forks/your-username/xiaoban-android?style=social)
![GitHub issues](https://img.shields.io/github/issues/your-username/xiaoban-android)
![GitHub pull requests](https://img.shields.io/github/issues-pr/your-username/xiaoban-android)

---

<div align="center">

**用科技连接家庭，用AI陪伴老人** ❤️

Made with ❤️ by XiaoBan Team

</div>
