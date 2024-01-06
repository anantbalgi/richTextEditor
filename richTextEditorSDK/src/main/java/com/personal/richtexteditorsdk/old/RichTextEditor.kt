package com.personal.richtexteditorsdk.old

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface
import android.text.Editable
import android.text.Spannable
import android.text.TextWatcher
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import android.util.AttributeSet
import android.widget.EditText
import com.personal.richtexteditorsdk.RichTextEditorInterface
import com.personal.richtexteditorsdk.components.MarkdownConverter

@SuppressLint("ViewConstructor", "AppCompatCustomView")
class RichTextEditor @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : EditText(context, attrs) {

    private var richTextEditorInterface: RichTextEditorInterface? = null

    private var isBoldActive = false
    private var isItalicActive = false
    private var isStrikeThroughActive = false

    private val textChangeListener = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            if (count != 0 && selectionStart != 0) {
                applyStyle(start, count)
            }
        }

        override fun afterTextChanged(s: Editable?) {}
    }

    init {

        if (context !is RichTextEditorInterface) {
            throw IllegalArgumentException("RichTextEditorInterface must be set programmatically using setRichTextEditorInterface")
        } else {
            richTextEditorInterface = context
        }

        setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                addTextChangedListener(textChangeListener)
            } else {
                removeTextChangedListener(textChangeListener)
            }
        }

        setOnClickListener {
            if (selectionStart != editableText.length && selectionStart != 0) {
                setPreviousIndexStyle(selectionStart)
            }
        }
    }

    fun toggleBoldStyleState() {
        isBoldActive = !isBoldActive
        richTextEditorInterface?.onStyleButtonStateChange(
            isBoldActive,
            isItalicActive,
            isStrikeThroughActive
        )
    }

    fun toggleItalicStyleState() {
        isItalicActive = !isItalicActive
        richTextEditorInterface?.onStyleButtonStateChange(
            isBoldActive,
            isItalicActive,
            isStrikeThroughActive
        )
    }

    fun toggleStrikeThroughStyleState() {
        isStrikeThroughActive = !isStrikeThroughActive
        richTextEditorInterface?.onStyleButtonStateChange(
            isBoldActive,
            isItalicActive,
            isStrikeThroughActive
        )
    }

    private fun setPreviousIndexStyle(start: Int) {
        isBoldActive = false
        isItalicActive = false
        isStrikeThroughActive = false

        val styleSpan = text?.getSpans(start, start + 1, StyleSpan::class.java)
        val strikethroughSpan = text?.getSpans(start, start + 1, StrikethroughSpan::class.java)

        if (!styleSpan.isNullOrEmpty()) {
            styleSpan.forEach {
                when (it.style) {
                    Typeface.BOLD -> isBoldActive = true
                    Typeface.ITALIC -> isItalicActive = true
                    else -> {}
                }
            }
        }

        if (!strikethroughSpan.isNullOrEmpty()) {
            isStrikeThroughActive = true
        }

        richTextEditorInterface?.onStyleButtonStateChange(
            isBoldActive,
            isItalicActive,
            isStrikeThroughActive
        )
    }

    private fun applyStyle(start: Int, count: Int) {
        if (isBoldActive) {
            text.setSpan(
                StyleSpan(Typeface.BOLD),
                start,
                start + count,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        if (isItalicActive) {
            text.setSpan(
                StyleSpan(Typeface.ITALIC),
                start,
                start + count,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        if (isStrikeThroughActive) {
            text.setSpan(
                StrikethroughSpan(),
                start,
                start + count,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
    }

    /**
     * This method is used to capture and map the styling of text to an IntArray of fixed size.
     */
    private fun createStyleArray(): IntArray {
        /**
         * Normal = 0
         * Bold = 1
         * Italic = 2
         * Bold_Italic = 3
         * StrikeThrough = 4
         * Bold_StrikeThrough = 5
         * Italic_StrikeThrough = 6
         * Bold_Italic_StrikeThrough = 7
         */

        val styleArray = IntArray(text.length)

        text.forEachIndexed { index, char ->

            if (char != ' ') {
                val styleSpan = text?.getSpans(index, index + 1, StyleSpan::class.java)
                val strikethroughSpan =
                    text?.getSpans(index, index + 1, StrikethroughSpan::class.java)

                if (!styleSpan.isNullOrEmpty()) {
                    if (styleSpan.size == 2) {
                        styleArray[index] = 3
                    } else {
                        styleSpan.forEach {
                            when (it.style) {
                                Typeface.BOLD -> styleArray[index] = 1
                                Typeface.ITALIC -> styleArray[index] = 2
                                else -> styleArray[index] = 0
                            }
                        }
                    }
                } else {
                    styleArray[index] = 0
                }

                if (!strikethroughSpan.isNullOrEmpty()) {
                    if (index in styleArray.indices) {
                        styleArray[index] = styleArray[index] + 4
                    } else {
                        styleArray[index] = 4
                    }
                }
            } else {
                styleArray[index] = 0
            }
        }

        return styleArray
    }

    fun getMarkdownText(): String {
        return MarkdownConverter(text.toString(), createStyleArray()).convertToMarkDown()
    }

}
