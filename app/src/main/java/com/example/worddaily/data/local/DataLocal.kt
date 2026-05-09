package com.example.worddaily.data.local

import android.os.Parcelable
import androidx.room.*
import kotlinx.parcelize.Parcelize

@Parcelize
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
) : Parcelable

@Entity(tableName = "user_progress")
data class UserProgressEntity(
    @PrimaryKey val userId: String,
    val totalStudyDays: Int = 0,
    var masteredWordIds: String = "", // JSON string of word IDs
    var lastStudyDate: String? = null, // ISO date format
    var totalScore: Int = 0
)

@Dao
interface WordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWord(word: WordEntity): Long
    
    @Update
    suspend fun updateWords(words: List<WordEntity>)
    
    @Query("SELECT * FROM words")
    suspend fun getAllWords(): List<WordEntity>
    
    @Query("SELECT * FROM words WHERE id IN (:wordIds)")
    suspend fun getWordsByIds(wordIds: List<String>): List<WordEntity>
    
    @Query("DELETE FROM words WHERE id IN (:wordIds)")
    suspend fun deleteWords(wordIds: List<String>)
}

@Dao
interface ProgressDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveProgress(progress: UserProgressEntity)
    
    @Update
    suspend fun updateProgress(progress: UserProgressEntity)
    
    @Query("SELECT * FROM user_progress WHERE userId = :userId")
    suspend fun getProgress(userId: String): UserProgressEntity?
}

@Database(entities = [WordEntity::class, UserProgressEntity::class], version = 3)
abstract class WordDailyDB : RoomDatabase() {
    abstract fun wordDao(): WordDao
    abstract fun progressDao(): ProgressDao
}
