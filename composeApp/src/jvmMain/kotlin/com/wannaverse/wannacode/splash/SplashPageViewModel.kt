package com.wannaverse.wannacode.splash

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import java.io.File
import java.net.URI
import java.net.URL
import java.nio.file.Files
import java.nio.file.Paths
import java.util.zip.ZipFile
import kotlin.system.exitProcess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray

class SplashPageViewModel : ViewModel() {
    var activeScreen by mutableStateOf(SplashPageOption.CREATE)
    var projectName = mutableStateOf("untitled1")
    var location = mutableStateOf(Paths.get(System.getProperty("user.home"), "WannaCodeProjects").toAbsolutePath().toString())
    var shouldCreateGitRepository = mutableStateOf(false)
    val javaOptions = (25 downTo 8).map { it.toString() }
    val selectedJavaVersion = mutableStateOf(javaOptions.first())
    val buildSystemOptions = listOf("Gradle", "Maven", "None")
    var selectedBuildSystem = mutableStateOf(BuildSystem.GRADLE)
    var selectedBuildSystemInstallation = mutableStateOf(BuildSystemInstallation.WRAPPER)
    val gradleDslOptions = listOf("Kotlin", "Groovy")
    var selectedGradleDsl = mutableStateOf(GradleDsl.KOTLIN)
    var buildToolInstallationPath = mutableStateOf("")

    val gradleDistributions = mutableStateOf(listOf("9.1.0", "9.0.0", "8.14.3", "7.6.6"))
    var selectedDistribution = mutableStateOf(gradleDistributions.value.first())

    var groupId = mutableStateOf("com.example")
    var artifactId = mutableStateOf(projectName.value)
    var isError = mutableStateOf(false)
    var errorMessage = mutableStateOf("")

    var isProjectCreating = mutableStateOf(false)
    var showIDE = mutableStateOf(false)
    var projectDir = mutableStateOf(File(""))

    fun fetchGradleVersions() = viewModelScope.launch {
        try {
            val url = URL("https://services.gradle.org/versions/all")
            val response = url.readText()
            val jsonArray = JSONArray(response)

            val versions = List(jsonArray.length()) { jsonArray.getJSONObject(it).getString("version") }
                .filter { "-" !in it } // Keep only versions without a dash

            gradleDistributions.value = versions
        } catch (e: Exception) {
            // Handle errors gracefully
            println("Failed to fetch Gradle versions: ${e.message}")
            gradleDistributions.value = emptyList()
        }
    }

    fun hasErrors(): Boolean = !isProjectNameValid() ||
        !isGroupIdValid() ||
        !isArtifactIdValid() ||
        !isLocationDirectoryEmpty()

    fun createProject(hideSplash: () -> Unit) {
        isError.value = hasErrors()

        if (isError.value) {
            errorMessage.value = "There are some errors in the form, please check and try again"
            return
        }

        errorMessage.value = ""

        isProjectCreating.value = true

        projectDir.value = File(location.value, projectName.value)

        if (!projectDir.value.exists()) {
            projectDir.value.mkdirs()
        }

        try {
            if (shouldCreateGitRepository.value) {
                executeCommand(listOf("git", "init"), projectDir.value)
            }

            when (selectedBuildSystem.value) {
                BuildSystem.NONE -> createJavaProject(projectDir.value)
                BuildSystem.GRADLE -> createGradleProject(projectDir.value)
                BuildSystem.MAVEN -> createMavenProject(projectDir.value)
            }

            isProjectCreating.value = false
            showIDE.value = true
            hideSplash()
        } catch (e: Exception) {
            isProjectCreating.value = false
            errorMessage.value = "Failed to create project: ${e.message}"
        }
    }

    fun getDir(): File = File(location.value, projectName.value)

    private fun createJavaProject(projectDir: File) {
        val srcDir = File(projectDir, "src")
        srcDir.mkdirs()
        val mainJava = File(srcDir, "Main.java")
        mainJava.writeText(
            """
        public class Main {
            public static void main(String[] args) {
                System.out.println("Hello, World!");
            }
        }
            """.trimIndent()
        )
    }

