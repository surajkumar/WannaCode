package com.wannaverse.wannacode.ide.editor.viewmodel

import java.io.File

data class TabContent(
    val id: Int,
    val file: File,
    val text: String,
    val isDirty: Boolean = false,
    val readOnly: Boolean = false
)
