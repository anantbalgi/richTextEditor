package com.personal.richtexteditorsdk

interface RichTextEditorInterface {
    fun onStyleButtonStateChange(
        isBoldActive: Boolean,
        isItalicActive: Boolean,
        isStrikeThroughActive: Boolean
    )
}