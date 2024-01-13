package com.personal.richtexteditor

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.personal.richtexteditorsdk.features.RichTextEditor
import com.personal.richtexteditorsdk.interfaces.RichTextEditorListener

const val buttonActiveColor = Color.BLUE
const val buttonInactiveColor = Color.GRAY

class MainActivity : AppCompatActivity(), RichTextEditorListener {

    private lateinit var richTextEditor: RichTextEditor
    private lateinit var boldButton: Button
    private lateinit var italicButton: Button
    private lateinit var strikethroughButton: Button
    private lateinit var markDownButton: Button
    private lateinit var markDownEditor: EditText
    private lateinit var styleSpanButton: Button
    private lateinit var styleSpanTextView: TextView

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initializeViews()
        setupListeners()
        setInitialStyleButtonState()
    }

    private fun initializeViews() {
        richTextEditor = findViewById(R.id.richTextEditor)
        boldButton = findViewById(R.id.boldButton)
        italicButton = findViewById(R.id.italicButton)
        strikethroughButton = findViewById(R.id.strikeThroughButton)
        markDownEditor = findViewById(R.id.markDownEditText)
        markDownButton = findViewById(R.id.markdownButton)
        styleSpanButton = findViewById(R.id.styleSpanButton)
        styleSpanTextView = findViewById(R.id.styleSpanTextView)
    }

    private fun setupListeners() {
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
            markDownEditor.setText(richTextEditor.getMarkdownText())
        }

        styleSpanButton.setOnClickListener {
            styleSpanTextView.text =
                richTextEditor.getSpanFromMarkDown(markDownEditor.text.toString())
        }
    }

    private fun setInitialStyleButtonState() {
        onStyleButtonStateChange(
            isBoldActive = false,
            isItalicActive = false,
            isStrikeThroughActive = false
        )
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
