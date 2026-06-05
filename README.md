# XiaoBan

<div align="center">

**AI-Powered Elderly Companion App — XiaoBan Always By Your Side, Family Always At Ease**

[![Android](https://img.shields.io/badge/Android-26%2B-green.svg)](https://developer.android.com)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

</div>

---

## 📖 Project Overview

**XiaoBan** is an AI-powered voice companion Android application designed for elderly users, featuring a dual-interface architecture: "Elder Mode + Family Mode".

- **Elder Mode**: Communicate with AI assistant "XiaoBan" via voice to get daily consultation, health reminders, and emotional companionship
- **Family Mode**: Remotely monitor parents' status, view intelligent daily reports, verify AI responses, and set remote reminders

## ✨ Core Features

- 🎤 **Full Voice Interaction** - Zero learning curve for elderly users, just press and speak
- 🛡️ **AI + Family Verification** - Industry-first dual safety mechanism where important responses are verified by family members
- 📊 **AI-Powered Daily Reports** - Automatically generate reports on elderly activity, emotional state, and health concerns
- 🔔 **Keyword Alerts** - Health-related questions are automatically flagged and pushed to family members
- 🔐 **Semantic Privacy Protection** - Privacy mode triggered when elderly says "don't tell my children"
- 🎨 **Elderly-Friendly Design** - Large fonts, big buttons, minimalist interface, voice-first interaction

## 📁 Project Structure

```
AIGC/
├── xiaoban-server/          # Spring Boot Backend Service
│   ├── src/
│   │   ├── main/java/       # Java source code
│   │   └── resources/       # Configuration files
│   ├── pom.xml              # Maven configuration
│   └── README.md            # Backend documentation
│
├── XiaoBan/                 # Android Client
│   ├── app/src/             # Application source code
│   ├── build.gradle         # Gradle configuration
│   └── README.md            # Frontend documentation
│
├── Phase*.md                # Development documentation
├── debug.md                 # Bug fix checklist
├── BUG_FIX_SUMMARY.md       # Bug fix summary
├── .gitignore               # Git ignore file
└── README.md                # This file
```

## 🔧 Tech Stack

### Backend Technologies
- **Framework**: Spring Boot 3.2.5
- **Database**: MySQL 8.0 + MyBatis-Plus 3.5.5
- **AI Engine**: Qwen LLM (DashScope API)
- **Push Service**: JPush
- **Authentication**: JWT Token
- **Documentation**: SpringDoc OpenAPI (Swagger UI)

### Frontend Technologies
- **Language**: Java 17
- **Min SDK**: Android 8.0 (API 26)
- **Target SDK**: Android 13 (API 33)
- **Networking**: Retrofit 2.9.0 + OkHttp 4.12.0
- **Voice Recognition/Synthesis**: iFlytek Voice SDK
- **Push**: JPush 5.2.0
- **Local Database**: Room 2.6.1
- **Image Loading**: Glide 4.16.0

## 🚀 Quick Start

### Requirements

**Backend:**
- JDK 17
- Maven 3.6+
- MySQL 8.0+

**Frontend:**
- Android Studio Hedgehog (2023.1.1) or higher
- JDK 17
- Android SDK (API 26+)

### Backend Deployment

#### 1. Create Database

```bash
mysql -u root -p
CREATE DATABASE xiaoban_mvp CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

Execute initialization script (if available):
```bash
mysql -u root -p xiaoban_mvp < database/init.sql
```

#### 2. Configure Environment Variables

Set environment variables or modify `application.yml`:

```bash
# Windows
set DB_PASSWORD=your_mysql_password
set AI_API_KEY=your_dashscope_api_key
set JPUSH_APP_KEY=your_jpush_appkey
set JPUSH_MASTER_SECRET=your_jpush_secret
set JWT_SECRET=your_jwt_secret_key

# Linux/Mac
export DB_PASSWORD=your_mysql_password
export AI_API_KEY=your_dashscope_api_key
export JPUSH_APP_KEY=your_jpush_appkey
export JPUSH_MASTER_SECRET=your_jpush_secret
export JWT_SECRET=your_jwt_secret_key
```

**Get API Keys:**
- Qwen API: https://dashscope.aliyun.com/
- JPush: https://www.jiguang.cn/

#### 3. Start Backend Service

```bash
cd xiaoban-server
mvn clean install
mvn spring-boot:run
```

Service will start at `http://localhost:8080`.

Access Swagger UI: http://localhost:8080/swagger-ui.html

### Frontend Configuration

#### 1. Configure Constants.java

Edit `XiaoBan/app/src/main/java/com/xiaoban/app/base/Constants.java`:

```java
public static final String BASE_URL = "http://your_server_ip:8080/";
public static final String IFLYTEK_APP_ID = "your_iflytek_appid";
public static final String JPUSH_APP_KEY = "your_jpush_appkey";
```

**Get SDK Keys:**
- iFlytek Voice SDK: https://www.xfyun.cn/
- JPush: https://www.jiguang.cn/

#### 2. Add iFlytek SDK .so Files

Place iFlytek Voice SDK native library files in:
```
XiaoBan/app/libs/
├── Msc.jar (included)
├── arm64-v8a/
│   └── libmsc.so (need to download)
└── armeabi-v7a/
    └── libmsc.so (need to download)
```

#### 3. Sync and Run

1. Open `XiaoBan` directory in Android Studio
2. Sync Gradle: `File` → `Sync Project with Gradle Files`
3. Connect Android device or start emulator
4. Click Run ▶️

## 📱 Feature Modules

### Elder Mode Features
- **AI Voice Chat** - Press to speak, AI responds in real-time with voice playback
- **Health Monitoring** - Health-related questions are automatically flagged and pushed to family
- **Family Messages** - Receive text and voice messages from family members
- **Emergency Call** - One-tap emergency contact dialing

### Family Mode Features
- **Smart Daily Reports** - View parents' daily conversation statistics, topic distribution, and health concerns
- **Response Verification** - Review AI responses and verify or correct critical information
- **Remote Reminders** - Set medication, exercise, and other scheduled reminders
- **Device Management** - Bind parents' phones via Bluetooth or device code

## 🗂️ API Documentation

After backend starts, access: http://localhost:8080/swagger-ui.html

Main Endpoints:
- `POST /api/auth/register` - User registration
- `POST /api/auth/login` - User login
- `POST /api/chat/send` - Send conversation message
- `GET /api/elder/daily-report` - Get smart daily report
- `POST /api/reminder/create` - Create reminder

## 🐛 Troubleshooting

### Backend Common Issues

**Q: Database connection failed**
- Check if MySQL service is running
- Verify `DB_PASSWORD` environment variable is set
- Confirm database name is `xiaoban_mvp`

**Q: AI chat returns error**
- Verify `AI_API_KEY` is correctly configured
- Check Qwen API account balance
- Check backend logs for detailed error messages

### Frontend Common Issues

**Q: Voice recognition not responding**
- Confirm microphone permission is granted
- Check if iFlytek AppID is correct
- Verify .so files are correctly placed in `libs/` directory

**Q: Network request failed**
- Check if `BASE_URL` is correct
- Verify phone can access backend service (same LAN or use public IP)
- Check Logcat logs for specific errors

For detailed bug fixes, see `debug.md` and `BUG_FIX_SUMMARY.md`.

## 📄 Development Documentation

- `Phase1.md` - Requirements analysis and prototype design
- `Phase2.md` - Technical architecture and API design
- `Phase3.md` - Implementation details and test reports
- `debug.md` - Bug checklist
- `BUG_FIX_SUMMARY.md` - Bug fix summary
- `xiaoban-server/README.md` - Detailed backend documentation
- `XiaoBan/README.md` - Android client detailed documentation

## 🤝 Contributing

Issues and Pull Requests are welcome!

**Development Standards:**
- Follow Alibaba Java Development Guidelines
- Use Conventional Commits for commit messages
- Code must pass all test cases

## 📝 License

This project is licensed under the [MIT License](LICENSE).

## 👥 Team Members

- **Project Lead**: [@BenjaminSHI4008](https://github.com/BenjaminSHI4008)

## 🙏 Acknowledgments

Thanks to the following technologies and services:
- [Qwen](https://tongyi.aliyun.com/) - AI conversation engine
- [iFlytek](https://www.xfyun.cn/) - Voice recognition and synthesis
- [JPush](https://www.jiguang.cn/) - Message push service
- [Spring Boot](https://spring.io/projects/spring-boot) - Backend framework
- [Android Open Source Project](https://source.android.com/) - Android platform

## 📞 Contact

- **GitHub Issues**: [Submit Issue](https://github.com/BenjaminSHI4008/Intelligent-elderly-care-companion-products/issues)
- **Email**: support@xiaoban.com

---

<div align="center">

**Connecting Families Through Technology, Accompanying the Elderly with AI** ❤️

Made with ❤️ by XiaoBan Team

</div>