    private fun createGradleProject(projectDir: File) {
        when (selectedBuildSystemInstallation.value) {
            BuildSystemInstallation.WRAPPER -> {
                setupGradleWrapper(projectDir, selectedDistribution.value)
            }
            BuildSystemInstallation.LOCAL -> "$buildToolInstallationPath/gradle"
        }
    }

    private fun createMavenProject(projectDir: File) = viewModelScope.launch {
        val mvnCmd = when (selectedBuildSystemInstallation.value) {
            BuildSystemInstallation.WRAPPER -> "./mvnw"
            BuildSystemInstallation.LOCAL -> "mvn"
        }

        val cmd = listOf(
            mvnCmd,
            "archetype:generate",
            "-DgroupId=${groupId.value}",
            "-DartifactId=${artifactId.value}",
            "-DarchetypeArtifactId=maven-archetype-quickstart",
            "-DinteractiveMode=false"
        )

        executeCommand(cmd, projectDir)
    }

    private fun executeCommand(cmd: List<String>, workingDir: File) {
        try {
            println("Running command: ${cmd.joinToString(" ")}")

            val process = ProcessBuilder(cmd)
                .directory(workingDir)
                .redirectErrorStream(false)
                .start()

            val stdout = Thread {
                process.inputStream.bufferedReader().use { reader ->
                    reader.forEachLine { line ->
                        println("[STDOUT] $line")
                    }
                }
            }

            val stderr = Thread {
                process.errorStream.bufferedReader().use { reader ->
                    reader.forEachLine { line ->
                        println("[STDERR] $line")
                    }
                }
            }

            stdout.start()
            stderr.start()

            val exitCode = process.waitFor()
            stdout.join()
            stderr.join()

            if (exitCode != 0) {
                throw RuntimeException("Command failed with exit code $exitCode: ${cmd.joinToString(" ")}")
            }
        } catch (e: Exception) {
            throw Exception("Command failed with exception: ${e.message}")
        }
    }

    fun setupGradleWrapper(projectDir: File, gradleVersion: String) = viewModelScope.launch {
        try {
            val gradleZipUrl = "https://services.gradle.org/distributions/gradle-$gradleVersion-bin.zip"
            val tempZipDownload = File.createTempFile("gradle-$gradleVersion-bin-", ".zip")
            val tempExtractedZip = Files.createTempDirectory("gradle-$gradleVersion-").toFile()

            URI(gradleZipUrl).toURL().openStream().use { input ->
                tempZipDownload.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            println("Gradle distribution has been downloaded " + tempZipDownload.absolutePath)
            println("Extracting " + tempZipDownload.absolutePath)

            ZipFile(tempZipDownload).use { zip ->
                zip.entries().asSequence().forEach { entry ->
                    val outFile = File(tempExtractedZip, entry.name)
                    if (entry.isDirectory) {
                        outFile.mkdirs()
                    } else {
                        outFile.parentFile.mkdirs()
                        zip.getInputStream(entry).use { input ->
                            outFile.outputStream().use { output ->
                                input.copyTo(output)
                            }
                        }
                    }
                }
            }

            println("Gradle has been extracted into directory " + tempExtractedZip.absolutePath)
            println("Running gradle wrapper command")

            executeCommand(
                listOf(
                    tempExtractedZip.absolutePath + "/gradle-$gradleVersion/bin/gradle.bat",
                    "init",
                    "--comments",
                    "--dsl", selectedGradleDsl.value.name.lowercase(),
                    "--no-incubating",
                    "--java-version", selectedJavaVersion.value,
                    "--overwrite",
                    "--package", groupId.value.lowercase(),
                    "--project-name", projectName.value,
                    "--no-split-project",
                    "--test-framework", "junit",
                    "--type", "java-application",
                    "--use-defaults"
                ),
                projectDir
            )

            executeCommand(
                listOf(
                    "gradlew.bat",
                    "build"
                ),
                projectDir
            )

            // tempZipDownload.deleteRecursively()
            // tempExtractedZip.deleteRecursively()
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                errorMessage.value = "Failed to setup Gradle wrapper: ${e.message}"
            }
        }
    }

    private fun isWindows() = System.getProperty("os.name").lowercase().contains("windows")

