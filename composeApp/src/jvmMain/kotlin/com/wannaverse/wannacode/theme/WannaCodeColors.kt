package com.wannaverse.wannacode.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
data class WannaCodeColors(
    // Background colors
    val background: Color,
    val backgroundSecondary: Color,
    val backgroundTertiary: Color,
    val surface: Color,
    val surfaceHover: Color,

    // Text colors
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val textDisabled: Color,

    // Border colors
    val border: Color,
    val borderLight: Color,
    val borderActive: Color,

    // Accent colors
    val accent: Color,
    val accentHover: Color,
    val success: Color,
    val error: Color,
    val warning: Color,

    // Editor colors
    val editorBackground: Color,
    val editorGutter: Color,
    val editorLineNumber: Color,
    val editorLineNumberActive: Color,
    val editorSelection: Color,
    val editorCursor: Color,

    // Component colors
    val buttonPrimary: Color,
    val buttonPrimaryText: Color,
    val buttonSecondary: Color,
    val buttonSecondaryText: Color,

    val inputBackground: Color,
    val inputBorder: Color,
    val inputText: Color,

    val dropdownBackground: Color,
    val dropdownBorder: Color,
    val dropdownText: Color,
    val dropdownHover: Color,

    val menuBackground: Color,
    val menuBorder: Color,
    val menuText: Color,
    val menuHover: Color,

    val tabBackground: Color,
    val tabBackgroundActive: Color,
    val tabBorder: Color,
    val tabBorderActive: Color,
    val tabText: Color,
    val tabTextActive: Color,

    val toolbarBackground: Color,
    val toolbarIcon: Color,
    val toolbarIconSuccess: Color,

    val tooltipBackground: Color,
    val tooltipText: Color,

    val scrollbarTrack: Color,
    val scrollbarThumb: Color,
    val scrollbarThumbHover: Color,

    // Explorer colors
    val explorerBackground: Color,
    val explorerIcon: Color,
    val explorerChevron: Color,
    val explorerText: Color,

    // Checkbox
    val checkboxBackground: Color,
    val checkboxBorder: Color,
    val checkboxCheck: Color,

    // Diagnostic colors
    val diagnosticError: Color,
    val diagnosticWarning: Color,
    val diagnosticInfo: Color,

    val isLight: Boolean
)

val DarkColors = WannaCodeColors(
    // Background colors
    background = Color(0xFF131317),
    backgroundSecondary = Color(0xFF17171D),
    backgroundTertiary = Color(0xFF1E1E24),
    surface = Color(0xFF2E2E33),
    surfaceHover = Color(0xFF3A3A40),

    // Text colors
    textPrimary = Color(0xFFFFFFFF),
    textSecondary = Color(0xFFB6B6B6),
    textTertiary = Color(0xFF848484),
    textDisabled = Color(0xFF5A5A5A),

    // Border colors
    border = Color(0xFF373737),
    borderLight = Color(0xFF2C2C2C),
    borderActive = Color(0xFF7796FF),

    // Accent colors
    accent = Color(0xFF7796FF),
    accentHover = Color(0xFF8CA6FF),
    success = Color(0xFF27FF27),
    error = Color(0xFFDE4545),
    warning = Color(0xFFFFB800),

    // Editor colors
    editorBackground = Color(0xFF17171D),
    editorGutter = Color(0xFF17171D),
    editorLineNumber = Color(0xFF383838),
    editorLineNumberActive = Color(0xFF858585),
    editorSelection = Color(0xFF264F78),
    editorCursor = Color(0xFFFFFFFF),

    // Component colors
    buttonPrimary = Color(0xFF7796FF),
    buttonPrimaryText = Color(0xFFFFFFFF),
    buttonSecondary = Color(0xFF3F3F3F),
    buttonSecondaryText = Color(0xFFFFFFFF),

    inputBackground = Color(0xFF2E2E33),
    inputBorder = Color(0xFF373737),
    inputText = Color(0xFFFFFFFF),

    dropdownBackground = Color(0xFF2E2E33),
    dropdownBorder = Color(0xFF373737),
    dropdownText = Color(0xFFFFFFFF),
    dropdownHover = Color(0xFF3A3A40),

    menuBackground = Color(0xFF111114),
    menuBorder = Color(0xFF252525),
    menuText = Color(0xFFB6B6B6),
    menuHover = Color(0xFF094771),

    tabBackground = Color(0xFF19191F),
    tabBackgroundActive = Color(0xFF17171D),
    tabBorder = Color(0xFF1B1B1B),
    tabBorderActive = Color(0xFF353232),
    tabText = Color(0xFFA0A0A0),
    tabTextActive = Color(0xFFFFFFFF),

    toolbarBackground = Color(0xFF16161A),
    toolbarIcon = Color(0xFFFFFFFF),
    toolbarIconSuccess = Color(0xFF27FF27),

    tooltipBackground = Color(0xFF2B2B2B),
    tooltipText = Color(0xFFFFFFFF),

    scrollbarTrack = Color(0xFF2C2C35),
    scrollbarThumb = Color(0xFF1E1E24),
    scrollbarThumbHover = Color(0xFF3A3A40),

    // Explorer colors
    explorerBackground = Color(0xFF131317),
    explorerIcon = Color(0xFF808080),
    explorerChevron = Color(0xFFFFFFFF),
    explorerText = Color(0xFFCCCCCC),

    // Checkbox
    checkboxBackground = Color(0xFF2E2E33),
    checkboxBorder = Color(0xFF373737),
    checkboxCheck = Color(0xFFFFFFFF),

    // Diagnostic colors
    diagnosticError = Color(0xFFDE4545),
    diagnosticWarning = Color(0xFFFFB800),
    diagnosticInfo = Color(0xFF00BFFF),

    isLight = false
)

