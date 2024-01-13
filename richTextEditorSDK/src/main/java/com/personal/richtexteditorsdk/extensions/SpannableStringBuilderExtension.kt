package com.personal.richtexteditorsdk.extensions

import android.text.SpannableStringBuilder

fun SpannableStringBuilder.removeCharacterAtIndex(
    index: Int,
    subStringLength: Int
): SpannableStringBuilder {
    if (index in indices) {
        delete(index, index + subStringLength)
    }
    return this
}

fun SpannableStringBuilder.replaceFirst(
    target: String,
    replacement: String
): SpannableStringBuilder {
    val startIndex = this.indexOf(target)

    if (startIndex != -1) {
        val endIndex = startIndex + target.length
        this.replace(startIndex, endIndex, replacement)
    }
    return this
}