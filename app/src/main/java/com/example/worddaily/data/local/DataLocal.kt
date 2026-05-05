package com.example.worddaily.data.local

import android.os.Parcelable
import androidx.room.*
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "words")
data class WordEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val wordText: String,
    var pronunciation: String = "",
    var partOfSpeech: String = "",
    var definitionCn: String = "",
    var exampleEn: String = "",
    var exampleCn: String = "",
    val difficultyLevel: Int = 1
) : Parcelable

@Entity(tableName = "user_progress")
data class UserProgressEntity(
    @PrimaryKey val userId: String,
    val totalStudyDays: Int = 0,
    var masteredWordIds: String = "",
    var lastStudyDate: String? = null,
    var totalScore: Int = 0
)

@Dao
interface WordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWord(word: WordEntity): Long

    @Query("SELECT * FROM words")
    suspend fun getAllWords(): List<WordEntity>

    @Query("SELECT * FROM words WHERE id IN (:wordIds)")
    suspend fun getWordsByIds(wordIds: List<Int>): List<WordEntity>
}

@Dao
interface ProgressDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveProgress(progress: UserProgressEntity)

    @Query("SELECT * FROM user_progress WHERE userId = :userId")
    suspend fun getProgress(userId: String): UserProgressEntity?
}

@Database(entities = [WordEntity::class, UserProgressEntity::class], version = 1)
abstract class WordDailyDB : RoomDatabase() {
    abstract fun wordDao(): WordDao
    abstract fun progressDao(): ProgressDao
}
