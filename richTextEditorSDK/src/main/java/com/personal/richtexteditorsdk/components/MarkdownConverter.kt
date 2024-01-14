package com.personal.richtexteditorsdk.components

import android.graphics.Typeface
import android.text.SpannableStringBuilder
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import com.personal.richtexteditorsdk.datastructures.Stack
import com.personal.richtexteditorsdk.extensions.removeCharacterAtIndex
import com.personal.richtexteditorsdk.extensions.replaceFirst
import com.personal.richtexteditorsdk.utils.Constants

/**
 * Converts plain text with styling indicators to Markdown and vice versa for rich text editing.
 */
class MarkdownConverter {

    /**
     * Converts styled text to Markdown format based on the provided styling information.
     *
     * @param input The input text with styling indicators.
     * @param styles An array representing the styling of each character in the text.
     * @return The converted Markdown text.
     */
    fun spanToMarkdown(
        input: String,
        styles: IntArray
    ): String {
        val result = StringBuilder()
        val stack = Stack<String>()
        var currentStyle: Int? = null

        input.forEachIndexed { index, char ->
            // Handles the opening markdown
            if (styles.getOrElse(index) { 0 } != currentStyle) {
                currentStyle = styles.getOrElse(index) { 0 }

                if (isBold(styles[index]) && !stack.contains(Constants.BOLD)) {
                    stack.push(Constants.BOLD)
                    result.append(Constants.BOLD)
                }

                if (isItalic(styles[index]) && !stack.contains(Constants.ITALIC)) {
                    stack.push(Constants.ITALIC)
                    result.append(Constants.ITALIC)
                }

                if (isStrikeThrough(styles[index]) && !stack.contains(Constants.STRIKE_THROUGH)) {
                    stack.push(Constants.STRIKE_THROUGH)
                    result.append(Constants.STRIKE_THROUGH)
                }

                if (styles[index] == 10) {
                    result.append(Constants.NEW_LINE_CHAR)
                }
            }

            // Character inserting
            result.append(char)

            // Handles the closing markdown
            if (index == input.length - 1 || (currentStyle != styles.getOrElse(index + 1) { 0 })) {
                stack.forEachInReverse {
                    when (it) {
                        Constants.BOLD -> {
                            if (index == input.length - 1 || !isBold(styles[index + 1])) {
                                stack.popElement(Constants.BOLD)
                                result.append(Constants.BOLD)
                            }
                        }

                        Constants.ITALIC -> {
                            if (index == input.length - 1 || !isItalic(styles[index + 1])) {
                                stack.popElement(Constants.ITALIC)
                                result.append(Constants.ITALIC)
                            }
                        }

                        Constants.STRIKE_THROUGH -> {
                            if (stack.contains(Constants.STRIKE_THROUGH)) {
                                stack.popElement(Constants.STRIKE_THROUGH)
                                result.append(Constants.STRIKE_THROUGH)
                            }
                        }
                    }
                }
                currentStyle = null
            }
        }

        return result.toString()
    }

    /**
     * Checks if the given style index represents a bold style.
     *
     * @param styleIndex The index representing the style of the text.
     * @return True if the style is bold, false otherwise.
     */
    private fun isBold(styleIndex: Int): Boolean {
        return styleIndex in mutableListOf(1, 3, 5, 7)
    }

    /**
     * Checks if the given style index represents an italic style.
     *
     * @param styleIndex The index representing the style of the text.
     * @return True if the style is italic, false otherwise.
     */
    private fun isItalic(styleIndex: Int): Boolean {
        return styleIndex in mutableListOf(2, 3, 6, 7)
    }

    /**
     * Checks if the given style index represents a strikethrough style.
     *
     * @param styleIndex The index representing the style of the text.
     * @return True if the style is strikethrough, false otherwise.
     */
    private fun isStrikeThrough(styleIndex: Int): Boolean {
        return styleIndex in mutableListOf(4, 5, 6, 7)
    }


    /**
     * Converts Markdown text to a `SpannableStringBuilder` with applied styles.
     *
     * @param markdownText The input Markdown text.
     * @return The `SpannableStringBuilder` with applied styles.
     */
    fun markdownToSpan(markdownText: String): SpannableStringBuilder {
        var spannableStringBuilder = SpannableStringBuilder(markdownText)

        val stylingSymbols = setOf("**", "*", "~~")
        stylingSymbols.forEach { openingSymbol ->
            var openingIndex = spannableStringBuilder.indexOf(openingSymbol)
            while (openingIndex != -1) {
                spannableStringBuilder = spannableStringBuilder.replaceFirst(openingSymbol, "")
                val closingIndex = spannableStringBuilder.indexOf(openingSymbol)
                if (closingIndex != -1) {
                    spannableStringBuilder = spannableStringBuilder.removeCharacterAtIndex(
                        closingIndex,
                        openingSymbol.length
                    )
                    when (openingSymbol) {
                        Constants.BOLD -> {
                            spannableStringBuilder.setSpan(
                                StyleSpan(Typeface.BOLD),
                                openingIndex,
                                closingIndex,
                                0
                            )
                        }

                        Constants.ITALIC -> {
                            spannableStringBuilder.setSpan(
                                StyleSpan(Typeface.ITALIC),
                                openingIndex,
                                closingIndex,
                                0
                            )
                        }

                        Constants.STRIKE_THROUGH -> {
                            spannableStringBuilder.setSpan(
                                StrikethroughSpan(),
                                openingIndex,
                                closingIndex,
                                0
                            )
                        }
                    }

                    openingIndex = spannableStringBuilder.indexOf(openingSymbol)
                } else {
                    openingIndex = -1
                }
            }
        }

        return spannableStringBuilder
    }
}