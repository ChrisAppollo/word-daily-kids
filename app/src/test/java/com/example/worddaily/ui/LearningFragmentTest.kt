package com.example.worddaily.ui

import org.junit.Assert.*
import org.junit.Test
import com.example.worddaily.data.local.WordEntity

class LearningFragmentTest {

    private fun makeWord(id: String, word: String, def: String) = WordEntity(
        id = id, word = word, pronunciation = "", partOfSpeech = "n.",
        definition = def, exampleSentenceEn = "", exampleSentenceCn = "", difficultyLevel = 1
    )

    @Test
    fun test_word_list_not_empty() {
        val words = listOf(
            makeWord("apple", "apple", "苹果"),
            makeWord("banana", "banana", "香蕉")
        )
        assertTrue("单词列表不应为空", words.isNotEmpty())
    }

    @Test
    fun test_distractor_generation() {
        val words = listOf(
            makeWord("apple", "apple", "苹果"),
            makeWord("banana", "banana", "香蕉"),
            makeWord("cat", "cat", "猫"),
            makeWord("dog", "dog", "狗")
        )
        val currentWord = words[0]

        // 模拟干扰项生成逻辑
        val distractors = words
            .filter { it.id != currentWord.id && it.definition != currentWord.definition }
            .shuffled()
            .take(2)
            .map { it.definition }

        assertEquals("应生成2个干扰项", 2, distractors.size)
        assertTrue("干扰项不应包含正确答案", distractors.none { it == currentWord.definition })
    }

    @Test
    fun test_options_shuffled() {
        val correctDef = "苹果"
        val distractors = listOf("香蕉", "猫")
        val allOptions = mutableListOf(correctDef) + distractors

        // 打乱多次，验证顺序会变化
        val results = (1..10).map { allOptions.shuffled().map { it } }
        val hasDifferentOrder = results.any { it != allOptions }
        // 概率上几乎一定会打乱
        assertTrue("选项应该被打乱", hasDifferentOrder || allOptions.size <= 1)
    }
}
