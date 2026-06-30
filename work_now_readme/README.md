# 小伴项目当前开发说明

本文档用于新开 Cursor 窗口后快速接续当前进度。项目是 Android 原生客户端 `XiaoBan` + Spring Boot 后端 `xiaoban-server`。

## 当前重点

最近主要在开发老人端首页、用户信息页、家人消息、AI聊天记录入口，以及子女端设置入口。

目前很多功能是前端原型，后端接口后续再接。

## 老人端首页

主要文件：

- `XiaoBan/app/src/main/java/com/xiaoban/app/elder/ElderHomeActivity.java`
- `XiaoBan/app/src/main/res/layout/activity_elder_home.xml`

当前已做：

- 右上角头像入口显示当前昵称首字，点击进入用户信息页。
- 家人消息卡片可点击进入家人消息列表。
- 家人消息红点逻辑是前端本地模拟：
  - 未读消息存在时首页红点显示。
  - 点进某条消息后只标记该条已读。
  - 所有未读消息均被阅读后，首页家人消息红点消失。
- 家人消息摘要当前为前端模拟：
  - 有未读时显示 `最新女儿发来1条语音消息`
  - 无未读时显示 `暂无未读家人消息`
- 家人消息下方新增同尺寸入口 `AI聊天记录`，点击进入 `ElderChatActivity` 并展示一组模拟 AI 对话。
- 已移除每次进入老人端首页后的欢迎气泡和语音播报逻辑。
- 紧急呼叫：
  - 未设置紧急联系人时默认拨 `120`
  - 设置了紧急联系人时拨紧急联系人手机号
  - 注意：Android 对 `120` 等系统紧急号码通常会强制进入系统紧急拨号界面，普通 App 不能保证静默直拨。

## 用户信息页

主要文件：

- `XiaoBan/app/src/main/java/com/xiaoban/app/elder/ElderProfileActivity.java`
- `XiaoBan/app/src/main/java/com/xiaoban/app/elder/ElderProfileEditActivity.java`
- `XiaoBan/app/src/main/res/layout/activity_elder_profile.xml`
- `XiaoBan/app/src/main/res/layout/activity_elder_profile_edit.xml`

当前已做：

- `我的信息` 顶部栏已恢复为浅色旧样式。
- 顶部栏现在固定在页面最上方，不随下方内容滚动。
- 实现方式：根布局改为 `RelativeLayout`，顶部 `top_bar` 独立放在最上层，下面的 `ScrollView` 使用 `android:layout_below="@id/top_bar"`。
- 用户信息展示：
  - 昵称
  - 角色
  - 手机号
  - 性别
  - 生日
  - 紧急联系人
  - 用户 ID
- 未填写字段显示 `未填写`。
- 子女端进入同一份用户信息页时，紧急联系人字段隐藏。
- 点击昵称或“点击修改名字”可弹窗修改昵称。
- 点击 `账号信息修改` 进入编辑页。
- 编辑页支持：
  - 性别只能选 `男 / 女`
  - 生日选择范围 `1850-01-01` 到 `2026-12-31`
  - 手机号弹窗修改
  - 老人端可修改紧急联系人
  - 子女端隐藏紧急联系人

后端已接入：

- `GET /api/auth/profile`
- `PUT /api/auth/profile`

后端相关文件：

- `xiaoban-server/src/main/java/com/xiaoban/server/controller/AuthController.java`
- `xiaoban-server/src/main/java/com/xiaoban/server/dto/ProfileUpdateRequest.java`
- `xiaoban-server/src/main/java/com/xiaoban/server/service/impl/AuthServiceImpl.java`
- `xiaoban-server/src/main/java/com/xiaoban/server/entity/User.java`
- `xiaoban-server/src/main/java/com/xiaoban/server/vo/UserProfileVO.java`

数据库新增字段：

- `gender`
- `birthday`
- `emergency_contact`

迁移脚本：

- `database/add_user_profile_fields.sql`
- `database/add_emergency_contact_field.sql`

如果用户信息页出现服务器内部错误，优先确认数据库是否已执行以上迁移。

## 家人消息

主要文件：

- `XiaoBan/app/src/main/java/com/xiaoban/app/elder/ElderMessageActivity.java`
- `XiaoBan/app/src/main/java/com/xiaoban/app/elder/ElderMessageDetailActivity.java`
- `XiaoBan/app/src/main/java/com/xiaoban/app/elder/adapter/MessageAdapter.java`
- `XiaoBan/app/src/main/java/com/xiaoban/app/model/Message.java`
- `XiaoBan/app/src/main/res/layout/activity_elder_message.xml`
- `XiaoBan/app/src/main/res/layout/activity_elder_message_detail.xml`
- `XiaoBan/app/src/main/res/layout/item_message_card.xml`

