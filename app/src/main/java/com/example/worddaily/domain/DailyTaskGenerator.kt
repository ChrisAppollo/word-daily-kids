package com.example.worddaily.domain

import android.content.Context
import androidx.room.TypeConverter
import com.example.worddaily.data.local.WordEntity
import com.example.worddaily.data.model.WordJson
import com.google.gson.Gson

class DailyTaskGenerator {
    
    private val gson = Gson()
    
    /**
     * 从 JSON 文件加载所有单词（小升初必背词库）
     */
    fun loadWordsFromJson(context: Context): List<WordEntity> {
        return try {
            // 读取 assets 目录下的 JSON 文件
            val jsonContent = context.assets.open("xiao_sheng_chu_words.json")
                .bufferedReader()
                .use { it.readText() }
            
            val wordJson: WordJson = gson.fromJson(jsonContent, WordJson::class.java)
            wordJson.words.map { word ->
                WordEntity(
                    id = "${word.id}",
                    word = word.word,
                    pronunciation = word.pronunciation,
                    partOfSpeech = word.partOfSpeech,
                    definition = word.definition,
                    exampleSentenceEn = word.exampleSentenceEn,
                    exampleSentenceCn = word.exampleSentenceCn,
                    difficultyLevel = word.difficultyLevel
                )
            }
        } catch (e: Exception) {
            // 如果加载失败，返回空列表或默认词库
            println("Error loading words from JSON: ${e.message}")
            emptyList()
        }
    }
    
    /**
     * 生成每日学习任务（5-8 个单词）
     * 优先选择未掌握的单词，随机混合已掌握单词保持复习
     */
    fun generateDailyTask(
        allWords: List<WordEntity>,
        masteredWordIds: Set<String>
    ): Pair<List<WordEntity>, Int> {
        val targetCount = (5..8).random()
        
        // 从未掌握的单词中随机选择
        val unmasteredWords = allWords.filter { it.id !in masteredWordIds }
        val newWords = unmasteredWords.shuffled().take(minOf(targetCount, unmasteredWords.size))
        
        // 如果需要更多单词，从已掌握的复习
        val remainingCount = targetCount - newWords.size
        if (remainingCount > 0 && masteredWordIds.isNotEmpty()) {
            val reviewedWords = allWords.filter { it.id in masteredWordIds }
                    .shuffled()
                    .take(remainingCount)
            return Pair(newWords + reviewedWords, targetCount)
        }
        
        // 如果全部是新词（新用户）
        return Pair(newWords.ifEmpty { allWords.take(targetCount) }, targetCount)
    }
    
    /**
     * 获取单词总数统计
     */
    fun getWordCount(allWords: List<WordEntity>): Int {
        return allWords.size
    }
}
