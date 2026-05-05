package com.example.worddaily

import android.app.Application
import com.example.worddaily.data.local.WordDailyDB
import com.example.worddaily.data.repository.WordRepository
import androidx.room.Room

/**
 * WordDaily Kids - Application entry point
 * Initializes database and provides global context
 */
class WordDailyApplication : Application() {
    companion object {
        lateinit var instance: WordDailyApplication
            private set
    }

    lateinit var database: WordDailyDB
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        // Initialize database
        database = Room.databaseBuilder(
            applicationContext,
            WordDailyDB::class.java,
            "worddaily_db"
        ).build()
    }
}
