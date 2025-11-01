package com.wannaverse.wannacode.ide.editor.syntax

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

data class SyntaxRuleJson(
    val pattern: String,
    val style: StyleJson
)

data class StyleJson(
    val color: String,
    val fontWeight: String? = null
)

data class LanguageSyntaxJson(
    val rules: List<SyntaxRuleJson>
)

fun loadSyntaxMap(path: String): Map<String, LanguageSyntax> {
    val json = File(path).readText()
    val gson = Gson()

    val type = object : TypeToken<Map<String, LanguageSyntaxJson>>() {}.type
    val parsed: Map<String, LanguageSyntaxJson> = gson.fromJson(json, type)

    return parsed.mapValues { (_, langJson) ->
        LanguageSyntax(
            rules = langJson.rules.map { ruleJson ->
                SyntaxRule(
                    pattern = Regex(ruleJson.pattern),
                    style = ruleJson.style.toSpanStyle()
                )
            }
        )
    }
}

fun StyleJson.toSpanStyle(): SpanStyle {
    val colorValue = when {
        color.equals("red", ignoreCase = true) -> Color.Red
        color.startsWith("#") -> parseColorString(color)
        else -> Color.White
    }

    val weight = when (fontWeight?.lowercase()) {
        "bold" -> FontWeight.Bold
        else -> FontWeight.Normal
    }

    return SpanStyle(color = colorValue, fontWeight = weight)
}

fun parseColorString(color: String): Color {
    val cleaned = color.trim()

    return when {
        cleaned.equals("red", ignoreCase = true) -> Color.Red
        cleaned.equals("black", ignoreCase = true) -> Color.Black
        cleaned.equals("white", ignoreCase = true) -> Color.White
        cleaned.startsWith("#") -> {
            val hex = cleaned.removePrefix("#")
            val colorLong = when (hex.length) {
                6 -> 0xFF000000 or hex.toLong(16)
                8 -> hex.toLong(16)
                else -> throw IllegalArgumentException("Invalid color hex: $color")
            }
            Color(colorLong)
        }
        else -> Color.White
    }
}
