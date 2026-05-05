package com.example.worddaily.data.model

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
