# 小米 Watch 5 · 音乐播放器（简化版 APK 工程）

Wear OS 手表端音乐播放器原型：**纯黑极简风**，三张横向滑动卡片，屏幕下方贴边**半圆弧进度条**，
音量调节为**整屏高模糊背景的二级菜单弹层**（无边框卡片）。

> 为什么用 Jetpack Compose 而不是 SwiftUI：SwiftUI 仅支持 iOS/watchOS，Wear OS 手表（小米 Watch 5 为
> Android/Wear OS）对应方案是 **Jetpack Compose for Wear OS**。两者在理念上同源（声明式 UI + 状态驱动），
> 本工程即为此架构。

## 功能映射

| 需求 | 实现 |
| --- | --- |
| 三卡横向滑动 | `HorizontalPager` + 随偏移快速淡入淡出 |
| 封面页（卡片 1） | `CoverCard`：渐变封面 + 长按播放/暂停，无控制按钮 |
| 歌词页（卡片 2） | `LyricCard`：蓝牙逐句推送，只显示当前句、大字号居中，句切换带模糊 + 上下跳动动画 |
| 控制页（卡片 3） | `ControlCard`：上一首 / 播放暂停 / 下一首 + 音量入口，无循环/随机（精简） |
| 下方半圆进度条 | `ArcOverlay` 绘制 + `arcSeekGesture` 手势（起点在进度带内才接管，否则交还 pager 滑动） |
| 拖拽显示秒数 | `TimePill`：随进度实时显示 `m:ss` |
| 音量二级菜单 | `VolumeOverlay`：展开时主内容层整体高模糊（`RenderEffect` blur 30px，API 31+；低版本仅压暗），弹层无边框 |
| 蓝牙断连 | `OfflineOverlay`：整屏「手机未连接」，暂停刷新，恢复后自动继续 |
| 极简纯黑 | `Theme.kt`：纯黑背景 + 克制蓝 + 白色玻璃按钮 |

## 目录结构

```
watch5-music-app/
├── .github/workflows/build-apk.yml   # GitHub Actions 云构建 → 输出 APK
├── gradle/libs.versions.toml
├── settings.gradle.kts / build.gradle.kts / gradle.properties
└── app/
    ├── build.gradle.kts
    └── src/main/
        ├── AndroidManifest.xml
        ├── res/                      # 纯黑主题、字符串、自适应图标
        └── java/com/xiaomi/watch5music/
            ├── MainActivity.kt
            ├── media/MediaSessionBridge.kt   # 蓝牙媒体桥（接口占位 + 实现指引）
            └── ui/
                ├── PlayerViewModel.kt        # 播放状态（原型用定时器模拟）
                ├── MusicPlayerScreen.kt      # 主界面组装
                ├── ArcOverlay.kt / ArcGesture.kt
                ├── CoverCard.kt / LyricCard.kt / ControlCard.kt
                ├── VolumeOverlay.kt / Components.kt / theme/Theme.kt
                └── model/Song.kt             # 示例歌曲 + 逐句歌词
```

## 构建方式

**本地（Android Studio）**：用 Android Studio 打开 `watch5-music-app/`，等待 Gradle Sync（会自动生成 wrapper），
然后 Build > Generate APK；或命令行：

```bash
gradle wrapper --gradle-version 8.7   # 生成 wrapper（一次性）
./gradlew assembleDebug
# 产物：app/build/outputs/apk/debug/app-debug.apk
```

**云构建（GitHub Actions）**：把本目录推到 GitHub 仓库，Actions 会自动运行
`.github/workflows/build-apk.yml`，构建完成后在 Artifacts 中下载 `watch5-music-debug-apk`。

## 真实蓝牙集成指引（后续接入）

1. 手机端配套 App 读取系统 MediaSession（需媒体通知读取权限 + 蓝牙权限），把
   歌名 / 歌手 / 封面 / 进度 / 歌词编码进 BLE GATT 特征值（UUID 见 `MediaSessionBridge`）。
2. 手表端 `BluetoothGatt` 订阅特征值通知实时同步；向命令特征值写入 播放/暂停/切歌/音量/seek 指令。
3. 歌词按 LRC 时间轴在换句时只推当前句文本（逐句推送），手表端直接显示。
4. 进度双向：手表拖圆弧 → 写进度特征值 → 手机跳转；手机进度变化 → 通知特征值 → 手表刷新。

## 已知简化点

- 进度 / 蓝牙为模拟态（`PlayerViewModel` 定时器 + `connected` 演示开关）。
- 封面为渐变色占位（可替换为手机端推送的 Bitmap）。
- 圆弧拖拽仅在「下方半圆进度带」内生效，上半屏留给卡片滑动（物理上符合贴边圆弧操作直觉）。
- 背景高模糊依赖 API 31+（RenderEffect）；低版本自动退化为压暗遮罩。
- 无循环 / 随机按钮（按需求精简）。
