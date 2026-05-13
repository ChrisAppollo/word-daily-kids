package com.example.worddaily.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.worddaily.R
import com.example.worddaily.data.local.WordEntity
import com.example.worddaily.util.TtsManager

class LearningFragment : Fragment() {

    companion object {
        private const val ARG_WORDS = "words"
        private const val ARG_ALL_WORDS = "all_words"

        fun newInstance(words: ArrayList<WordEntity>, allWords: ArrayList<WordEntity>): LearningFragment {
            return LearningFragment().apply {
                arguments = Bundle().apply {
                    putParcelableArrayList(ARG_WORDS, words)
                    putParcelableArrayList(ARG_ALL_WORDS, allWords)
                }
            }
        }
    }

    private var currentWordIndex = 0
    private lateinit var words: List<WordEntity>
    private lateinit var allWords: List<WordEntity>
    private var ttsManager: TtsManager? = null

    private val correctWordIds = mutableSetOf<String>()
    private val wrongWordIds = mutableSetOf<String>()

    private var currentOptions = listOf<String>()
    private var currentCorrectIndex = 0
    private var answered = false

    private lateinit var tvWordText: TextView
    private lateinit var tvPronunciation: TextView
    private lateinit var tvPartOfSpeech: TextView
    private lateinit var tvDefinition: TextView
    private lateinit var tvExampleEn: TextView
    private lateinit var tvExampleCn: TextView
    private lateinit var tvProgressText: TextView
    private lateinit var tvQuizHint: TextView
    private lateinit var btnNextWord: Button
    private lateinit var btnOptionA: Button
    private lateinit var btnOptionB: Button
    private lateinit var btnOptionC: Button
    private lateinit var btnSpeakWord: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_learning, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ttsManager = TtsManager(requireContext())

        @Suppress("DEPRECATION")
        words = arguments?.getParcelableArrayList(ARG_WORDS) ?: emptyList()
        @Suppress("DEPRECATION")
        allWords = arguments?.getParcelableArrayList(ARG_ALL_WORDS) ?: emptyList()

        if (words.isEmpty()) {
            Toast.makeText(requireContext(), "没有单词数据", Toast.LENGTH_SHORT).show()
            return
        }

        tvWordText = view.findViewById(R.id.tvWordText)
        tvPronunciation = view.findViewById(R.id.tvPronunciation)
        tvPartOfSpeech = view.findViewById(R.id.tvPartOfSpeech)
        tvDefinition = view.findViewById(R.id.tvDefinition)
        tvExampleEn = view.findViewById(R.id.tvExampleEn)
        tvExampleCn = view.findViewById(R.id.tvExampleCn)
        tvProgressText = view.findViewById(R.id.tvProgressText)
        tvQuizHint = view.findViewById(R.id.tvQuizHint)
        btnNextWord = view.findViewById(R.id.btnNextWord)
        btnOptionA = view.findViewById(R.id.btnOptionA)
        btnOptionB = view.findViewById(R.id.btnOptionB)
        btnOptionC = view.findViewById(R.id.btnOptionC)
        btnSpeakWord = view.findViewById(R.id.btnSpeakWord)

        updateUI()
        generateOptions()

        btnOptionA.setOnClickListener { handleAnswer(0) }
        btnOptionB.setOnClickListener { handleAnswer(1) }
        btnOptionC.setOnClickListener { handleAnswer(2) }

        btnNextWord.setOnClickListener {
            if (currentWordIndex < words.size - 1) {
                currentWordIndex++
                answered = false
                updateUI()
                generateOptions()
            } else {
                showCompletionDialog()
            }
        }

