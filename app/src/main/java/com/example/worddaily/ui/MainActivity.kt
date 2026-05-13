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
    private lateinit var btnReviewWrong: Button
    private lateinit var tvDailyTip: TextView
    private lateinit var homeLayout: View
    private lateinit var fragmentContainer: FrameLayout

    // 学习状态变量
    private val allWords = mutableListOf<WordEntity>()
    private val dailyTaskWords = mutableListOf<WordEntity>()
    private var masteredWordIds = setOf<String>()
    private var wrongWordIds = setOf<String>()

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
            wrongWordIds = progress.wrongWordIds
                .split(",")
                .filter { it.isNotBlank() }
                .toSet()

            tvStreakCount.text = "🔥 连续学习 ${progress.totalStudyDays} 天\n" +
                    "已掌握 ${masteredWordIds.size} 个单词\n" +
                    "今日学习 ${progress.todayLearned} 个"

            // 如果有错题，显示错题本按钮
            btnReviewWrong.visibility = if (wrongWordIds.isNotEmpty()) View.VISIBLE else View.GONE
            btnReviewWrong.text = "📖 错题本 (${wrongWordIds.size}个)"
        }
    }

    private fun setupUI() {
        homeLayout = findViewById(R.id.homeLayout)
        fragmentContainer = findViewById(R.id.fragmentContainer)

        tvStreakCount = findViewById(R.id.tvStreakCount)
        tvStreakCount.text = "🔥 连续学习 0 天\n已掌握 0 个单词"

        btnStartLearning = findViewById(R.id.btnStartLearning)
        btnStartLearning.setOnClickListener {
            startDailyLearning()
        }

        btnReviewWrong = findViewById(R.id.btnReviewWrong)
        btnReviewWrong.setOnClickListener {
            startReviewSession()
        }
        btnReviewWrong.visibility = View.GONE // 默认隐藏，有错题时才显示

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

            showLearningScreen(ArrayList(dailyTaskWords))
        }
    }

    /**
     * 开始错题复习
     */
    private fun startReviewSession() {
        if (wrongWordIds.isEmpty()) {
            Toast.makeText(this, "没有错题，太棒了！", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            val wrongWords = repository.getWordsByIds(wrongWordIds)
            if (wrongWords.isEmpty()) {
                showError("未找到错题数据")
                return@launch
            }

            // 错题复习也传入全部单词作为干扰项来源
            val all = repository.getAllWords()
            showLearningScreen(ArrayList(wrongWords), ArrayList(all))
        }
    }

    private fun showLearningScreen(words: ArrayList<WordEntity>, allWordsList: ArrayList<WordEntity>? = null) {
        // 隐藏主界面，显示学习界面
        homeLayout.visibility = View.GONE
        fragmentContainer.visibility = View.VISIBLE

        val allForDistractors = allWordsList ?: ArrayList(allWords)
        val fragment = LearningFragment.newInstance(words, allForDistractors)
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    /**
     * 学习完成回调 - 由 LearningFragment 调用
     */
    fun onLearningComplete(correctIds: Set<String>, wrongIds: Set<String>) {
        lifecycleScope.launch {
            repository.saveLearningResult(correctIds, wrongIds)
            showHomeScreen()
        }
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