    fun isProjectNameValid(): Boolean {
        val trimmed = projectName.value.trim()
        val pattern = "^[A-Za-z][A-Za-z0-9_-]{0,49}$".toRegex()
        return pattern.matches(trimmed)
    }

    fun getProjectNameError(): String? = when {
        projectName.value.isBlank() -> "Project name cannot be empty"
        !projectName.value.first().isLetter() -> "Must start with a letter"
        !projectName.value.matches("^[A-Za-z][A-Za-z0-9_-]*$".toRegex()) ->
            "Only letters, numbers, _ and - are allowed"
        else -> null
    }

    fun isGroupIdValid(): Boolean {
        val trimmed = groupId.value.trim()
        if (trimmed.isEmpty()) return false
        if (trimmed.startsWith('.') || trimmed.endsWith('.')) return false
        if (".." in trimmed) return false
        val segments = trimmed.split('.')
        if (segments.isEmpty()) return false
        val segmentPattern = "^[a-zA-Z][a-zA-Z0-9_]*$".toRegex()
        for (segment in segments) {
            if (!segmentPattern.matches(segment)) return false
        }
        if (trimmed != trimmed.lowercase()) return false
        return true
    }

    fun isArtifactIdValid(): Boolean {
        val trimmed = groupId.value.trim()
        if (trimmed.isEmpty()) return false
        if (trimmed.startsWith('.') || trimmed.endsWith('.')) return false
        if (".." in trimmed) return false
        val segments = trimmed.split('.')
        if (segments.isEmpty()) return false
        val segmentPattern = "^[a-zA-Z][a-zA-Z0-9_]*$".toRegex()
        for (segment in segments) {
            if (!segmentPattern.matches(segment)) return false
        }
        if (trimmed != trimmed.lowercase()) return false
        return true
    }

    private fun validateId(id: String, idType: String): String? {
        if (id.isBlank()) return "$idType cannot be empty."
        if (id.startsWith('.') || id.endsWith('.')) return "$idType cannot start or end with a dot."
        if (".." in id) return "$idType cannot contain consecutive dots."
        if (id.split('.').any { it.isEmpty() }) return "Each segment of the $idType must be non-empty."
        if (id.split('.').any { !it.matches("^[a-zA-Z][a-zA-Z0-9_]*$".toRegex()) }) {
            return "Each segment of the $idType must start with a letter and contain only letters, digits, or underscores."
        }
        if (id != id.lowercase()) return "$idType should be in lowercase."
        return null
    }

    fun getGroupIdError(): String? = validateId(groupId.value, "Group ID")

    fun getArtifactIdError(): String? = validateId(artifactId.value, "Artifact ID")

    fun isLocationDirectoryEmpty(): Boolean {
        val dir =
            if (location.value.endsWith("/"))
                File(location.value + projectName.value)
            else File(location.value + "/" + projectName.value)

        return !dir.exists() ||
            (dir.isDirectory && dir.listFiles()?.isEmpty() == true)
    }

    fun getLocationError(): String? = when {
        !isLocationDirectoryEmpty() -> "The location directory is not empty."
        location.value.isBlank() -> "The location field cannot be empty."
        else -> null
    }

    fun getGitError(): String? = when {
        !checkGit() -> "Git cannot be found"
        else -> null
    }

    fun checkGit(): Boolean {
        try {
            executeCommand(listOf("git", "--version"), File(location.value, projectName.value))
            return true
        } catch (_: Exception) {
            return false
        }
    }

    fun reset() {
        projectName.value = ""
        location.value = Paths.get(System.getProperty("user.home"), "WannaCodeProjects").toAbsolutePath().toString()
        shouldCreateGitRepository.value = false
        selectedJavaVersion.value = javaOptions.first()
        selectedBuildSystem.value = BuildSystem.NONE
        selectedBuildSystemInstallation.value = BuildSystemInstallation.WRAPPER
        selectedGradleDsl.value = GradleDsl.KOTLIN
        buildToolInstallationPath.value = ""
        selectedDistribution.value = gradleDistributions.value.first()
        groupId.value = "com.example"
        artifactId.value = ""
    }

    fun closeProgram() {
        exitProcess(0)
    }
}
