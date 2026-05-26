package com.example.worddaily.data.repository

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.worddaily.data.local.WordDao
import com.example.worddaily.data.local.ProgressDao
import com.example.worddaily.data.local.WordEntity
import com.example.worddaily.data.local.UserProgressEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class WordRepository(private val wordDao: WordDao, private val progressDao: ProgressDao) {

    companion object {
        const val DEFAULT_USER_ID = "default_user"
        private const val DATE_FORMAT = "yyyy-MM-dd"
    }

    private val _progressState = MutableLiveData<UserProgressEntity?>()
    var progressState: LiveData<UserProgressEntity?> = _progressState

    // 从 assets 加载单词列表
    suspend fun loadWordsFromAssets(context: Context): List<WordEntity> {
        return withContext(Dispatchers.IO) {
            try {
                val jsonText = context.assets.open("vocabulary.json")
                    .bufferedReader()
                    .use { it.readText() }

                val wordJson = JSONObject(jsonText)
                val categories = wordJson.getJSONArray("categories")
                val allWords = mutableListOf<WordEntity>()

                for (i in 0 until categories.length()) {
                    val category = categories.getJSONObject(i)
                    val wordsArray = category.getJSONArray("words")

                    for (j in 0 until wordsArray.length()) {
                        val wordObj = wordsArray.getJSONObject(j)
                        allWords.add(
                            WordEntity(
                                id = wordObj.getString("word"),
                                word = wordObj.getString("word"),
                                pronunciation = wordObj.optString("phonetic", ""),
                                partOfSpeech = wordObj.optString("pos", ""),
                                definition = wordObj.getString("definition"),
                                exampleSentenceEn = wordObj.optString("example", ""),
                                exampleSentenceCn = wordObj.optString("example_cn", ""),
                                difficultyLevel = 1
                            )
                        )
                    }
                }

                allWords
            } catch (e: Exception) {
                println("Error loading words from assets: ${e.message}")
                emptyList()
            }
        }
    }

    suspend fun initializeDatabase(context: Context) {
        withContext(Dispatchers.IO) {
            val existingWords = wordDao.getAllWords()
            if (existingWords.isEmpty()) {
                val allWords = loadWordsFromAssets(context)
                if (allWords.isNotEmpty()) {
                    allWords.forEach { wordDao.insertWord(it) }
                }
            }

            if (progressDao.getProgress(DEFAULT_USER_ID) == null) {
                progressDao.saveProgress(
                    UserProgressEntity(
                        userId = DEFAULT_USER_ID,
                        totalStudyDays = 0,
                        masteredWordIds = "",
                        wrongWordIds = "",
                        lastStudyDate = null,
                        totalScore = 0,
                        todayLearned = 0
                    )
                )
            }
        }
    }

    suspend fun getAllWords(): List<WordEntity> {
        return withContext(Dispatchers.IO) {
            wordDao.getAllWords()
        }
    }

    suspend fun getWordsByIds(wordIds: Set<String>): List<WordEntity> {
        return withContext(Dispatchers.IO) {
            if (wordIds.isEmpty()) emptyList()
            else wordDao.getWordsByIds(wordIds.toList())
        }
    }

    /**
     * 获取用户进度，同时检查并处理日期变化：
     * 1. 如果跨天了，重置 todayLearned = 0
     * 2. 如果中断了（lastStudyDate 不是昨天也不是今天），重置连续天数
     */
    suspend fun getUserProgress(userId: String = DEFAULT_USER_ID): UserProgressEntity? {
        return withContext(Dispatchers.IO) {
            val progress = progressDao.getProgress(userId) ?: return@withContext null

            val today = getTodayString()
            val yesterday = getYesterdayString()

            var needUpdate = false
            var updatedProgress = progress

            // 检查是否跨天（0点重置今日学习数）
            if (progress.lastStudyDate != today) {
                // 跨天了，重置今日学习数
                updatedProgress = updatedProgress.copy(todayLearned = 0)
                needUpdate = true

                // 检查连续天数是否中断
                if (progress.lastStudyDate != null && progress.lastStudyDate != yesterday) {
                    // lastStudyDate 既不是今天也不是昨天，说明中断了
                    // 比如：lastStudyDate = "2026-05-11"，today = "2026-05-13"，中间断了一天
                    val daysBetween = getDaysBetween(progress.lastStudyDate!!, today)
                    if (daysBetween > 1) {
                        // 中断超过1天，重置连续学习天数
                        updatedProgress = updatedProgress.copy(totalStudyDays = 0)
                        needUpdate = true
                    }
                }
            }

            if (needUpdate) {
                progressDao.saveProgress(updatedProgress)
            }

            _progressState.postValue(updatedProgress)
            updatedProgress
        }
    }

    /**
     * 保存学习结果
     * @param correctWordIds 本次答对的单词ID
     * @param wrongWordIds   本次答错的单词ID
     */
    suspend fun saveLearningResult(
        correctWordIds: Set<String>,
        wrongWordIds: Set<String>
    ) {
        withContext(Dispatchers.IO) {
            val progress = progressDao.getProgress(DEFAULT_USER_ID) ?: return@withContext

            val today = getTodayString()
            val yesterday = getYesterdayString()

            // 合并已掌握的单词（去重）
            val existingMastered = progress.masteredWordIds
                .split(",").filter { it.isNotBlank() }.toMutableSet()
            existingMastered.addAll(correctWordIds)

            // 合并错题（答对的从错题中移除）
            val existingWrong = progress.wrongWordIds
                .split(",").filter { it.isNotBlank() }.toMutableSet()
            existingWrong.addAll(wrongWordIds)
            existingWrong.removeAll(correctWordIds)

            // 判断连续学习天数
            val isNewDay = progress.lastStudyDate != today
            val newStudyDays: Int
            val newTodayLearned: Int

            if (isNewDay) {
                // 新的一天
                if (progress.lastStudyDate == yesterday) {
                    // 昨天学过，连续天数 +1
                    newStudyDays = progress.totalStudyDays + 1
                } else if (progress.lastStudyDate == null) {
                    // 首次学习
                    newStudyDays = 1
                } else {
                    // 中断了（不是昨天也不是今天），重置为 1
                    val daysBetween = getDaysBetween(progress.lastStudyDate!!, today)
                    newStudyDays = if (daysBetween > 1) 1 else progress.totalStudyDays + 1
                }
                // 新的一天，今日学习数从本次开始
                newTodayLearned = correctWordIds.size + wrongWordIds.size
            } else {
                // 同一天，连续天数不变
                newStudyDays = progress.totalStudyDays
                // 今日学习数累加
                newTodayLearned = progress.todayLearned + correctWordIds.size + wrongWordIds.size
            }

            val updatedProgress = progress.copy(
                totalStudyDays = newStudyDays,
                masteredWordIds = existingMastered.joinToString(","),
                wrongWordIds = existingWrong.joinToString(","),
                lastStudyDate = today,
                totalScore = progress.totalScore + correctWordIds.size,
                todayLearned = newTodayLearned
            )

            progressDao.saveProgress(updatedProgress)
            _progressState.postValue(updatedProgress)
        }
    }

    // ========== 日期工具方法 ==========

    /** 获取今天日期字符串 yyyy-MM-dd */
    private fun getTodayString(): String {
        return SimpleDateFormat(DATE_FORMAT, Locale.getDefault()).format(Date())
    }

    /** 获取昨天日期字符串 yyyy-MM-dd */
    private fun getYesterdayString(): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        return SimpleDateFormat(DATE_FORMAT, Locale.getDefault()).format(calendar.time)
    }

    /** 计算两个日期之间的天数差 */
    private fun getDaysBetween(dateStr1: String, dateStr2: String): Long {
        return try {
            val sdf = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
            val date1 = sdf.parse(dateStr1)
            val date2 = sdf.parse(dateStr2)
            if (date1 != null && date2 != null) {
                val diffMillis = date2.time - date1.time
                TimeUnit.DAYS.convert(diffMillis, TimeUnit.MILLISECONDS)
            } else {
                999 // 解析失败，视为中断
            }
        } catch (e: Exception) {
            999 // 异常视为中断
        }
    }
}