当前是前端原型，暂未接后端真实消息。

当前已做：

- 家人消息列表显示模拟数据：
  - 女儿语音消息
  - 儿子照片消息
  - 女儿语音消息
  - 儿子文字消息
- 列表项显示：
  - 发送人
  - 具体时间
  - 消息类型
  - 语音时长 / 照片提示 / 文字内容
- 未读消息右上角有红点。
- 点击某条消息进入详情页。
- 点击后该条消息标记为已读。
- 删除了列表页底部的 `按住录音回复`。

当前已读状态是本地模拟，存储在 `SharedPreferences`：

- key: `read_family_message_ids`

后续接后端时，需要用真实 message id 替代模拟 id，并通过接口标记已读。

## AI聊天记录入口

当前已做：

- 老人首页家人消息卡片下方新增入口 `AI聊天记录`。
- 点击后进入 `ElderChatActivity`。
- 传入模拟内容：
  - 用户问题：`小伴，我降压药什么时候吃？`
  - AI 回复：降压药建议固定时间、最好问医生
  - category: `health`

后续接后端时，需要新增/复用历史对话接口，进入页面后展示真实历史消息。

## 子女端首页设置入口

主要文件：

- `XiaoBan/app/src/main/java/com/xiaoban/app/child/ChildHomeActivity.java`
- `XiaoBan/app/src/main/res/layout/activity_child_home.xml`

当前已做：

- 移除了子女端首页右上角齿轮设置图标。
- 清理了右上角设置图标相关变量和空点击逻辑。
- 底部导航右下角 `设置` 已可点击。
- 点击后进入和老人端相同的用户信息页 `ElderProfileActivity`。
- 子女端进入该页面时不显示紧急联系人。

## 后端鉴权修复

之前用户信息页请求失败的原因是 `/api/auth/profile` 鉴权配置不合理。

当前已做：

- `SecurityConfig.java` 只放行：
  - `/api/auth/register`
  - `/api/auth/login`
- `/api/auth/profile` 需要有效 token。
- `AuthController.getProfile()` 和 `updateProfile()` 会检查 `Authentication` 是否有效。
- Android `ApiCallback` 已支持解析后端错误体，不再所有 HTTP 错误都显示笼统的“请求失败”。

相关文件：

- `xiaoban-server/src/main/java/com/xiaoban/server/security/SecurityConfig.java`
- `xiaoban-server/src/main/java/com/xiaoban/server/controller/AuthController.java`
- `XiaoBan/app/src/main/java/com/xiaoban/app/network/ApiCallback.java`

## 技术配置检查

已确认代码中仍存在以下配置和实现：

- 科大讯飞语音识别：
  - `VoiceRecognizer.java`
  - 使用 `SpeechRecognizer`
  - `ENGINE_TYPE = TYPE_CLOUD`
  - 中文普通话识别
- 科大讯飞 TTS：
  - `VoiceSynthesizer.java`
  - 使用 `SpeechSynthesizer`
  - 发音人 `xiaoyan`
- 讯飞初始化：
  - `BaseApplication.java`
  - `VoiceManager.java`
  - `Constants.IFLYTEK_APP_ID`
- 通义千问 DashScope：
  - `xiaoban-server/src/main/java/com/xiaoban/server/service/AiChatService.java`
  - `application.yml` 中 `ai.provider = dashscope`
  - model 当前为 `qwen-turbo`
- 极光推送：
  - Android 端依赖 `jpush`、`jcore`
  - 登录/注册后设置 JPush alias 为 userId
  - 后端 `PushService.java` 使用 JPushClient 按 alias 推送

注意事项：

- 当前 `XiaoBan/app/libs/Msc.jar` 和 `.so` native 库未在工作区中找到。讯飞语音在真机运行可能会失败，需要补回 SDK 文件。
- `application.yml` 中存在硬编码的 DashScope 和 JPush 密钥，后续建议改为环境变量，避免泄露。

## 当前已知问题

- Android Gradle wrapper 缺少 `gradle/wrapper/gradle-wrapper.jar`，所以之前无法通过 `gradlew.bat` 编译 Android。
- 当前工作区根目录不是 git 仓库，不能依赖 `git log` 或 `git status` 回溯。
- 部分前端功能仍是本地模拟：
  - 家人消息列表
  - 家人消息未读状态
  - AI聊天记录
  - 子女端首页部分统计数据

## 建议下一步

1. 补齐 Android Gradle wrapper 或用 Android Studio 本地 Gradle 编译。
2. 补回讯飞 `Msc.jar` 和 `.so` 文件。
3. 把家人消息列表和已读状态接后端。
4. 把 AI聊天记录入口接后端历史对话接口。
5. 把 `application.yml` 中密钥迁移到环境变量。
6. 对用户资料更新接口补完整异常提示和测试。
