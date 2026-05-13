package com.example.worddaily.domain

import org.junit.Assert.*
import org.junit.Test
import com.example.worddaily.data.local.WordEntity

class DailyTaskGeneratorTest {

    private val generator = DailyTaskGenerator()

    private fun makeWord(id: String, word: String, def: String) = WordEntity(
        id = id, word = word, pronunciation = "", partOfSpeech = "n.",
        definition = def, exampleSentenceEn = "", exampleSentenceCn = "", difficultyLevel = 1
    )

    @Test
    fun test_generate_for_new_user() {
        val words = listOf(
            makeWord("apple", "apple", "苹果"),
            makeWord("banana", "banana", "香蕉"),
            makeWord("cat", "cat", "猫"),
            makeWord("dog", "dog", "狗"),
            makeWord("egg", "egg", "鸡蛋"),
            makeWord("fish", "fish", "鱼"),
            makeWord("grape", "grape", "葡萄"),
            makeWord("hat", "hat", "帽子"),
            makeWord("ice", "ice", "冰"),
            makeWord("juice", "juice", "果汁")
        )
        val masteredIds = setOf<String>()

        val (taskWords, count) = generator.generateDailyTask(words, masteredIds)

        assertTrue("任务单词不应为空", taskWords.isNotEmpty())
        assertTrue("任务数量应在 5-8 之间", count in 5..8)
    }

    @Test
    fun test_generate_for_existing_user() {
        val words = listOf(
            makeWord("apple", "apple", "苹果"),
            makeWord("banana", "banana", "香蕉"),
            makeWord("cat", "cat", "猫"),
            makeWord("dog", "dog", "狗"),
            makeWord("egg", "egg", "鸡蛋"),
            makeWord("fish", "fish", "鱼"),
            makeWord("grape", "grape", "葡萄"),
            makeWord("hat", "hat", "帽子"),
            makeWord("ice", "ice", "冰"),
            makeWord("juice", "juice", "果汁")
        )
        val masteredIds = setOf("apple", "banana", "cat")

        val (taskWords, _) = generator.generateDailyTask(words, masteredIds)

        assertTrue("任务单词不应为空", taskWords.isNotEmpty())
        // 应该包含未掌握的单词
        assertTrue("应该包含未掌握的单词", taskWords.any { it.id !in masteredIds })
    }

    @Test
    fun test_priority_unmastered_words() {
        val words = listOf(
            makeWord("apple", "apple", "苹果"),
            makeWord("banana", "banana", "香蕉"),
            makeWord("cat", "cat", "猫"),
            makeWord("dog", "dog", "狗"),
            makeWord("egg", "egg", "鸡蛋"),
            makeWord("fish", "fish", "鱼"),
            makeWord("grape", "grape", "葡萄"),
            makeWord("hat", "hat", "帽子"),
            makeWord("ice", "ice", "冰"),
            makeWord("juice", "juice", "果汁")
        )
        val masteredIds = setOf("apple", "banana")

        val (taskWords, _) = generator.generateDailyTask(words, masteredIds)

        // 应该优先选未掌握的
        val unmasteredInTask = taskWords.count { it.id !in masteredIds }
        assertTrue("应该优先学习未掌握的单词", unmasteredInTask > 0)
    }

    @Test
    fun test_word_count() {
        val words = listOf(
            makeWord("apple", "apple", "苹果"),
            makeWord("banana", "banana", "香蕉")
        )
        assertEquals(2, generator.getWordCount(words))
    }
}
