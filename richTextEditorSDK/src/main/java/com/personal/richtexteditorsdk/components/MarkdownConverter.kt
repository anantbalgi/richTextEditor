package com.personal.richtexteditorsdk.components

class MarkdownConverter(
    private val input: String,
    private val styles: IntArray
) {

    fun convertToMarkDown(): String {
        val result = StringBuilder()
        val stack = Stack<String>()
        var currentStyle: Int? = null

        input.forEachIndexed { index, char ->

            // Handles the opening markdown
            if (styles.getOrElse(index) { 0 } != currentStyle) {
                currentStyle = styles.getOrElse(index) { 0 }

                if (isBold(styles[index]) && !stack.contains("**")) {
                    stack.push("**")
                    result.append("**")
                }

                if (isItalic(styles[index]) && !stack.contains("*")) {
                    stack.push("*")
                    result.append("*")
                }

                if (isStrikeThrough(styles[index]) && !stack.contains("~~")) {
                    stack.push("~~")
                    result.append("~~")
                }
            }

            // Character inserting
            result.append(char)

            // Handles the closing markdown
            if (index == input.length - 1 || (currentStyle != styles.getOrElse(index + 1) { 0 })) {
                stack.forEachInReverse {
                    when (it) {
                        "**" -> {
                            if (index == input.length - 1 || !isBold(styles[index + 1])) {
                                stack.popElement("**")
                                result.append("**")
                            }
                        }

                        "*" -> {
                            if (index == input.length - 1 || !isItalic(styles[index + 1])) {
                                stack.popElement("*")
                                result.append("*")
                            }
                        }

                        "~~" -> {
                            if (stack.contains("~~")) {
                                stack.popElement("~~")
                                result.append("~~")
                            }
                        }
                    }
                }
                currentStyle = null
            }
        }

        return result.toString()
    }

    private fun isBold(styleIndex: Int): Boolean {
        return styleIndex in mutableListOf(1, 3, 5, 7)
    }

    private fun isItalic(styleIndex: Int): Boolean {
        return styleIndex in mutableListOf(2, 3, 6, 7)
    }

    private fun isStrikeThrough(styleIndex: Int): Boolean {
        return styleIndex in mutableListOf(4, 5, 6, 7)
    }

}