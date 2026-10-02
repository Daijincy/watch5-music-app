package com.xiaomi.watch5music.ui.model

/** 歌曲与逐句歌词（时间戳秒 → 文本，模拟蓝牙逐句推送） */
data class LyricLine(val startSec: Int, val text: String)

data class Song(
    val title: String,
    val artist: String,
    val durationSec: Int,
    val coverStart: Long,   // 封面渐变起始色
    val coverEnd: Long,     // 封面渐变结束色
    val lyrics: List<LyricLine>
)

object SampleSongs {
    val all: List<Song> = listOf(
        Song(
            title = "光年之外", artist = "G.E.M.邓紫棋", durationSec = 228,
            coverStart = 0xFF16213E, coverEnd = 0xFF4C3F8A,
            lyrics = listOf(
                LyricLine(0, "感受停在我发端的指尖"),
                LyricLine(16, "如何瞬间冻结时间"),
                LyricLine(32, "记住望着我坚定的双眼"),
                LyricLine(48, "也许已经没有明天"),
                LyricLine(64, "面对浩瀚的星海"),
                LyricLine(80, "我们微小得像尘埃"),
                LyricLine(96, "漂浮在一片无奈"),
                LyricLine(112, "缘分让我们相遇乱世以外"),
                LyricLine(130, "命运却要我们危难中相爱"),
                LyricLine(150, "也许未来遥远在光年之外")
            )
        ),
        Song(
            title = "晴天", artist = "周杰伦", durationSec = 269,
            coverStart = 0xFF2E5E8A, coverEnd = 0xFFD99A3D,
            lyrics = listOf(
                LyricLine(0, "故事的小黄花"),
                LyricLine(14, "从出生那年就飘着"),
                LyricLine(28, "童年的荡秋千"),
                LyricLine(42, "随记忆一直晃到现在"),
                LyricLine(58, "吹着前奏望着天空的"),
                LyricLine(74, "我想起花瓣试着掉落"),
                LyricLine(96, "为你翘课的那一天"),
                LyricLine(112, "花落的那一天"),
                LyricLine(130, "教室的那一间"),
                LyricLine(150, "我怎么看不见")
            )
        ),
        Song(
            title = "平凡之路", artist = "朴树", durationSec = 320,
            coverStart = 0xFF3A4650, coverEnd = 0xFF6B7A8C,
            lyrics = listOf(
                LyricLine(0, "徘徊着的 在路上的"),
                LyricLine(16, "你要走吗 via via"),
                LyricLine(32, "易碎的 骄傲着"),
                LyricLine(48, "那也曾是我的模样"),
                LyricLine(64, "沸腾着的 不安着的"),
                LyricLine(82, "你要去哪 via via"),
                LyricLine(100, "谜一样的 沉默着的"),
                LyricLine(118, "故事你真的在听吗"),
                LyricLine(136, "我曾经跨过山和大海"),
                LyricLine(156, "也穿过人山人海")
            )
        )
    )
}

/** 根据播放进度计算当前歌词行索引 */
fun currentLineIndex(song: Song, progress: Float): Int {
    val t = (progress * song.durationSec).toInt()
    var ci = 0
    song.lyrics.forEachIndexed { i, line -> if (t >= line.startSec) ci = i }
    return ci
}
