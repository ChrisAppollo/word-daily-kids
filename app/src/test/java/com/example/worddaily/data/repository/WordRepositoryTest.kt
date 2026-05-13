package com.example.worddaily.data.repository

import org.junit.Assert.*
import org.junit.Test
import com.example.worddaily.data.local.WordEntity

class WordRepositoryTest {

    private fun makeWord(id: String, word: String, def: String) = WordEntity(
        id = id, word = word, pronunciation = "/test/", partOfSpeech = "n.",
        definition = def, exampleSentenceEn = "Example", exampleSentenceCn = "例子", difficultyLevel = 1
    )

    @Test
    fun test_word_entity_creation() {
        val word = makeWord("apple", "apple", "苹果")
        assertEquals("apple", word.id)
        assertEquals("apple", word.word)
        assertEquals("苹果", word.definition)
        assertEquals("n.", word.partOfSpeech)
    }

    @Test
    fun test_word_data_completeness() {
        val word = makeWord("apple", "apple", "苹果")
        with(word) {
            assertTrue("应该有单词", word.isNotBlank())
            assertTrue("应该有音标", pronunciation.isNotBlank())
            assertTrue("应该有词性", partOfSpeech.isNotBlank())
            assertTrue("应该有释义", definition.isNotBlank())
            assertTrue("应该有英文例句", exampleSentenceEn.isNotBlank())
        }
    }

    @Test
    fun test_progress_entity_defaults() {
        val progress = com.example.worddaily.data.local.UserProgressEntity(
            userId = "test_user"
        )
        assertEquals("test_user", progress.userId)
        assertEquals(0, progress.totalStudyDays)
        assertEquals("", progress.masteredWordIds)
        assertEquals("", progress.wrongWordIds)
        assertNull(progress.lastStudyDate)
        assertEquals(0, progress.totalScore)
        assertEquals(0, progress.todayLearned)
    }
}
