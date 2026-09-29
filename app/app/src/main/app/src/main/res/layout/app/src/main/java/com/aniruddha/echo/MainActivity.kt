package com.aniruddha.echo

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import java.util.Locale

class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var textToSpeech: TextToSpeech

    private lateinit var statusText: TextView
    private lateinit var resultText: TextView
    private lateinit var micButton: Button

    private val requestCode = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        statusText = findViewById(R.id.statusText)
        resultText = findViewById(R.id.resultText)
        micButton = findViewById(R.id.micButton)

        textToSpeech = TextToSpeech(this, this)

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                requestCode
            )
        }

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)

        micButton.setOnClickListener {
            startListening()
        }
    }

    private fun startListening() {

        statusText.text = "Listening..."

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)

        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )

        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE,
            Locale.getDefault()
        )

        speechRecognizer.setRecognitionListener(
            object : android.speech.RecognitionListener {

                override fun onReadyForSpeech(params: Bundle?) {
                    statusText.text = "Listening..."
                }

                override fun onBeginningOfSpeech() {
                    statusText.text = "I'm listening..."
                }

                override fun onRmsChanged(rmsdB: Float) {}

                override fun onBufferReceived(buffer: ByteArray?) {}

                override fun onEndOfSpeech() {
                    statusText.text = "Processing..."
                }

                override fun onError(error: Int) {
                    statusText.text = "Try again"
                }

                override fun onResults(results: Bundle?) {

                    val matches =
                        results?.getStringArrayList(
                            SpeechRecognizer.RESULTS_RECOGNITION
                        )

                    if (!matches.isNullOrEmpty()) {

                        val userText = matches[0]

                        resultText.text = userText

                        respondToUser(userText)
                    }
                }

                override fun onPartialResults(
                    partialResults: Bundle?
                ) {}

                override fun onEvent(
                    eventType: Int,
                    params: Bundle?
                ) {}
            }
        )

        speechRecognizer.startListening(intent)
    }

    private fun respondToUser(text: String) {

        val command = text.lowercase(Locale.getDefault())

        val response = when {

            command.contains("hello") ||
            command.contains("hi") -> {
                "Hello! I'm ECHO. How can I help you?"
            }

            command.contains("your name") -> {
                "My name is ECHO, your voice assistant."
            }

            command.contains("time") -> {
                val time = java.text.SimpleDateFormat(
                    "hh:mm a",
                    Locale.getDefault()
                ).format(java.util.Date())

                "The time is $time."
            }

            command.contains("date") -> {
                val date = java.text.SimpleDateFormat(
                    "EEEE, dd MMMM yyyy",
                    Locale.getDefault()
                ).format(java.util.Date())

                "Today is $date."
            }

            command.contains("who are you") -> {
                "I am ECHO, your personal voice assistant."
            }

            else -> {
                "I heard you say: $text"
            }
        }

        resultText.text = response
        statusText.text = "ECHO"

        textToSpeech.speak(
            response,
            TextToSpeech.QUEUE_FLUSH,
            null,
            "ECHO_RESPONSE"
        )
    }

    override fun onInit(status: Int) {

        if (status == TextToSpeech.SUCCESS) {

            textToSpeech.language = Locale.US

            textToSpeech.setSpeechRate(0.95f)
        }
    }

    override fun onDestroy() {

        speechRecognizer.destroy()

        textToSpeech.stop()
        textToSpeech.shutdown()

        super.onDestroy()
    }
}
