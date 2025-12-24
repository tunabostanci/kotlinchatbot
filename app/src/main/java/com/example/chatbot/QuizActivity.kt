package com.example.chatbot

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.chatbot.data.Question

class QuizActivity : AppCompatActivity() {

    private lateinit var questionTextView: TextView
    private lateinit var option1Button: Button
    private lateinit var option2Button: Button
    private lateinit var option3Button: Button
    private lateinit var option4Button: Button
    private lateinit var scoreTextView: TextView

    private val questions = listOf(
        Question("Which one is a fruit?", listOf("Carrot", "Apple", "Broccoli", "Potato"), 1),
        Question("What is the capital of Turkey?", listOf("Ankara", "Istanbul", "Izmir", "Bursa"), 0),
        Question("Which number comes after 5?", listOf("Three", "Four", "Six", "Seven"), 2)
    )

    private var currentQuestionIndex = 0
    private var score = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz)

        questionTextView = findViewById(R.id.questionTextView)
        option1Button = findViewById(R.id.option1Button)
        option2Button = findViewById(R.id.option2Button)
        option3Button = findViewById(R.id.option3Button)
        option4Button = findViewById(R.id.option4Button)
        scoreTextView = findViewById(R.id.scoreTextView)

        showQuestion()

        option1Button.setOnClickListener { checkAnswer(0) }
        option2Button.setOnClickListener { checkAnswer(1) }
        option3Button.setOnClickListener { checkAnswer(2) }
        option4Button.setOnClickListener { checkAnswer(3) }
    }

    private fun showQuestion() {
        if (currentQuestionIndex < questions.size) {
            val question = questions[currentQuestionIndex]
            questionTextView.text = question.text
            option1Button.text = question.options[0]
            option2Button.text = question.options[1]
            option3Button.text = question.options[2]
            option4Button.text = question.options[3]
        } else {
            // Quiz is over
            Toast.makeText(this, "Quiz Over! Your score: $score", Toast.LENGTH_SHORT).show()
            finish() // Go back to the previous activity
        }
    }

    private fun checkAnswer(selectedAnswerIndex: Int) {
        val question = questions[currentQuestionIndex]
        if (selectedAnswerIndex == question.correctAnswerIndex) {
            score += 10 // Increase score
            Toast.makeText(this, "Correct!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Wrong!", Toast.LENGTH_SHORT).show()
        }
        scoreTextView.text = "Score: $score"
        currentQuestionIndex++
        showQuestion()
    }
}
