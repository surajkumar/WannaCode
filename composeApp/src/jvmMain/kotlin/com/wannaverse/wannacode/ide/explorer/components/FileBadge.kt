package com.wannaverse.wannacode.ide.explorer.components

import androidx.compose.ui.graphics.Color

data class FileBadge(val letter: String, val color: Color)

val fileBadges = mapOf(
    // ── Programming languages ───────────────────────────────────────────────
    "java" to FileBadge("J", Color(0xFF7796FF)), // Java
    "kt" to FileBadge("K", Color(0xFF3F51B5)), // Kotlin
    "kts" to FileBadge("Ks", Color(0xFF3F51B5)), // Kotlin script
    "js" to FileBadge("JS", Color(0xFFF7DF1E)), // JavaScript
    "ts" to FileBadge("TS", Color(0xFF3178C6)), // TypeScript
    "tsx" to FileBadge("TSX", Color(0xFF3178C6)), // TSX (React)
    "jsx" to FileBadge("JSX", Color(0xFF61DAFB)), // JSX (React)
    "py" to FileBadge("Py", Color(0xFF3776AB)), // Python
    "rb" to FileBadge("Rb", Color(0xFFCC342D)), // Ruby
    "go" to FileBadge("Go", Color(0xFF00ADD8)), // Go
    "rs" to FileBadge("Rs", Color(0xFFDEA584)), // Rust
    "cpp" to FileBadge("C++", Color(0xFF00599C)), // C++
    "c" to FileBadge("C", Color(0xFF00599C)), // C
    "h" to FileBadge("H", Color(0xFF00599C)), // Header (C/C++)
    "hpp" to FileBadge("H++", Color(0xFF00599C)), // C++ header
    "cs" to FileBadge("C#", Color(0xFF68217A)), // C#
    "php" to FileBadge("PHP", Color(0xFF777BB4)), // PHP
    "swift" to FileBadge("Sw", Color(0xFFF05138)), // Swift
    "scala" to FileBadge("Sc", Color(0xFFDE3423)), // Scala
    "dart" to FileBadge("Dt", Color(0xFF00D1B2)), // Dart
    "lua" to FileBadge("Lua", Color(0xFF000080)), // Lua
    "sh" to FileBadge("Sh", Color(0xFF4EAA25)), // Bash / Shell
    "bash" to FileBadge("Sh", Color(0xFF4EAA25)), // Bash script
    "zsh" to FileBadge("Zsh", Color(0xFF4EAA25)), // Zsh script

    // ── Web / Markup ───────────────────────────────────────────────────────
    "html" to FileBadge("H", Color(0xFFE44D26)), // HTML
    "htm" to FileBadge("H", Color(0xFFE44D26)),
    "css" to FileBadge("CSS", Color(0xFF1572B6)), // CSS
    "scss" to FileBadge("SC", Color(0xFFCC6699)), // SCSS
    "sass" to FileBadge("Sa", Color(0xFFCC6699)), // SASS
    "less" to FileBadge("L", Color(0xFF1D365D)), // LESS
    "xml" to FileBadge("X", Color(0xFF0066CC)), // XML
    "svg" to FileBadge("SVG", Color(0xFFFFB900)), // SVG
    "json" to FileBadge("J", Color(0xFFF9C806)), // JSON
    "yaml" to FileBadge("Y", Color(0xFFCB3837)), // YAML
    "yml" to FileBadge("Y", Color(0xFFCB3837)),
    "toml" to FileBadge("T", Color(0xFF9A3412)), // TOML
    "md" to FileBadge("MD", Color(0xFF4183C4)), // Markdown
    "markdown" to FileBadge("MD", Color(0xFF4183C4)),

    // ── Data / Config ───────────────────────────────────────────────────────
    "sql" to FileBadge("SQL", Color(0xFFDAD8D8)), // SQL
    "csv" to FileBadge("CSV", Color(0xFF73AF56)), // CSV
    "properties" to FileBadge("Pr", Color(0xFF6A737B)), // .properties
    "ini" to FileBadge("INI", Color(0xFF6A737B)), // .ini
    "env" to FileBadge(".env", Color(0xFF00BFA5)), // .env
    "gitignore" to FileBadge("Git", Color(0xFFF54E4E)), // .gitignore

    // ── Build / Gradle / Maven ─────────────────────────────────────────────
    "gradle" to FileBadge("Gr", Color(0xFF02303A)), // build.gradle
    "gradle.kts" to FileBadge("GrK", Color(0xFF02303A)), // build.gradle.kts
    "pom" to FileBadge("Pom", Color(0xFFBA1A1A)), // pom.xml

    // ── Misc / Docs ───────────────────────────────────────────────────────
    "txt" to FileBadge("T", Color(0xFFFF9800)), // Plain text
    "log" to FileBadge("Log", Color(0xFF9E9E9E)), // Log files
    "pdf" to FileBadge("PDF", Color(0xFFB30000)), // PDF
    "doc" to FileBadge("Doc", Color(0xFF2B579A)), // Word
    "docx" to FileBadge("Doc", Color(0xFF2B579A)),
    "xls" to FileBadge("Xls", Color(0xFF217346)), // Excel
    "xlsx" to FileBadge("Xls", Color(0xFF217346)),

    // ── Images ─────────────────────────────────────────────────────────────
    "png" to FileBadge("Img", Color(0xFF9C27B0)),
    "jpg" to FileBadge("Img", Color(0xFF9C27B0)),
    "jpeg" to FileBadge("Img", Color(0xFF9C27B0)),
    "gif" to FileBadge("Img", Color(0xFF9C27B0)),
    "webp" to FileBadge("Img", Color(0xFF9C27B0)),
    "ico" to FileBadge("Ico", Color(0xFF9C27B0)),

    // ── Archives ───────────────────────────────────────────────────────────
    "zip" to FileBadge("Zip", Color(0xFFFDD835)),
    "rar" to FileBadge("Rar", Color(0xFFFDD835)),
    "7z" to FileBadge("7z", Color(0xFFFDD835)),
    "tar" to FileBadge("Tar", Color(0xFFFDD835)),
    "gz" to FileBadge("Gz", Color(0xFFFDD835)),

    // ── Executables / Binaries ─────────────────────────────────────────────
    "exe" to FileBadge("Exe", Color(0xFF004E8A)),
    "dll" to FileBadge("Dll", Color(0xFF004E8A)),
    "so" to FileBadge("So", Color(0xFF004E8A)),
    "class" to FileBadge("Cls", Color(0xFF7796FF)), // Java bytecode

    // ── Special / IDE files ─────────────────────────────────────────────────
    "iml" to FileBadge("IML", Color(0xFF808080)), // IntelliJ module
    "idea" to FileBadge("IDEA", Color(0xFF808080)), // .idea folder files
    "vscode" to FileBadge("VS", Color(0xFF007ACC)), // VSCode settings
    "editorconfig" to FileBadge("EC", Color(0xFF808080))
)
