package com.personal.richtexteditorsdk.features

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface
import android.text.Editable
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import android.util.AttributeSet
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import com.personal.richtexteditorsdk.components.MarkdownConverter
import com.personal.richtexteditorsdk.interfaces.RichTextEditorListener
import com.personal.richtexteditorsdk.utils.Constants

/**
 * Custom EditText for rich text editing with support for bold, italic, and strikethrough styles.
 * It implements a TextWatcher to apply styles dynamically and supports markdown conversion.
 *
 * @property context The context in which the view operates.
 * @property attrs The attribute set associated with the view.
 */
@SuppressLint("ViewConstructor", "AppCompatCustomView")
class RichTextEditor @JvmOverloads constructor(
    private val context: Context,
    private val attrs: AttributeSet? = null,
) : EditText(context, attrs) {

    // Interface for communicating style changes to the parent
    private var richTextEditorInterface: RichTextEditorListener? = null

    // Tracking style states
    private var isBoldActive = false
    private var isItalicActive = false
    private var isStrikeThroughActive = false

    // TextWatcher to apply styles dynamically
    private val textChangeListener = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            if (count != 0 && selectionStart != 0) {
                applyStyle(start, count)
            }
        }

        override fun afterTextChanged(s: Editable?) {}
    }

    /**
     * Initialization block: Setting up listeners, click actions, and ActionMode callback.
     */
    init {

        // Ensuring the context implements the required interface
        if (context !is RichTextEditorListener) throw IllegalArgumentException(Constants.RICH_TEXT_EDITOR_CONTRACT_ERROR)
        else richTextEditorInterface = context

        // Adding or removing TextWatcher based on focus state
        setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                addTextChangedListener(textChangeListener)
            } else {
                removeTextChangedListener(textChangeListener)
            }
        }

        // Click listener to handle styling at the beginning of the text
        setOnClickListener {
            if (selectionStart == 0) {
                resetButtonStates()
                richTextEditorInterface?.onStyleButtonStateChange(
                    isBoldActive,
                    isItalicActive,
                    isStrikeThroughActive
                )
                return@setOnClickListener
            }
            setPreviousOrSelectedIndexStyle(selectionStart - 1)
        }

        // Custom ActionMode callback for styling during text selection
        customSelectionActionModeCallback = object : ActionMode.Callback {
            override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                setPreviousOrSelectedIndexStyle(selectionStart, selectionEnd)
                return true
            }

            override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean = false
            override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean = false
            override fun onDestroyActionMode(mode: ActionMode?) {}
        }
    }

    /**
     * Toggles the bold style state and notifies the parent about the style change.
     */
    fun toggleBoldStyleState() {
        isBoldActive = !isBoldActive
        richTextEditorInterface?.onStyleButtonStateChange(
            isBoldActive,
            isItalicActive,
            isStrikeThroughActive
        )
        removeAndApplyStyleForSelectedText()
    }

    /**
     * Toggles the italic style state and notifies the parent about the style change.
     */
    fun toggleItalicStyleState() {
        isItalicActive = !isItalicActive
        richTextEditorInterface?.onStyleButtonStateChange(
            isBoldActive,
            isItalicActive,
            isStrikeThroughActive
        )
        removeAndApplyStyleForSelectedText()
    }

    /**
     * Toggles the strikethrough style state and notifies the parent about the style change.
     */
    fun toggleStrikeThroughStyleState() {
        isStrikeThroughActive = !isStrikeThroughActive
        richTextEditorInterface?.onStyleButtonStateChange(
            isBoldActive,
            isItalicActive,
            isStrikeThroughActive
        )
        removeAndApplyStyleForSelectedText()
    }

    /**
     * Sets the style states based on the existing styling at the specified indices.
     */
    private fun setPreviousOrSelectedIndexStyle(start: Int, end: Int? = null) {
        resetButtonStates()

        val actualEnd = end ?: (start + 1)
        val styleSpan = text?.getSpans(start, actualEnd, StyleSpan::class.java)
        val strikethroughSpan = text?.getSpans(start, actualEnd, StrikethroughSpan::class.java)

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

    /**
     * Applies the selected styles to the specified range of text.
     */
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
     * Resets the button states for bold, italic, and strikethrough.
     */
    private fun resetButtonStates() {
        isBoldActive = false
        isItalicActive = false
        isStrikeThroughActive = false
    }

    /**
     * Creates an array representing the styling of each character in the text.
     * Used for mapping text styling to an integer array.
     */
    private fun createStyleArray(): IntArray {
        /**
         * Normal = 0,
         * Bold = 1,
         * Italic = 2,
         * Bold_Italic = 3,
         * StrikeThrough = 4,
         * Bold_StrikeThrough = 5,
         * Italic_StrikeThrough = 6,
         * Bold_Italic_StrikeThrough = 7,
         * \n = 20
         */

        val styleArray = IntArray(text.length)

        text.forEachIndexed { index, char ->

            if (char != ' ' && char != '\n') {
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
            }

            if (char == ' ') {
                styleArray[index] = 0
            }

            if (char == '\n') {
                styleArray[index] = 20
            }
        }

        return styleArray
    }

    /**
     * Converts the text and its styling to Markdown format.
     */
    fun getMarkdownText(): String {
        return MarkdownConverter().spanToMarkdown(text.toString(), createStyleArray())
    }

    /**
     * Converts Markdown text to a SpannableStringBuilder.
     */
    fun getSpanFromMarkDown(markdownText: String): SpannableStringBuilder {
        return MarkdownConverter().markdownToSpan(markdownText)
    }

    /**
     * Removes and reapplies styles for the selected text, ensuring consistency.
     */
    private fun removeAndApplyStyleForSelectedText() {
        val startSelection = selectionStart
        val endSelection = selectionEnd

        if (startSelection != endSelection) {
            val selectedTextSpannableStringBuilder =
                SpannableStringBuilder(text.substring(startSelection, endSelection))
            selectedTextSpannableStringBuilder.clearSpans()
            text.delete(startSelection, endSelection)
            text.replace(
                startSelection,
                startSelection,
                selectedTextSpannableStringBuilder.toString()
            )
            setSelection(startSelection, endSelection)
        }
    }
}