        btnSpeakWord.setOnClickListener {
            val word = words[currentWordIndex]
            ttsManager?.speak(word.word)
        }
    }

    private fun generateOptions() {
        val currentWord = words[currentWordIndex]
        val correctDef = currentWord.definition

        val distractors = allWords
            .filter { it.id != currentWord.id && it.definition != correctDef }
            .shuffled()
            .take(2)
            .map { it.definition }

        val allOptions = mutableListOf(correctDef)
        allOptions.addAll(distractors)

        while (allOptions.size < 3) {
            allOptions.add("（无）")
        }

        val shuffled = allOptions.mapIndexed { i, v -> Pair(i, v) }.shuffled()
        currentOptions = shuffled.map { it.second }
        currentCorrectIndex = currentOptions.indexOf(correctDef)

        btnOptionA.text = "A. ${currentOptions[0]}"
        btnOptionB.text = "B. ${currentOptions[1]}"
        btnOptionC.text = "C. ${currentOptions[2]}"

        resetButtonColors()
    }

    private fun resetButtonColors() {
        btnOptionA.setBackgroundColor(0xFF4CAF50.toInt())
        btnOptionB.setBackgroundColor(0xFFFF9800.toInt())
        btnOptionC.setBackgroundColor(0xFFFF5722.toInt())
        btnOptionA.isEnabled = true
        btnOptionB.isEnabled = true
        btnOptionC.isEnabled = true
    }

    private fun updateUI() {
        val word = words[currentWordIndex]
        tvWordText.text = word.word
        tvPronunciation.text = word.pronunciation
        tvPartOfSpeech.text = "词性：${word.partOfSpeech}"
        
        // 答题前隐藏释义
        tvDefinition.text = "❓ 答对后显示释义"
        tvDefinition.setTextColor(0xFF999999.toInt())
        
        // 显示英文例句
        if (word.exampleSentenceEn.isNotBlank()) {
            tvExampleEn.text = "📝 ${word.exampleSentenceEn}"
            tvExampleEn.visibility = View.VISIBLE
        } else {
            tvExampleEn.visibility = View.GONE
        }
        
        // 隐藏中文翻译（答后显示）
        tvExampleCn.visibility = View.GONE

        val percent = if (words.size > 1) currentWordIndex * 100 / (words.size - 1) else 100
        tvProgressText.text = "${currentWordIndex + 1}/${words.size} | ${percent}% 完成"
        tvQuizHint.text = "🤔 这个单词的中文意思是什么？"

        ttsManager?.stop()
    }

    private fun handleAnswer(selectedIndex: Int) {
        if (answered) return
        answered = true

        val currentWord = words[currentWordIndex]
        val isCorrect = selectedIndex == currentCorrectIndex

        // 显示正确释义
        tvDefinition.text = currentWord.definition
        tvDefinition.setTextColor(0xFFFF0000.toInt())
        
        // 显示例句中文翻译
        if (currentWord.exampleSentenceCn.isNotBlank()) {
            tvExampleCn.text = "📖 ${currentWord.exampleSentenceCn}"
            tvExampleCn.visibility = View.VISIBLE
        }

        if (isCorrect) {
            correctWordIds.add(currentWord.id)
            highlightButton(selectedIndex, 0xFF2E7D32.toInt())
            tvQuizHint.text = "🎉 太棒了！答对了！"
            Toast.makeText(requireContext(), "🎉 正确！", Toast.LENGTH_SHORT).show()
        } else {
            wrongWordIds.add(currentWord.id)
            highlightButton(selectedIndex, 0xFFC62828.toInt())
            highlightButton(currentCorrectIndex, 0xFF2E7D32.toInt())
            tvQuizHint.text = "😅 正确答案已标绿，继续加油！"
            Toast.makeText(requireContext(), "😅 答案是：${currentOptions[currentCorrectIndex]}", Toast.LENGTH_LONG).show()
        }

        btnOptionA.isEnabled = false
        btnOptionB.isEnabled = false
        btnOptionC.isEnabled = false
    }

    private fun highlightButton(index: Int, color: Int) {
        when (index) {
            0 -> btnOptionA.setBackgroundColor(color)
            1 -> btnOptionB.setBackgroundColor(color)
            2 -> btnOptionC.setBackgroundColor(color)
        }
    }

    private fun showCompletionDialog() {
        val total = words.size
        val correct = correctWordIds.size
        val wrong = wrongWordIds.size

        val message = buildString {
            appendLine("📊 本次学习统计：")
            appendLine("  总共学习：${total} 个单词")
            appendLine("  答对：${correct} 个 ✅")
            appendLine("  答错：${wrong} 个 ❌")
            appendLine()
            if (wrong > 0) {
                appendLine("答错的单词已加入错题本，可以随时复习哦～")
            } else {
                appendLine("全部答对！太厉害了！👏")
            }
        }

        AlertDialog.Builder(requireContext())
            .setTitle("🎉 学习完成！")
            .setMessage(message)
            .setPositiveButton("返回主页") { _, _ ->
                (activity as? MainActivity)?.onLearningComplete(correctWordIds, wrongWordIds)
            }
            .setCancelable(false)
            .show()
    }

    override fun onPause() {
        super.onPause()
        ttsManager?.stop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        ttsManager?.shutdown()
        ttsManager = null
    }
}
