# WorkDay

WorkDay 是一款 Android 考勤与工时记录应用，适合日常打卡、工时统计、日历查看和数据管理。

## 功能特点

- 每日上下班打卡
- 自动计算工时与时长
- 日历视图查看打卡记录
- 支持手动补录和记录编辑
- 主题切换（跟随系统 / 浅色 / 深色）
- 导出打卡记录为 CSV 文件
- 清空全部数据（带确认提示）

## 技术栈

- Kotlin
- Jetpack Compose
- Android Gradle Plugin
- Material 3
- SQLite / 本地数据存储

## 运行方式

1. 安装 Android Studio
2. 打开本项目
3. 同步 Gradle
4. 运行 `app` 模块到模拟器或真实设备

## 生成发布包

```bash
./gradlew assembleRelease
```

## 发布与包管理

本仓库已接入 GitHub Actions 自动化流程：

- `Releases`：在创建版本标签后自动生成 GitHub Release，并上传 APK 产物
- `Packages`：通过 Maven 发布流程推送到 GitHub Packages

相关工作流位于：

- `.github/workflows/release.yml`
- `.github/workflows/publish-github-packages.yml`

## 项目结构

```text
.
├── app/
│   ├── src/
│   ├── build.gradle.kts
│   └── proguard-rules.pro
├── gradle/
├── build.gradle.kts
├── settings.gradle.kts
├── README.md
├── LICENSE
├── .gitignore
└── gradlew
```

## 许可证

本项目采用 MIT License。详情请见 [LICENSE](LICENSE)。
