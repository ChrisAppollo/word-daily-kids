package com.example.worddaily.util

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import java.net.URLEncoder

/**
 * TTS 发音管理器
 * 单词: 有道词典 TTS（美式发音）
 * 例句: Google Translate TTS（句子流畅）
 */
class TtsManager(private val context: Context) {

    companion object {
        private const val TAG = "TtsManager"
        private const val YOUDAO_TTS = "https://dict.youdao.com/dictvoice"
        private const val GOOGLE_TTS = "https://translate.googleapis.com/translate_tts"
        private const val MAX_RETRIES = 2
    }

    private var mediaPlayer: MediaPlayer? = null
    var isReady = true
        private set

    init {
        Log.d(TAG, "TTS 初始化")
    }

    fun speak(text: String) {
        if (text.isBlank()) return
        releasePlayer()

        val isSentence = text.contains(' ')
        val encoded = URLEncoder.encode(text, "UTF-8")

        val url = if (isSentence) {
            // Google Translate TTS: tl=en, client=gtx
            "$GOOGLE_TTS?ie=UTF-8&q=$encoded&tl=en&client=gtx"
        } else {
            // 有道词典 TTS: type=2 美式
            "$YOUDAO_TTS?audio=$encoded&type=2"
        }

        Log.d(TAG, "播放(${if (isSentence) "例句/Google" else "单词/有道"}): $text")
        playUrl(url, 0)
    }

    private fun playUrl(url: String, retryCount: Int) {
        try {
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setOnPreparedListener { mp ->
                    Log.d(TAG, "开始播放")
                    mp.start()
                }
                setOnCompletionListener { mp ->
                    Log.d(TAG, "播放完成")
                    releasePlayer()
                }
                setOnErrorListener { mp, what, extra ->
                    Log.e(TAG, "播放错误: what=$what, extra=$extra, retry=$retryCount")
                    releasePlayer()
                    // 重试
                    if (retryCount < MAX_RETRIES) {
                        Log.d(TAG, "重试播放 (${retryCount + 1}/$MAX_RETRIES)")
                        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                            playUrl(url, retryCount + 1)
                        }, 500)
                    }
                    true
                }
                val headers = HashMap<String, String>()
                headers["User-Agent"] = "Mozilla/5.0 (Linux; Android 12) AppleWebKit/537.36"
                setDataSource(context, Uri.parse(url), headers)
                prepareAsync()
            }
        } catch (e: Exception) {
            Log.e(TAG, "播放失败: ${e.message}", e)
            releasePlayer()
        }
    }

    fun stop() {
        releasePlayer()
    }

    private fun releasePlayer() {
        try {
            mediaPlayer?.let {
                try { if (it.isPlaying) it.stop() } catch (_: Exception) {}
                it.release()
            }
        } catch (_: Exception) {}
        mediaPlayer = null
    }

    fun shutdown() {
        releasePlayer()
    }
}
