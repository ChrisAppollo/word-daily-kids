package com.example.worddaily.ui

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.example.worddaily.R
import com.example.worddaily.WordDailyApplication
import com.example.worddaily.data.local.WordEntity
import com.example.worddaily.data.repository.WordRepository
import com.example.worddaily.domain.DailyTaskGenerator

class MainActivity : AppCompatActivity() {

    private lateinit var repository: WordRepository
    private lateinit var taskGenerator: DailyTaskGenerator

    // UI 组件引用
    private lateinit var tvStreakCount: TextView
    private lateinit var btnStartLearning: Button
    private lateinit var tvDailyTip: TextView
    private lateinit var homeLayout: View
    private lateinit var fragmentContainer: FrameLayout

    // 学习状态变量
    private val allWords = mutableListOf<WordEntity>()
    private val dailyTaskWords = mutableListOf<WordEntity>()
    private var masteredWordIds = setOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 初始化依赖
        val app = application as WordDailyApplication
        repository = WordRepository(app.database.wordDao(), app.database.progressDao())
        taskGenerator = DailyTaskGenerator()

        // 设置 UI 组件
        setupUI()

        // 初始化数据库并加载进度
        lifecycleScope.launch {
            repository.initializeDatabase(context = this@MainActivity)
            loadUserProgress()
        }
    }

    private suspend fun loadUserProgress() {
        val progress = repository.getUserProgress()
        if (progress != null) {
            masteredWordIds = progress.masteredWordIds
                .split(",")
                .filter { it.isNotBlank() }
                .toSet()
            tvStreakCount.text = "🔥 连续学习 ${progress.totalStudyDays} 天\n完成 ${progress.totalScore} 个单词！"
        }
    }

    private fun setupUI() {
        homeLayout = findViewById(R.id.homeLayout)
        fragmentContainer = findViewById(R.id.fragmentContainer)

        tvStreakCount = findViewById(R.id.tvStreakCount)
        tvStreakCount.text = "🔥 连续学习 0 天\n完成 0 个单词！"

        btnStartLearning = findViewById(R.id.btnStartLearning)
        btnStartLearning.setOnClickListener {
            startDailyLearning()
        }

        tvDailyTip = findViewById(R.id.tvDailyTip)
        tvDailyTip.text = "💡 今日目标：学习 5-8 个新单词，坚持每天进步！"
    }

    private fun startDailyLearning() {
        lifecycleScope.launch {
            val words = repository.getAllWords()
            allWords.clear()
            allWords.addAll(words)

            if (allWords.isEmpty()) {
                showError("未找到单词数据，请检查数据库")
                return@launch
            }

            val (taskWords, count) = taskGenerator.generateDailyTask(allWords, masteredWordIds)
            dailyTaskWords.clear()
            dailyTaskWords.addAll(taskWords)

            showLearningScreen()
        }
    }

    private fun showLearningScreen() {
        // 隐藏主界面，显示学习界面
        homeLayout.visibility = View.GONE
        fragmentContainer.visibility = View.VISIBLE

        val fragment = LearningFragment.newInstance(ArrayList(dailyTaskWords))
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    fun showHomeScreen() {
        // 学习完成，返回主界面
        homeLayout.visibility = View.VISIBLE
        fragmentContainer.visibility = View.GONE

        // 刷新进度
        lifecycleScope.launch {
            loadUserProgress()
        }
    }

    private fun showError(message: String) {
        AlertDialog.Builder(this)
            .setTitle("⚠️ 错误")
            .setMessage(message)
            .setPositiveButton("确定", null)
            .show()
    }
}
