package com.personal.richtexteditorsdk.features

//noinspection SuspiciousImport
import android.R
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
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.widget.EditText
import androidx.core.view.inputmethod.EditorInfoCompat
import androidx.core.view.inputmethod.InputConnectionCompat
import com.personal.richtexteditorsdk.components.MarkdownConverter
import com.personal.richtexteditorsdk.interfaces.CutCopyPasteListener
import com.personal.richtexteditorsdk.interfaces.MediaSelectionListener
import com.personal.richtexteditorsdk.interfaces.RichTextEditorInterface

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

    // Tracking style states
    private var isBoldActive = false
    private var isItalicActive = false
    private var isStrikeThroughActive = false

    // Interface for communicating style changes to the parent
    private var richTextEditorInterface: RichTextEditorInterface? = null

    // Listener for cut, copy, and paste actions
    private var cutCopyPasteListener: CutCopyPasteListener? = null

    // Listener for media selection
    private var mediaSelectionListener: MediaSelectionListener? = null

    // External TextWatcher provided by the exposed view user
    private var externalTextWatcher: TextWatcher? = null

    // Both internal and external TextWatcher to apply styles dynamically and different user specific use-cases
    private val internalTextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            externalTextWatcher?.beforeTextChanged(s, start, count, after)
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            externalTextWatcher?.onTextChanged(s, start, before, count)
            if (count != 0 && selectionStart != 0) {
                applyStyle(start, count)
            }
        }

        override fun afterTextChanged(s: Editable?) {
            externalTextWatcher?.afterTextChanged(s)
        }
    }

    /**
     * Initialization block: Setting up listeners, click actions, and ActionMode callback.
     */
    init {

        // Adding or removing TextWatcher based on focus state
        setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                addTextChangedListener(internalTextWatcher)
            } else {
                removeTextChangedListener(internalTextWatcher)
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


    override fun onCreateInputConnection(editorInfo: EditorInfo): InputConnection? {

        // Call the superclass method to obtain the default input connection
        val inputConnection = super.onCreateInputConnection(editorInfo)

        // Set the allowed content mime types to image/gif and image/png
        EditorInfoCompat.setContentMimeTypes(editorInfo, arrayOf("image/gif", "image/png"))

        // Define a callback for handling the commit of content (e.g., image insertion)
        val callback = InputConnectionCompat.OnCommitContentListener { inputContentInfo, flags, _ ->
            // Check if read URI permission is granted
            if (flags and InputConnectionCompat.INPUT_CONTENT_GRANT_READ_URI_PERMISSION != 0) {
                try {
                    // Request permission to read the content URI
                    inputContentInfo.requestPermission()
                } catch (e: Exception) {
                    // Handle permission request failure
                    return@OnCommitContentListener false
                }
                // Notify the listener about the selected media (contentUri)
                mediaSelectionListener?.onMediaSelected(inputContentInfo.contentUri)
            }
            true
        }

        // Wrap the existing input connection with a customized wrapper or return null
        return inputConnection?.let {
            InputConnectionCompat.createWrapper(it, editorInfo, callback)
        }

    }

    /**
     * Overrides the method to handle text context menu item selection.
     * Calls corresponding methods based on the selected menu item.
     *
     * @param id The ID of the selected menu item.
     * @return True if the event was consumed, false otherwise.
     */
    override fun onTextContextMenuItem(id: Int): Boolean {
        when (id) {
            R.id.cut -> onCut()
            R.id.copy -> onCopy()
            R.id.paste -> onPaste()
        }
        return super.onTextContextMenuItem(id)
    }

    /**
     * Invokes the `onCut()` method of the `cutCopyPasteListener` if it is not null.
     */
    private fun onCut() {
        cutCopyPasteListener?.onCut()
    }

    /**
     * Invokes the `onCopy()` method of the `cutCopyPasteListener` if it is not null.
     */
    private fun onCopy() {
        cutCopyPasteListener?.onCopy()
    }

    /**
     * Invokes the `onPaste()` method of the `cutCopyPasteListener` if it is not null.
     */
    private fun onPaste() {
        cutCopyPasteListener?.onPaste()
    }

    /**
     * Overrides the method to specify that the view has no autofill type.
     *
     * @return The autofill type, which is set to View.AUTOFILL_TYPE_NONE.
     */
    override fun getAutofillType(): Int {
        return View.AUTOFILL_TYPE_NONE
    }

    /**
     * Ensures that the provided interfaces are set for communication and handling specific actions.
     *
     * @param richTextEditorInterface The listener implementing the `RichTextEditorInterface` interface.
     */
    fun setRichTextEditorInterface(richTextEditorInterface: RichTextEditorInterface) {
        this.richTextEditorInterface = richTextEditorInterface
    }

    /**
     * Sets the `CutCopyPasteListener` for handling cut, copy, and paste actions.
     *
     * @param cutCopyPasteListener The listener implementing the `CutCopyPasteListener` interface.
     */
    fun setCutCopyPasteListener(cutCopyPasteListener: CutCopyPasteListener) {
        this.cutCopyPasteListener = cutCopyPasteListener
    }

    /**
     * Sets the `MediaSelectionListener` for handling media selection actions.
     *
     * @param mediaSelectionListener The listener implementing the `MediaSelectionListener` interface.
     */
    fun setMediaSelectionListener(mediaSelectionListener: MediaSelectionListener) {
        this.mediaSelectionListener = mediaSelectionListener
    }

    /**
     * Setter method for external TextWatcher provided by the exposed view user.
     *
     * @param textWatcher The external TextWatcher to be set.
     */
    fun setExternalTextWatcher(textWatcher: TextWatcher) {
        externalTextWatcher = textWatcher
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