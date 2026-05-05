package com.example.worddaily.domain

import com.example.worddaily.data.local.WordEntity

class DailyTaskGenerator {

    /**
     * 生成每日学习任务（5-8 个单词）
     * 优先选择未掌握的单词，随机混合已掌握单词保持复习
     */
    fun generateDailyTask(
        allWords: List<WordEntity>,
        masteredWordIds: Set<Int>
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
}
