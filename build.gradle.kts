plugins {
    alias(libs.plugins.composeHotReload) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.spotless)
}

spotless {
    kotlin {
        target("**/*.kt")
        targetExclude(layout.buildDirectory)
        ktlint().setEditorConfigPath("$rootDir/.editorconfig")
    }

    kotlinGradle {
        target("*.gradle.kts")
        ktlint()
    }
}
