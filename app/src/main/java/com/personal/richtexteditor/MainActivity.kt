package com.personal.richtexteditor

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.personal.richtexteditorsdk.old.RichTextEditor
import com.personal.richtexteditorsdk.RichTextEditorInterface

const val buttonActiveColor = Color.BLUE
const val buttonInactiveColor = Color.GRAY

class MainActivity : AppCompatActivity(), RichTextEditorInterface {

    private lateinit var richTextEditor: RichTextEditor
    private lateinit var boldButton: Button
    private lateinit var italicButton: Button
    private lateinit var strikethroughButton: Button

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        richTextEditor = findViewById(R.id.richTextEditor)
        boldButton = findViewById(R.id.boldButton)
        italicButton = findViewById(R.id.italicButton)
        strikethroughButton = findViewById(R.id.strikeThroughButton)

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
