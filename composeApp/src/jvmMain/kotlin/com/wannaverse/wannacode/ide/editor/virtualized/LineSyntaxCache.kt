package com.wannaverse.wannacode.ide.editor.virtualized

import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.withStyle
import com.wannaverse.wannacode.ERROR_RED
import com.wannaverse.wannacode.ide.editor.syntax.LanguageSyntax
import com.wannaverse.wannacode.ide.editor.viewmodel.DiagnosticLineInfo

class LineSyntaxCache(
    private val languageSyntax: LanguageSyntax?
) {
    private val cache = mutableStateMapOf<Int, CacheEntry>()

    private data class CacheEntry(
        val version: Int,
        val highlighted: AnnotatedString
    )

    fun getHighlightedLine(
        lineIndex: Int,
        lineContent: String,
        diagnosticsForLine: List<DiagnosticLineInfo>
    ): AnnotatedString {
        val version = lineContent.hashCode() + diagnosticsForLine.hashCode()

        cache[lineIndex]?.let { entry ->
            if (entry.version == version) {
                return entry.highlighted
            }
        }

        val highlighted = highlightSingleLine(lineContent, diagnosticsForLine)
        cache[lineIndex] = CacheEntry(version, highlighted)
        return highlighted
    }

    private fun highlightSingleLine(
        lineText: String,
        diagnostics: List<DiagnosticLineInfo>
    ): AnnotatedString {
        if (languageSyntax == null) {
            return AnnotatedString(lineText)
        }

        val builder = AnnotatedString.Builder()

        var index = 0
        while (index < lineText.length) {
            var matched = false
            for (rule in languageSyntax.rules) {
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

        val appliedRanges = mutableSetOf<Pair<Int, Int>>()

        diagnostics.forEach { diagnostic ->
            val start = diagnostic.startChar.coerceIn(0, lineText.length)
            val end = diagnostic.endChar.coerceIn(0, lineText.length)
            if (start < end) {
                val rangePair = start to end
                if (rangePair !in appliedRanges) {
                    builder.addStyle(
                        SpanStyle(color = ERROR_RED),
                        start = start,
                        end = end
                    )
                    builder.addStringAnnotation(
                        tag = "diagnostic",
                        annotation = "error",
                        start = start,
                        end = end
                    )
                    appliedRanges.add(rangePair)
                }
            }
        }

        return builder.toAnnotatedString()
    }

    fun invalidateLine(lineIndex: Int) {
        cache.remove(lineIndex)
    }
}
