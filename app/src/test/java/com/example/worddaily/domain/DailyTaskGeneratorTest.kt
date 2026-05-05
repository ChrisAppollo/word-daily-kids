package com.example.worddaily.domain

import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import com.example.worddaily.data.local.WordEntity

class DailyTaskGeneratorTest {
    
    @Test
    fun test_generate_for_new_user() {
        val words = listOf(
            WordEntity(1, "apple", "n.", "苹果"),
            WordEntity(2, "banana", "n.", "香蕉"),
            WordEntity(3, "cat", "n.", "猫")
        )
        val masteredIds = setOf<String>()

        val result = DailyTaskGenerator.generateDailyTask(words, masteredIds)
        val (taskWords, count) = result

        // 新用户应该学习全部可用单词（不超过 5-8）
        assertTrue("任务单词不应为空", taskWords.isNotEmpty())
        assertTrue("任务数量应在合理范围", count in 1..3)
    }

    @Test
    fun test_generate_for_existing_user() {
        val words = listOf(
            WordEntity(1, "apple", "n.", "苹果"),
            WordEntity(2, "banana", "n.", "香蕉"),
            WordEntity(3, "cat", "n.", "猫")
        )
        val masteredIds = setOf("1") // 已掌握 apple

        val result = DailyTaskGenerator.generateDailyTask(words, masteredIds)
        val (taskWords, count) = result

        assertTrue("任务单词不应为空", taskWords.isNotEmpty())
        assertFalse("应该包含未掌握的单词", taskWords.any { it.id == 1 })
    }

    @Test
    fun test_priority_unmastered_words() {
        val words = listOf(
            WordEntity(1, "apple", "n.", "苹果"),
            WordEntity(2, "banana", "n.", "香蕉")
        )
        val masteredIds = setOf("1") // 已掌握 apple

        val result = DailyTaskGenerator.generateDailyTask(words, masteredIds)
        val (taskWords, _) = result

        assertTrue("应该优先学习未掌握的单词", taskWords.any { it.id == 2 })
    }
}