val LightColors = WannaCodeColors(
    // Background colors
    background = Color(0xFFF5F5F5),
    backgroundSecondary = Color(0xFFFFFFFF),
    backgroundTertiary = Color(0xFFE8E8E8),
    surface = Color(0xFFFFFFFF),
    surfaceHover = Color(0xFFE8E8E8),

    // Text colors
    textPrimary = Color(0xFF1A1A1A),
    textSecondary = Color(0xFF5A5A5A),
    textTertiary = Color(0xFF8A8A8A),
    textDisabled = Color(0xFFB0B0B0),

    // Border colors
    border = Color(0xFFD0D0D0),
    borderLight = Color(0xFFE0E0E0),
    borderActive = Color(0xFF5070D0),

    // Accent colors
    accent = Color(0xFF5070D0),
    accentHover = Color(0xFF6080E0),
    success = Color(0xFF28A745),
    error = Color(0xFFDC3545),
    warning = Color(0xFFFFC107),

    // Editor colors
    editorBackground = Color(0xFFFFFFFF),
    editorGutter = Color(0xFFF5F5F5),
    editorLineNumber = Color(0xFFB0B0B0),
    editorLineNumberActive = Color(0xFF5A5A5A),
    editorSelection = Color(0xFFADD6FF),
    editorCursor = Color(0xFF1A1A1A),

    // Component colors
    buttonPrimary = Color(0xFF5070D0),
    buttonPrimaryText = Color(0xFFFFFFFF),
    buttonSecondary = Color(0xFFE0E0E0),
    buttonSecondaryText = Color(0xFF1A1A1A),

    inputBackground = Color(0xFFFFFFFF),
    inputBorder = Color(0xFFD0D0D0),
    inputText = Color(0xFF1A1A1A),

    dropdownBackground = Color(0xFFFFFFFF),
    dropdownBorder = Color(0xFFD0D0D0),
    dropdownText = Color(0xFF1A1A1A),
    dropdownHover = Color(0xFFE8E8E8),

    menuBackground = Color(0xFFFFFFFF),
    menuBorder = Color(0xFFD0D0D0),
    menuText = Color(0xFF5A5A5A),
    menuHover = Color(0xFFE8F4FD),

    tabBackground = Color(0xFFE8E8E8),
    tabBackgroundActive = Color(0xFFFFFFFF),
    tabBorder = Color(0xFFD0D0D0),
    tabBorderActive = Color(0xFF5070D0),
    tabText = Color(0xFF5A5A5A),
    tabTextActive = Color(0xFF1A1A1A),

    toolbarBackground = Color(0xFFE8E8E8),
    toolbarIcon = Color(0xFF5A5A5A),
    toolbarIconSuccess = Color(0xFF28A745),

    tooltipBackground = Color(0xFF1A1A1A),
    tooltipText = Color(0xFFFFFFFF),

    scrollbarTrack = Color(0xFFE8E8E8),
    scrollbarThumb = Color(0xFFC0C0C0),
    scrollbarThumbHover = Color(0xFFA0A0A0),

    // Explorer colors
    explorerBackground = Color(0xFFF5F5F5),
    explorerIcon = Color(0xFF808080),
    explorerChevron = Color(0xFF5A5A5A),
    explorerText = Color(0xFF1A1A1A),

    // Checkbox
    checkboxBackground = Color(0xFFFFFFFF),
    checkboxBorder = Color(0xFFD0D0D0),
    checkboxCheck = Color(0xFF5070D0),

    // Diagnostic colors
    diagnosticError = Color(0xFFDC3545),
    diagnosticWarning = Color(0xFFFFC107),
    diagnosticInfo = Color(0xFF17A2B8),

    isLight = true
)
