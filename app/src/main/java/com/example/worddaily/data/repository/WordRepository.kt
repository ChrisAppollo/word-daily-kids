package com.example.worddaily.data.repository

import com.example.worddaily.WordDailyApplication
import com.example.worddaily.data.local.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext

class WordRepository(private val application: WordDailyApplication) {

    private val db: WordDailyDB = application.database
    private val wordDao: WordDao = db.wordDao()
    private val progressDao: ProgressDao = db.progressDao()

    private val _progressState = MutableStateFlow<UserProgressEntity?>(null)
    val progressState: StateFlow<UserProgressEntity?> get() = _progressState

    companion object {
        private const val DEFAULT_USER_ID = "default_user"

        fun getSampleWords(): List<WordEntity> = listOf(
            WordEntity(1, "apple", "/ˈæpl/", "n.", "苹果", "I eat an apple every day.", "我每天吃一个苹果。"),
            WordEntity(2, "banana", "/bəˈnænə/", "n.", "香蕉", "Monkeys love bananas.", "猴子喜欢香蕉。"),
            WordEntity(3, "cat", "/kæt/", "n.", "猫", "The cat sleeps on the sofa.", "猫在沙发上睡觉。"),
            WordEntity(4, "dog", "/dɒɡ/", "n.", "狗", "My dog barks loudly.", "我的狗叫得很大声。"),
            WordEntity(5, "elephant", "/ˈelɪfənt/", "n.", "大象", "The elephant has a long nose.", "大象有一个长鼻子。"),
            WordEntity(6, "fish", "/fɪʃ/", "n.", "鱼", "Fish swim in the water.", "鱼在水里游。"),
            WordEntity(7, "green", "/ɡriːn/", "adj.", "绿色的", "The grass is green.", "草是绿色的。"),
            WordEntity(8, "happy", "/ˈhæpi/", "adj.", "快乐的", "She looks very happy today.", "她今天看起来很开心。"),
            WordEntity(9, "ice cream", "/aɪs kriːm/", "n.", "冰淇淋", "Let's eat ice cream!", "让我们吃冰淇淋吧！"),
            WordEntity(10, "juice", "/dʒuːs/", "n.", "果汁", "Orange juice is my favorite drink.", "橙汁是我喜欢的饮料。"),
            WordEntity(11, "kind", "/kaɪnd/", "adj.", "友好的", "He is a kind boy.", "他是一个好孩子。"),
            WordEntity(12, "large", "/lɑːdʒ/", "adj.", "大的", "This is a large book.", "这是一本大书。"),
            WordEntity(13, "monkey", "/ˈmʌŋki/", "n.", "猴子", "The monkey jumps on the tree.", "猴子在树上跳。"),
            WordEntity(14, "nice", "/naɪs/", "adj.", "美好的", "Nice to meet you!", "很高兴见到你！"),
            WordEntity(15, "orange", "/ˈɒrɪndʒ/", "n.", "橙子", "I like oranges very much.", "我非常喜欢橙子。"),
            WordEntity(16, "pig", "/pɪɡ/", "n.", "猪", "Pigs eat corn and vegetables.", "猪吃玉米和蔬菜。"),
            WordEntity(17, "quiet", "/ˈkwaɪət/", "adj.", "安静的", "Please be quiet in the library.", "请在图书馆保持安静。"),
            WordEntity(18, "red", "/red/", "adj.", "红色的", "The apple is red and sweet.", "苹果是红色又甜的。"),
            WordEntity(19, "school bus", "/skuːl bʌs/", "n.", "校车", "The school bus arrives at 7:30 AM.", "校车早上七点半到达。"),
            WordEntity(20, "teacher", "/ˈtiːtʃə(r)/", "n.", "老师", "Our teacher is very friendly.", "我们的老师很友好。")
        )
    }

    suspend fun initializeDatabase() {
        withContext(Dispatchers.IO) {
            val existingWords = wordDao.getAllWords()
            if (existingWords.isEmpty()) {
                getSampleWords().forEach { wordDao.insertWord(it) }
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
            if (words.isEmpty()) {
                getSampleWords().also { sample ->
                    sample.forEach { wordDao.insertWord(it) }
                }
            } else {
                words
            }
        }
    }

    suspend fun getUserProgress(userId: String = DEFAULT_USER_ID): UserProgressEntity? {
        return withContext(Dispatchers.IO) {
            progressDao.getProgress(userId).also {
                _progressState.value = it
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
