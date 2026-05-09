package com.example.worddaily.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

data class Word(
    val id: String = "",
    var word: String = "",
    var pronunciation: String = "",
    var partOfSpeech: String = "",
    var definition: String = "",
    var exampleSentenceEn: String = "",
    var exampleSentenceCn: String = "",
    var difficultyLevel: Int = 1
)

data class DailyTask(
    val date: Date,
    val wordIds: List<String>,
    var completedWords: Set<String> = emptySet(),
    var lastUpdateTime: Date = Date()
)

data class UserProgress(
    val userId: String = "default_user",
    val totalStudyDays: Int = 0,
    val masteredWords: Set<String> = emptySet(),
    var lastStudyDate: Date? = null,
    var totalScore: Int = 0
)

data class TestResult(
    val wordId: String,
    val isCorrect: Boolean,
    val userAnswer: String?,
    val correctAnswer: String?,
    var explanation: String? = null
)

// Room Entity - 数据库实体类
@Entity(tableName = "words")
data class WordEntity(
    @PrimaryKey val id: String,
    var word: String = "",
    var pronunciation: String = "",
    var partOfSpeech: String = "",
    var definition: String = "",
    var exampleSentenceEn: String = "",
    var exampleSentenceCn: String = "",
    var difficultyLevel: Int = 1
)

// JSON 数据类 - 用于从文件加载词库
data class WordJson(
    val words: List<Word>
)