package com.wannaverse.wannacode.ide.editor.syntax

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import com.wannaverse.wannacode.ERROR_RED
import com.wannaverse.wannacode.ide.editor.CodeEditorViewModel

fun highlightCode(
    code: String,
    language: String = "java",
    diagnostics: List<CodeEditorViewModel.DiagnosticLineInfo> = emptyList()
): AnnotatedString {
    val builder = AnnotatedString.Builder()
    val syntax = syntaxMap[language] ?: return AnnotatedString(code)

    val lines = code.lines()
    var charIndex = 0

    lines.forEachIndexed { lineNumber, lineText ->
        // Apply syntax highlighting per line
        var index = 0
        while (index < lineText.length) {
            var matched = false
            for (rule in syntax.rules) {
                val match = rule.pattern.find(lineText, index)
                if (match != null && match.range.first == index) {
                    builder.withStyle(rule.style) { append(match.value) }
                    index += match.value.length
                    matched = true
                    break
                }
            }
            if (!matched) {
                builder.append(lineText[index])
                index++
            }
        }

        // Add a newline (except after the last line)
        if (lineNumber < lines.size - 1) builder.append('\n')

        // Check if this line has diagnostics
        val diagsOnLine = diagnostics.filter { it.diagnosticLine == lineNumber }

        // Track applied ranges as pairs of (start, end)
        val appliedRanges = mutableSetOf<Pair<Int, Int>>()

        diagsOnLine.forEach { diagnostic ->
            val line = diagnostic.diagnosticLine

            if (line == lineNumber) {
                val start = diagnostic.startChar
                val end = diagnostic.endChar

                val rangePair = start to end // track just the line-relative positions
                if (rangePair !in appliedRanges) {
                    builder.addStyle(
                        SpanStyle(color = ERROR_RED),
                        start = charIndex + start, // still add charIndex for styling
                        end = charIndex + end
                    )

                    builder.addStringAnnotation(
                        tag = "diagnostic",
                        annotation = "error",
                        start = charIndex + start,
                        end = charIndex + end
                    )

                    appliedRanges.add(rangePair)
                }
            }
        }

        charIndex += lineText.length + 1 // +1 for '\n'
    }

    return builder.toAnnotatedString()
}
