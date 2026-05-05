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

class LearningFragment : Fragment() {

    companion object {
        private const val ARG_WORDS = "words"

        fun newInstance(words: ArrayList<WordEntity>): LearningFragment {
            return LearningFragment().apply {
                arguments = Bundle().apply {
                    putParcelableArrayList(ARG_WORDS, words)
                }
            }
        }
    }

    private var currentWordIndex = 0
    private lateinit var words: List<WordEntity>

    // UI references
    private lateinit var tvWordText: TextView
    private lateinit var tvPronunciation: TextView
    private lateinit var tvPartOfSpeech: TextView
    private lateinit var tvDefinition: TextView
    private lateinit var tvExampleEn: TextView
    private lateinit var tvExampleCn: TextView
    private lateinit var tvProgressText: TextView
    private lateinit var btnNextWord: Button
    private lateinit var btnOptionA: Button
    private lateinit var btnOptionB: Button
    private lateinit var btnOptionC: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_learning, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        @Suppress("DEPRECATION")
        words = arguments?.getParcelableArrayList(ARG_WORDS) ?: emptyList()

        if (words.isEmpty()) {
            Toast.makeText(requireContext(), "没有单词数据", Toast.LENGTH_SHORT).show()
            return
        }

        // Bind UI
        tvWordText = view.findViewById(R.id.tvWordText)
        tvPronunciation = view.findViewById(R.id.tvPronunciation)
        tvPartOfSpeech = view.findViewById(R.id.tvPartOfSpeech)
        tvDefinition = view.findViewById(R.id.tvDefinition)
        tvExampleEn = view.findViewById(R.id.tvExampleEn)
        tvExampleCn = view.findViewById(R.id.tvExampleCn)
        tvProgressText = view.findViewById(R.id.tvProgressText)
        btnNextWord = view.findViewById(R.id.btnNextWord)
        btnOptionA = view.findViewById(R.id.btnOptionA)
        btnOptionB = view.findViewById(R.id.btnOptionB)
        btnOptionC = view.findViewById(R.id.btnOptionC)

        updateUI()

        btnOptionA.setOnClickListener { handleAnswer(true) }
        btnOptionB.setOnClickListener { handleAnswer(false) }
        btnOptionC.setOnClickListener { handleAnswer(false) }

        btnNextWord.setOnClickListener {
            if (currentWordIndex < words.size - 1) {
                currentWordIndex++
                updateUI()
            } else {
                showCompletionDialog()
            }
        }
    }

    private fun updateUI() {
        val word = words[currentWordIndex]
        tvWordText.text = word.wordText
        tvPronunciation.text = word.pronunciation
        tvPartOfSpeech.text = "词性：${word.partOfSpeech}"
        tvDefinition.text = word.definitionCn
        tvExampleEn.text = word.exampleEn
        tvExampleCn.text = word.exampleCn

        val percent = if (words.size > 1) currentWordIndex * 100 / (words.size - 1) else 100
        tvProgressText.text = "${currentWordIndex + 1}/${words.size} | ${percent}% 完成"
    }

    private fun handleAnswer(isCorrect: Boolean) {
        val message = if (isCorrect) "🎉 太棒了！答对了！" else "😅 再想想哦～"
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun showCompletionDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("🎉 学习完成！")
            .setMessage("今天学习了 ${words.size} 个单词，坚持就是胜利！\n\n你的词汇量正在悄悄增长哦~ 👏")
            .setPositiveButton("返回主页") { _, _ ->
                // Call MainActivity to show home screen
                (activity as? MainActivity)?.showHomeScreen()
            }
            .setCancelable(false)
            .show()
    }
}
