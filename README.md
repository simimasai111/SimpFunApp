# 简幻欢 安卓客户端（Java 原生）

基于逆向得到的简幻欢 API，用 **原生 Java（Android）** 编写的安卓 App。
应用名「简幻欢」，底部三栏导航：

1. **服务器管理** —— 实例列表、启动/停止/重启电源操作
2. **邀请信息** —— 推荐码、邀请统计、邀请链接（一键复制）
3. **个人信息** —— 用户资料、积分/钻石、退出登录

首次进入强制登录（无 token 跳登录页），登录态用 `SharedPreferences` 持久化（等价于网页端 `localStorage.token`）。

---

## 技术栈

- 语言：**Java 17**
- UI：AndroidX + Material Components（`BottomNavigationView` / `TextInputLayout` / `MaterialButton`）
- 网络：原生 `HttpURLConnection` + `org.json`（**零第三方网络/JSON 依赖**，只依赖 AndroidX/Material）
- 异步：子线程请求 + 主线程 `Handler` 回调，未引入 RxJava / 协程 / OkHttp / Retrofit
- 最低支持：API 21（Android 5.0）

---

## 目录结构

```
SimpFunApp/
├── settings.gradle
├── build.gradle                 # 项目级：AGP 8.2.2 + 国内镜像仓库
├── gradle.properties
├── gradle/wrapper/
│   └── gradle-wrapper.properties
└── app/
    ├── build.gradle
    ├── proguard-rules.pro
    └── src/main/
        ├── AndroidManifest.xml
        ├── java/com/simpfun/app/
        │   ├── LoginActivity.java
        │   ├── MainActivity.java
        │   ├── api/ApiClient.java          # GET/POST 封装（form-urlencoded + Authorization）
        │   ├── util/Prefs.java             # token / 用户信息存储
        │   ├── model/Instance.java         # 实例模型（字段多候选兼容）
        │   ├── model/InviteInfo.java       # 邀请模型
        │   └── fragment/
        │       ├── ServerFragment.java     # 第一栏
        │       ├── InviteFragment.java     # 第二栏
        │       └── ProfileFragment.java    # 第三栏
        └── res/
            ├── layout/  (activity_login / activity_main / fragment_* / item_instance)
            ├── menu/bottom_nav_menu.xml
            ├── values/ (strings / colors / styles / themes)
            ├── color/bottom_nav_tint.xml
            └── drawable/ (ic_launcher / ic_server / ic_invite / ic_profile)
```

---

## 构建方式

> ⚠️ 当前开发机未安装 Android SDK / Gradle，无法在此直接产出 APK。
> 请在已配置 Android 开发环境的机器上按以下任一方式构建。

### 方式 A：Android Studio（推荐）

1. 打开 Android Studio → `Open` → 选择本项目根目录 `SimpFunApp/`
2. 首次打开会自动下载 Gradle 8.2 与依赖，并生成 `gradle/wrapper/gradle-wrapper.jar`、提示安装缺失的 SDK Platform（API 34）与 Build-Tools
3. 连接手机或启动模拟器，点击 ▶ `Run 'app'`（或 `Build → Build Bundle(s)/APK(s) → Build APK(s)`）
4. 产出 APK：`app/build/outputs/apk/debug/app-debug.apk`

### 方式 B：命令行 Gradle

```bash
# 若本机已装 Gradle，先生成 wrapper（否则直接用 gradle 命令）
gradle wrapper --gradle-version 8.2

# 调试包
./gradlew assembleDebug        # Windows: gradlew.bat assembleDebug

# 安装到已连接设备
./gradlew installDebug
```

> 若出现 `SDK location not found`，在项目根创建 `local.properties`：
> ```
> sdk.dir=/path/to/Android/Sdk
> ```
> （Windows 示例：`sdk.dir=C:\\Users\\you\\AppData\\Local\\Android\\Sdk`）

---

## API 对接要点（来自逆向报告）

| 项 | 值 |
|----|----|
| 域名 | `https://api.simpfun.cn` |
| 鉴权 | 请求头 `Authorization: <token>` |
| 请求体 | 非 GET 一律 `application/x-www-form-urlencoded`（**非 JSON**） |
| 响应 | 统一 `{ code, msg, data }`，`code==200` 为成功 |
| 登录 | `POST /api/auth/login`  body: `username`, `passwd` |
| 退出 | 清除本地 token 即可（后端无独立登出接口需求，前端按需调用） |

主要接口：

- `GET /api/ins/list` —— 服务器实例列表
- `GET /api/ins/{id}/power?action=start|stop|restart` —— 电源操作
- `GET /api/invite` —— 邀请信息（推荐码 + 统计）
- `GET /api/auth/info` —— 当前用户资料

---

## 字段兼容说明（重要）

逆向阶段**未实际登录**，实例列表与用户资料的字段名是按 Pterodactyl 风格 + 常见命名推断的。
代码中已做**多候选 key 兼容** + **原始 JSON 兜底展示**：

- `Instance.from()` 兼容 `instance_uuid/uuid/ins_id`、`friendly_name/name`、`state/status`、`version_name/version` 等
- `ProfileFragment` 兼容 `username/user/nickname`、`uid/user_id`、`points/point/integral`、`diamond/diamonds`、`verified/is_verified/realname`

拿到真实接口响应后，若字段不一致，只需在 `model/Instance.java` 与 `ProfileFragment.java` 的候选 key 列表中补上真实字段名即可，无需改动网络层。

---

## 后续可扩展（API 已逆向，随时可接）

- 终端 WebSocket：`GET /api/ins/{id}/ws` → Pterodactyl Wings 协议（日志推送 + 命令输入）
- 文件管理：`/api/ins/{id}/file/*`（列表/上传/下载/编辑/删除）
- 备份回档：`/api/ins/{id}/backup`、`/api/ins/{id}/rollback`
- 创建实例：`POST /api/ins/create`、`/api/games/list` 选版本
- 充值/商店：`/api/recharge`、`/api/shop/list`

完整接口清单见同工作区的 `简幻欢API逆向分析报告.md`。
