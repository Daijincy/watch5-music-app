package com.xiaomi.watch5music.media

/**
 * 手机端媒体会话桥（简化版 / 接口占位）。
 *
 * 真实实现建议：
 *  1. 手机端配套 App 读取系统 MediaSession（需媒体通知读取权限 + 蓝牙权限），
 *     将 专辑封面 / 歌名 / 歌手 / 进度 / 歌词 编码进 BLE GATT 特征值。
 *  2. 手表端通过 BluetoothGatt 订阅特征值通知，实时同步元信息与进度；
 *     向命令特征值写入指令，下发 播放 / 暂停 / 切歌 / 音量 / seek。
 *  3. 歌词按蓝牙逐句推送：手机端按 LRC 时间轴在换句时才推当前句文本，
 *     手表端只显示当前句（大字号居中），随进度一句一句前进。
 *
 * 本工程为 UI 原型：进度由 PlayerViewModel 模拟，蓝牙为演示态。
 */
object MediaSessionBridge {
    const val SERVICE_UUID = "0000ff00-0000-1000-8000-00805f9b34fb"
    const val CHAR_META = "0000ff01-0000-1000-8000-00805f9b34fb"      // 元信息（歌名/歌手/封面）
    const val CHAR_PROGRESS = "0000ff02-0000-1000-8000-00805f9b34fb" // 进度同步（双向）
    const val CHAR_COMMAND = "0000ff03-0000-1000-8000-00805f9b34fb"  // 控制指令（手表→手机）
    const val CHAR_LYRIC = "0000ff04-0000-1000-8000-00805f9b34fb"    // 逐句歌词推送（手机→手表）
}
