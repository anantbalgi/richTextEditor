package com.personal.richtexteditor

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.personal.richtexteditorsdk.RichTextEditorInterface
import com.personal.richtexteditorsdk.old.RichTextEditor

const val buttonActiveColor = Color.BLUE
const val buttonInactiveColor = Color.GRAY

class MainActivity : AppCompatActivity(), RichTextEditorInterface {

    private lateinit var richTextEditor: RichTextEditor
    private lateinit var boldButton: Button
    private lateinit var italicButton: Button
    private lateinit var strikethroughButton: Button
    private lateinit var markDownButton: Button
    private lateinit var markDownTextView: TextView


    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        richTextEditor = findViewById(R.id.richTextEditor)
        boldButton = findViewById(R.id.boldButton)
        italicButton = findViewById(R.id.italicButton)
        strikethroughButton = findViewById(R.id.strikeThroughButton)
        markDownTextView = findViewById(R.id.markDownTextView)
        markDownButton = findViewById(R.id.markdownButton)

        onStyleButtonStateChange(
            isBoldActive = false,
            isItalicActive = false,
            isStrikeThroughActive = false
        )

        boldButton.setOnClickListener {
            richTextEditor.toggleBoldStyleState()
        }
        italicButton.setOnClickListener {
            richTextEditor.toggleItalicStyleState()
        }
        strikethroughButton.setOnClickListener {
            richTextEditor.toggleStrikeThroughStyleState()
        }

        markDownButton.setOnClickListener {
            markDownTextView.text = richTextEditor.getMarkdownText()
        }
    }

    override fun onStyleButtonStateChange(
        isBoldActive: Boolean,
        isItalicActive: Boolean,
        isStrikeThroughActive: Boolean
    ) {
        boldButton.background.setTint(if (isBoldActive) buttonActiveColor else buttonInactiveColor)
        italicButton.background.setTint(if (isItalicActive) buttonActiveColor else buttonInactiveColor)
        strikethroughButton.background.setTint(if (isStrikeThroughActive) buttonActiveColor else buttonInactiveColor)
    }
}
