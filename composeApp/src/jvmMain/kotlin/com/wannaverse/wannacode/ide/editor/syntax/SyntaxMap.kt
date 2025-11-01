package com.wannaverse.wannacode.ide.editor.syntax

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight

val syntaxMap: Map<String, LanguageSyntax> = mapOf(
    "kotlin" to LanguageSyntax(
        rules = listOf(
            SyntaxRule(
                pattern = Regex("""\b(package|import|private|class|fun|val|var)\b"""),
                style = SpanStyle(color = Color(0xFFFF9500))
            ),
            SyntaxRule(
                pattern = Regex("""\b\d+\b"""),
                style = SpanStyle(color = Color(0xFF3C12F9))
            ),
            SyntaxRule(
                pattern = Regex("""error"""),
                style = SpanStyle(color = Color.Red, fontWeight = FontWeight.Bold)
            ),
            SyntaxRule(
                pattern = Regex("""".*?""""),
                style = SpanStyle(color = Color(0xFF00FF00)) // strings
            )
        )
    ),
    "java" to LanguageSyntax(
        rules = listOf(
            // Keywords (orange – matches IntelliJ's keyword color)
            SyntaxRule(
                pattern = Regex("""\b(abstract|assert|boolean|break|byte|case|catch|char|class|const|continue|default|do|double|else|enum|extends|false|final|finally|float|for|goto|if|implements|import|instanceof|int|interface|long|native|new|null|package|private|protected|public|return|short|static|strictfp|super|switch|synchronized|this|throw|throws|transient|true|try|void|volatile|while)\b"""),
                style = SpanStyle(color = Color(0xFFFF9500))
            ),

            // Literals – numbers (blue)
            SyntaxRule(
                pattern = Regex("""\b\d+(\.\d+)?([eE][+-]?\d+)?[fFdD]?\b"""),
                style = SpanStyle(color = Color(0xFF9F3EDB))
            ),

            // String literals (green)
            SyntaxRule(
                pattern = Regex("""("([^"\\]|\\.)*")|('([^'\\]|\\.)*')"""),
                style = SpanStyle(color = Color(0xFF2B9E00))
            ),

            // Character literals – treated same as strings in IntelliJ
            SyntaxRule(
                pattern = Regex("""'(\\?.)'"""),
                style = SpanStyle(color = Color(0xFF2B9E00))
            ),

            // Annotations (purple)
            SyntaxRule(
                pattern = Regex("""@\w+"""),
                style = SpanStyle(color = Color(0xFF9F3EDB))
            ),

            // Comments – line and block (gray)
            SyntaxRule(
                pattern = Regex("""//.*|/\*[\s\S]*?\*/"""),
                style = SpanStyle(color = Color(0xFF7F7F7F))
            ),

            // Javadoc tags (teal)
            SyntaxRule(
                pattern = Regex("""@\w+"""), // inside comments
                style = SpanStyle(color = Color(0xFF008080))
            ).copy(
                // We'll apply this only inside comments – handled in code if needed
            ),

            // Class names / types in declarations (light blue)
            SyntaxRule(
                pattern = Regex("""\b[A-Z]\w*\b"""),
                style = SpanStyle(color = Color(0xFF00A1D6))
            ),

            // Error placeholder (bold red) – useful for syntax errors
            SyntaxRule(
                pattern = Regex("""error"""),
                style = SpanStyle(color = Color.Red, fontWeight = FontWeight.Bold)
            )
        )
    )
)
