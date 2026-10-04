package com.example.galaxyguardian.data.service.llm

import com.example.galaxyguardian.data.model.GeneratedFile
import com.example.galaxyguardian.data.model.GeneratedFileKind

object MultiFileProjectParser {

    fun parse(rawCode: String, defaultFileName: String = "Main.kt"): List<GeneratedFile> {
        val trimmed = rawCode.trim()
        val fileMarkers = listOf(
            Regex("""(?m)^(?://|#)\s*(?:File|Path):\s*([a-zA-Z0-9_/\\.-]+)"""),
            Regex("""(?m)^===\s*(?:File|Path):\s*([a-zA-Z0-9_/\\.-]+)\s*==="""),
            Regex("""(?m)^```(?:kotlin|python|kt|py)?\s*//\s*(?:File|Path):\s*([a-zA-Z0-9_/\\.-]+)""")
        )

        val files = mutableListOf<GeneratedFile>()
        val lines = trimmed.lines()
        var currentPath: String? = null
        val currentContent = StringBuilder()

        for (line in lines) {
            var matchedPath: String? = null
            for (pattern in fileMarkers) {
                val match = pattern.find(line)
                if (match != null) {
                    matchedPath = match.groupValues[1].trim()
                    break
                }
            }

            if (matchedPath != null) {
                if (currentPath != null && currentContent.isNotBlank()) {
                    files.add(
                        GeneratedFile(
                            path = currentPath,
                            content = currentContent.toString().trim(),
                            kind = inferFileKind(currentPath)
                        )
                    )
                    currentContent.clear()
                }
                currentPath = matchedPath
            } else {
                if (currentPath != null) {
                    currentContent.append(line).append("\n")
                } else {
                    currentContent.append(line).append("\n")
                }
            }
        }

        if (currentPath != null && currentContent.isNotBlank()) {
            files.add(
                GeneratedFile(
                    path = currentPath,
                    content = currentContent.toString().trim(),
                    kind = inferFileKind(currentPath)
                )
            )
        } else if (files.isEmpty() && trimmed.isNotBlank()) {
            files.add(
                GeneratedFile(
                    path = defaultFileName,
                    content = trimmed,
                    kind = inferFileKind(defaultFileName)
                )
            )
        }

        return files
    }

    private fun inferFileKind(path: String): GeneratedFileKind {
        val lower = path.lowercase()
        return when {
            lower.endsWith("screen.kt") || lower.contains("ui/") -> GeneratedFileKind.COMPOSE_UI
            lower.endsWith("viewmodel.kt") -> GeneratedFileKind.VIEW_MODEL
            lower.endsWith("uistate.kt") || lower.endsWith("state.kt") -> GeneratedFileKind.UI_STATE
            lower.endsWith("repository.kt") -> GeneratedFileKind.REPOSITORY
            lower.endsWith("dao.kt") || lower.endsWith("entity.kt") || lower.endsWith("database.kt") -> GeneratedFileKind.ENTITY_DAO
            lower.endsWith("service.kt") || lower.contains("api") -> GeneratedFileKind.NETWORK_CLIENT
            lower.endsWith("worker.kt") -> GeneratedFileKind.WORKER_SERVICE
            lower.endsWith("build.gradle.kts") || lower.endsWith("settings.gradle.kts") -> GeneratedFileKind.GRADLE_BUILD
            lower.endsWith("androidmanifest.xml") -> GeneratedFileKind.MANIFEST
            lower.endsWith(".py") -> GeneratedFileKind.PYTHON_SOURCE
            lower.contains("test") -> GeneratedFileKind.TEST_SOURCE
            lower.endsWith(".md") -> GeneratedFileKind.DOCUMENTATION
            else -> GeneratedFileKind.OTHER
        }
    }
}
