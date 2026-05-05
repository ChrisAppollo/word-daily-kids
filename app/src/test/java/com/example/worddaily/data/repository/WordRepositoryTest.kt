package com.example.worddaily.data.repository

import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import com.example.worddaily.data.local.WordEntity

class WordRepositoryTest {
    
    @Test
    fun test_sample_words_loaded() {
        val words = WordRepository.getSampleWords()
        assertNotNull("示例词库不应为空", words)
        assertEquals("应该有 20 个基础词汇", 20, words.size)
        assertTrue("第一个单词应该是 apple", words[0].wordText == "apple")
        assertTrue("最后一个单词应该是 teacher", words[19].wordText == "teacher")
    }

    @Test
    fun test_word_data_completeness() {
        val word = WordRepository.getSampleWords()[0]
        with(word) {
            assertEquals("apple", wordText)
            assertEquals("/\\u0259pl/", pronunciation)
            assertEquals("n.", partOfSpeech)
            assertEquals("苹果", definitionCn)
            assertTrue("应该有英文例句")
            assertTrue("应该有中文翻译")
        }
    }

    @Test
    fun test_daily_task_generation() {
        val words = WordRepository.getSampleWords()
        val masteredIds = setOf<String>() // 新用户无掌握词汇
        
        val result = DailyTaskGenerator.generateDailyTask(words, masteredIds)
        val (taskWords, count) = result
        
        assertTrue("生成的任务单词数应在 5-8 之间", count in 5..8)
        assertEquals("任务单词列表不应为空", true, taskWords.isNotEmpty())
    }
}
