package com.example.galaxyguardian.data.model

import com.example.galaxyguardian.data.service.llm.GenerationConfig

enum class TargetLanguage(
    val persistenceId: String,
    val displayName: String,
    val fenceNames: Set<String>
) {
    KOTLIN_ANDROID(
        persistenceId = "kotlin_android",
        displayName = "Kotlin / Android",
        fenceNames = setOf("kotlin", "kt")
    ),
    PYTHON(
        persistenceId = "python",
        displayName = "Python",
        fenceNames = setOf("python", "py")
    );

    companion object {
        fun fromPersistenceId(id: String?): TargetLanguage {
            return when (id?.lowercase()?.trim()) {
                "kotlin_android", "kotlin", "kt" -> KOTLIN_ANDROID
                "python", "py" -> PYTHON
                else -> PYTHON
            }
        }
    }

    fun supportedTargets(): List<GenerationTarget> {
        return when (this) {
            KOTLIN_ANDROID -> listOf(
                GenerationTarget.COMPOSABLE_SCREEN,
                GenerationTarget.ANDROID_FEATURE,
                GenerationTarget.ANDROID_APP,
                GenerationTarget.VIEW_MODEL,
                GenerationTarget.DATA_LAYER,
                GenerationTarget.WORK_MANAGER_WORKER,
                GenerationTarget.BACKGROUND_SERVICE,
                GenerationTarget.CODE_SNIPPET
            )
            PYTHON -> listOf(
                GenerationTarget.BOT,
                GenerationTarget.AUTOMATION,
                GenerationTarget.CODE_SNIPPET
            )
        }
    }
}

enum class GenerationTarget(
    val displayName: String,
    val description: String
) {
    AUTO("Auto Detect", "Automatically infer from prompt"),
    CODE_SNIPPET("Code Snippet", "Focused single-file code snippet"),
    COMPOSABLE_SCREEN("Compose Screen", "Jetpack Compose M3 UI Screen"),
    ANDROID_FEATURE("Android Feature", "Multi-file feature (UI + ViewModel + Repository)"),
    ANDROID_APP("Full Android App", "Complete Android Studio app scaffold"),
    VIEW_MODEL("ViewModel & State", "ViewModel with StateFlow & UiState"),
    DATA_LAYER("Data Layer (Room / Retrofit)", "Repository + Entity + DAO + Database"),
    WORK_MANAGER_WORKER("WorkManager Worker", "Background worker & request factory"),
    BACKGROUND_SERVICE("Foreground Service", "Android Foreground Service & Notification"),
    BOT("Autonomous Bot", "Python autonomous bot"),
    AUTOMATION("Automation Script", "Python task automation script");

    companion object {
        fun fromString(id: String?): GenerationTarget {
            return entries.firstOrNull { it.name.equals(id, ignoreCase = true) } ?: AUTO
        }
    }
}

enum class ArchitectureProfile(val displayName: String) {
    SIMPLE("Simple Monolith"),
    MVVM("MVVM + Compose"),
    MVI("MVI Unidirectional State"),
    CLEAN_ARCHITECTURE("Clean Architecture"),
    OFFLINE_FIRST("Offline-First Room");

    companion object {
        fun fromString(id: String?): ArchitectureProfile {
            return entries.firstOrNull { it.name.equals(id, ignoreCase = true) } ?: MVVM
        }
    }
}

enum class GeneratedFileKind {
    COMPOSE_UI,
    VIEW_MODEL,
    UI_STATE,
    REPOSITORY,
    ENTITY_DAO,
    NETWORK_CLIENT,
    WORKER_SERVICE,
    GRADLE_BUILD,
    MANIFEST,
    PYTHON_SOURCE,
    TEST_SOURCE,
    DOCUMENTATION,
    OTHER
}

data class GeneratedFile(
    val path: String,
    val content: String,
    val kind: GeneratedFileKind = GeneratedFileKind.OTHER
)

data class ProjectMetadata(
    val packageName: String? = "com.example.generatedapp",
    val tags: List<String> = emptyList(),
    val category: String = "General",
    val qualityGate: QualityGate = QualityGate.NONE,
    val generationDurationMs: Long = 0L,
    val versionNumber: Int = 1
)

enum class QualityGate {
    NONE,
    CRITICAL,
    HIGH,
    MEDIUM
}

data class GeneratedProject(
    val id: String,
    val title: String,
    val prompt: String,
    val language: TargetLanguage,
    val target: GenerationTarget,
    val architecture: ArchitectureProfile = ArchitectureProfile.MVVM,
    val files: List<GeneratedFile>,
    val analysis: QualityAnalysis,
    val lastExecution: ExecutionResult? = null,
    val providerType: String = "GEMINI",
    val modelName: String = "gemini-3.5-flash",
    val metadata: ProjectMetadata = ProjectMetadata(),
    val createdAt: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val versionHistory: List<ProjectVersionRecord> = emptyList()
)

data class ProjectVersionRecord(
    val versionId: String,
    val versionNumber: Int,
    val createdAt: Long,
    val description: String,
    val files: List<GeneratedFile>
)

data class GenerationRequest(
    val prompt: String,
    val language: TargetLanguage = TargetLanguage.KOTLIN_ANDROID,
    val target: GenerationTarget = GenerationTarget.COMPOSABLE_SCREEN,
    val architecture: ArchitectureProfile = ArchitectureProfile.MVVM,
    val personality: BotPersonality? = null,
    val config: GenerationConfig = GenerationConfig()
)
