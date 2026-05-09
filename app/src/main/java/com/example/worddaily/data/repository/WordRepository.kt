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

class WordRepository(private val wordDao: WordDao, private val progressDao: ProgressDao) {
    
    companion object {
        const val DEFAULT_USER_ID = "default_user"
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
                                exampleSentenceCn = "",
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
    
    // 生成唯一的单词 ID
    private fun generateWordId(word: String): String {
        return word.lowercase().replace(Regex("\\W+"), "")
    }
    
    // 获取示例词库（用于首次安装）
    private fun getSampleWords(): List<WordEntity> {
        return listOf(
            WordEntity(
                id = "apple",
                word = "apple",
                pronunciation = "/ˈæpl/",
                partOfSpeech = "n.",
                definition = "苹果",
                exampleSentenceEn = "I like to eat apples.",
                exampleSentenceCn = "我喜欢吃苹果。",
                difficultyLevel = 1
            ),
            WordEntity(
                id = "banana",
                word = "banana",
                pronunciation = "/bəˈnɑːnə/",
                partOfSpeech = "n.",
                definition = "香蕉",
                exampleSentenceEn = "Monkeys love bananas.",
                exampleSentenceCn = "猴子喜欢香蕉。",
                difficultyLevel = 1
            )
        )
    }
    
    suspend fun initializeDatabase(context: Context) {
        withContext(Dispatchers.IO) {
            val existingWords = wordDao.getAllWords()
            if (existingWords.isEmpty()) {
                // 首次安装，加载完整词库
                val allWords = loadWordsFromAssets(context)
                if (allWords.isNotEmpty()) {
                    allWords.forEach { wordDao.insertWord(it) }
                } else {
                    // 如果 JSON 加载失败，使用示例词库
                    getSampleWords().forEach { wordDao.insertWord(it) }
                }
            }
            
            if (progressDao.getProgress(DEFAULT_USER_ID) == null) {
                progressDao.saveProgress(
                    UserProgressEntity(
                        userId = DEFAULT_USER_ID,
                        totalStudyDays = 0,
                        masteredWordIds = "",
                        lastStudyDate = null,
                        totalScore = 0
                    )
                )
            }
        }
    }
    
    suspend fun getAllWords(): List<WordEntity> {
        return withContext(Dispatchers.IO) {
            val words = wordDao.getAllWords()
            // 数据库为空时返回空列表，加载逻辑已在 initializeDatabase 中处理
            words
        }
    }
    
    suspend fun getUserProgress(userId: String = DEFAULT_USER_ID): UserProgressEntity? {
        return withContext(Dispatchers.IO) {
            progressDao.getProgress(userId).also {
                _progressState.postValue(it)
            }
        }
    }
    
    suspend fun updateProgress(
        userId: String,
        totalStudyDays: Int,
        masteredWordIds: String,
        lastStudyDate: String,
        totalScore: Int
    ) {
        withContext(Dispatchers.IO) {
            progressDao.saveProgress(
                UserProgressEntity(
                    userId = userId,
                    totalStudyDays = totalStudyDays,
                    masteredWordIds = masteredWordIds,
                    lastStudyDate = lastStudyDate,
                    totalScore = totalScore
                )
            )
            _progressState.value = progressDao.getProgress(userId)
        }
    }
}
