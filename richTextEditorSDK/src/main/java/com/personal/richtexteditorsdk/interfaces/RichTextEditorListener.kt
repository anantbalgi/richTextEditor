package com.personal.richtexteditorsdk.interfaces

/**
 * Listener interface for observing style button state changes in a Rich Text Editor.
 * Implement this interface to receive callbacks when the state of style buttons (e.g., Bold, Italic,
 * StrikeThrough) changes in a Rich Text Editor.
 */
interface RichTextEditorListener {

    /**
     * Called when the state of style buttons (Bold, Italic, StrikeThrough) changes in the Rich Text Editor.
     *
     * @param isBoldActive Whether the Bold style is currently active.
     * @param isItalicActive Whether the Italic style is currently active.
     * @param isStrikeThroughActive Whether the StrikeThrough style is currently active.
     */
    fun onStyleButtonStateChange(
        isBoldActive: Boolean,
        isItalicActive: Boolean,
        isStrikeThroughActive: Boolean
    )
}