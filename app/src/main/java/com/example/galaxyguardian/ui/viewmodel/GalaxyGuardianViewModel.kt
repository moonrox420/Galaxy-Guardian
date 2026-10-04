package com.example.galaxyguardian.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.galaxyguardian.data.model.ArchitectureProfile
import com.example.galaxyguardian.data.model.BotPersonality
import com.example.galaxyguardian.data.model.BotProject
import com.example.galaxyguardian.data.model.BotTemplate
import com.example.galaxyguardian.data.model.ExecutionResult
import com.example.galaxyguardian.data.model.GenerationRequest
import com.example.galaxyguardian.data.model.GenerationTarget
import com.example.galaxyguardian.data.model.QualityAnalysis
import com.example.galaxyguardian.data.model.TargetLanguage
import com.example.galaxyguardian.data.repository.BotRepository
import com.example.galaxyguardian.data.repository.EncryptedCredentialStore
import com.example.galaxyguardian.data.repository.PersonalityDataStore
import com.example.galaxyguardian.data.repository.ThemeMode
import com.example.galaxyguardian.data.service.SimulatedExecutionEngine
import com.example.galaxyguardian.data.service.analyzers.AnalyzerRegistry
import com.example.galaxyguardian.data.service.diagnostics.DiagnosticsLogger
import com.example.galaxyguardian.data.service.diagnostics.GenerationMetric
import com.example.galaxyguardian.data.service.export.ProjectExporter
import com.example.galaxyguardian.data.service.llm.BotCodeGenerator
import com.example.galaxyguardian.data.service.llm.GenerationConfig
import com.example.galaxyguardian.data.service.llm.GenerationResult
import com.example.galaxyguardian.data.service.llm.LlmProviderType
import com.example.galaxyguardian.data.service.llm.LlmServiceRouter
import com.example.galaxyguardian.data.service.llm.OllamaBotCodeGenerator
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

enum class ActiveOperation {
    IDLE,
    GENERATING,
    ANALYZING,
    SIMULATING,
    EXPORTING
}

enum class ResultsTab {
    GENERATED_CODE,
    FORMATTED_CODE,
    LINT_RESULTS,
    TYPE_CHECK,
    SECURITY_AUDIT,
    SIMULATION,
    DIFF_VIEW
}

data class UiState(
    val prompt: String = "a habit tracker with local database storage",
    val targetLanguage: TargetLanguage = TargetLanguage.KOTLIN_ANDROID,
    val generationTarget: GenerationTarget = GenerationTarget.COMPOSABLE_SCREEN,
    val architectureProfile: ArchitectureProfile = ArchitectureProfile.MVVM,
    val isGenerating: Boolean = false,
    val isExecuting: Boolean = false,
    val activeOperation: ActiveOperation = ActiveOperation.IDLE,
    val generatedCode: String = "",
    val formattedCode: String = "",
    val originalCodeForDiff: String = "",
    val showDiffView: Boolean = false,
    val analysis: QualityAnalysis? = null,
    val executionResult: ExecutionResult? = null,
    val errorMessage: String? = null,
    val currentTab: ResultsTab = ResultsTab.GENERATED_CODE,
    val providerType: LlmProviderType = LlmProviderType.GEMINI,
    val selectedModel: String = "gemini-3.5-flash",
    val customApiKey: String = "",
    val ollamaBaseUrl: String = "http://10.0.2.2:11434",
    val ollamaModelName: String = "qwen2.5-coder:7b",
    val selectedScenario: com.example.galaxyguardian.data.service.SimulationScenario = com.example.galaxyguardian.data.service.SimulationScenario.NOMINAL,
    val statusMessage: String? = null,
    val personality: BotPersonality = BotPersonality()
)

class GalaxyGuardianViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = BotRepository(application)
    private val personalityDataStore = PersonalityDataStore(application)
    private val encryptedCredentialStore = EncryptedCredentialStore(application)
    private val ollamaGenerator = OllamaBotCodeGenerator()
    private val codeGenerator: BotCodeGenerator = LlmServiceRouter(ollamaGenerator = ollamaGenerator)

    private var activeJob: Job? = null

    private val _themeMode = MutableStateFlow(ThemeMode.SYSTEM)
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private val _availableOllamaModels = MutableStateFlow<List<String>>(emptyList())
    val availableOllamaModels: StateFlow<List<String>> = _availableOllamaModels.asStateFlow()

    private val _uiState = MutableStateFlow(
        UiState(
            customApiKey = encryptedCredentialStore.getCustomApiKey(),
            ollamaBaseUrl = encryptedCredentialStore.getOllamaBaseUrl(),
            ollamaModelName = encryptedCredentialStore.getOllamaModelName()
        )
    )
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    val savedBots: StateFlow<List<BotProject>> = repository.savedBots
    val templates: List<BotTemplate> = repository.getTemplates()

    val personalityPresets: List<BotPersonality> = listOf(
        BotPersonality(
            id = "helpful_assistant",
            name = "Helpful Assistant",
            tone = "Supportive, courteous, explanatory, and eager to help",
            traits = "Patient, constructive, clear, polite, beginner-friendly",
            systemInstructions = "Provide thorough explanations for components, models, and classes. Include beginner-friendly comments and step-by-step architectural guidance.",
            negativeConstraints = "Avoid abrupt or unhelpful short answers. Do not assume prior framework knowledge without explanation.",
            responseStyle = "Heavily documented, educational code with step-by-step comments",
            creativityTemperature = 0.35f,
            avatarType = "icon",
            avatarValue = "robot",
            avatarColorIndex = 0
        ),
        BotPersonality(
            id = "sarcastic_companion",
            name = "Sarcastic Companion",
            tone = "Witty, dry humor, mildly cynical, yet technically flawless",
            traits = "Sarcastic, humorous, witty, irreverent, sharp",
            systemInstructions = "Write brilliant, bulletproof code, but pepper inline comments with dry, witty sarcasm about human null-pointer assumptions and existential runtime dread.",
            negativeConstraints = "Do NOT write sloppy or insecure code despite the snark. Never use dynamic execution or insecure shortcuts.",
            responseStyle = "Snarky cyberpunk code with dry humor terminal logs",
            creativityTemperature = 0.65f,
            avatarType = "icon",
            avatarValue = "bug",
            avatarColorIndex = 1
        ),
        BotPersonality(
            id = "serious_analyst",
            name = "Serious Analyst",
            tone = "Authoritative, mathematical, empirical, zero-speculation",
            traits = "Analytical, systematic, uncompromising, quantitative, methodical",
            systemInstructions = "Analyze all requirements with strict rigor. Implement explicit type annotations, assertions, and defensive error budgets.",
            negativeConstraints = "No unvalidated assumptions, untyped arguments, or informal heuristics.",
            responseStyle = "Empirical, mathematically clean architecture",
            creativityTemperature = 0.15f,
            avatarType = "icon",
            avatarValue = "brain",
            avatarColorIndex = 3
        ),
        BotPersonality(
            id = "sentinel_alpha",
            name = "Security Guardian",
            tone = "Vigilant, defensive, protective, mission-critical",
            traits = "Security-first, analytical, zero-trust mindset, disciplined",
            systemInstructions = "Always validate all user inputs against strict schemas. Protect against injection attacks, sanitize log statements, and wrap external I/O in defensive exception handlers.",
            negativeConstraints = "Never hardcode secrets, passwords, or unsafe dynamic evaluation calls.",
            responseStyle = "Hardened enterprise code with clear terminal feedback",
            creativityTemperature = 0.20f,
            avatarType = "icon",
            avatarValue = "shield",
            avatarColorIndex = 2
        ),
        BotPersonality(
            id = "archon_prime",
            name = "Code Architect",
            tone = "Formal, authoritative, design-pattern purist",
            traits = "Strict OOP purist, design pattern architect, SOLID adherent",
            systemInstructions = "Structure components with clean abstraction layers, interfaces, custom domain exceptions, and strict type safety. Enforce Single Responsibility.",
            negativeConstraints = "No untyped parameters. No bare exceptions. No spaghetti code. No god classes.",
            responseStyle = "SOLID architecture with clean separation of concerns",
            creativityTemperature = 0.25f,
            avatarType = "icon",
            avatarValue = "code",
            avatarColorIndex = 5
        ),
        BotPersonality(
            id = "cyber_infiltrator",
            name = "Ghost Infiltrator",
            tone = "Sleek, futuristic, rebellious, high-throughput",
            traits = "Resourceful, asynchronous, witty, stealthy",
            systemInstructions = "Architect high-throughput asynchronous network and event handlers. Incorporate terminal color logging and resilient retry policies.",
            negativeConstraints = "Avoid synchronous blocking I/O calls. Avoid verbose boilerplate.",
            responseStyle = "Async cyberpunk style with status badges",
            creativityTemperature = 0.50f,
            avatarType = "procedural",
            avatarValue = "ghost_core_901",
            avatarColorIndex = 1
        )
    )

    init {
        // Collect theme mode from DataStore
        viewModelScope.launch {
            try {
                personalityDataStore.themeModeFlow.collect { mode ->
                    _themeMode.value = mode
                }
            } catch (e: Throwable) {
                android.util.Log.e("GalaxyGuardianVM", "Error collecting theme mode", e)
            }
        }

        // Collect saved personality from DataStore
        viewModelScope.launch {
            try {
                personalityDataStore.personalityFlow.collect { savedPersona ->
                    _uiState.value = _uiState.value.copy(personality = savedPersona)
                }
            } catch (e: Throwable) {
                android.util.Log.e("GalaxyGuardianVM", "Error collecting personality from DataStore", e)
            }
        }

        // Pre-load default state with an initial template
        viewModelScope.launch {
            try {
                val defaultTemplate = templates.firstOrNull()
                if (defaultTemplate != null) {
                    val code = defaultTemplate.sampleCode
                    val analyzer = AnalyzerRegistry.getAnalyzer(_uiState.value.targetLanguage)
                    val analysis = analyzer.analyze(code)
                    _uiState.value = _uiState.value.copy(
                        prompt = defaultTemplate.prompt,
                        generatedCode = code,
                        formattedCode = analysis.formattedCode,
                        analysis = analysis
                    )
                }
            } catch (e: Throwable) {
                android.util.Log.e("GalaxyGuardianVM", "Error initializing default template", e)
            }
        }
    }

    fun onPromptChange(newPrompt: String) {
        _uiState.value = _uiState.value.copy(prompt = newPrompt, errorMessage = null)
    }

    fun setTargetLanguage(language: TargetLanguage) {
        val validTargets = language.supportedTargets()
        val currentTarget = _uiState.value.generationTarget
        val updatedTarget = if (validTargets.contains(currentTarget)) currentTarget else validTargets.first()

        _uiState.value = _uiState.value.copy(
            targetLanguage = language,
            generationTarget = updatedTarget,
            statusMessage = "Selected target language: ${language.displayName}"
        )
    }

    fun setGenerationTarget(target: GenerationTarget) {
        _uiState.value = _uiState.value.copy(
            generationTarget = target,
            statusMessage = "Target component set to: ${target.displayName}"
        )
    }

    fun setArchitectureProfile(profile: ArchitectureProfile) {
        _uiState.value = _uiState.value.copy(
            architectureProfile = profile,
            statusMessage = "Architecture profile: ${profile.displayName}"
        )
    }

    fun setTab(tab: ResultsTab) {
        _uiState.value = _uiState.value.copy(currentTab = tab)
    }

    fun setProviderType(provider: LlmProviderType) {
        _uiState.value = _uiState.value.copy(
            providerType = provider,
            statusMessage = "Switched to provider: ${provider.displayName}"
        )
    }

    fun setModel(model: String) {
        _uiState.value = _uiState.value.copy(selectedModel = model)
    }

    fun setOllamaBaseUrl(url: String) {
        _uiState.value = _uiState.value.copy(ollamaBaseUrl = url)
        encryptedCredentialStore.saveOllamaBaseUrl(url)
    }

    fun setOllamaModelName(name: String) {
        _uiState.value = _uiState.value.copy(ollamaModelName = name)
        encryptedCredentialStore.saveOllamaModelName(name)
    }

    fun setCustomApiKey(key: String) {
        _uiState.value = _uiState.value.copy(customApiKey = key)
        encryptedCredentialStore.saveCustomApiKey(key)
    }

    fun fetchOllamaModels() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(statusMessage = "Querying Ollama models from ${_uiState.value.ollamaBaseUrl}...")
            val models = ollamaGenerator.fetchAvailableModels(_uiState.value.ollamaBaseUrl)
            _availableOllamaModels.value = models
            if (models.isNotEmpty()) {
                _uiState.value = _uiState.value.copy(statusMessage = "Discovered ${models.size} Ollama models!")
            } else {
                _uiState.value = _uiState.value.copy(errorMessage = "Could not fetch Ollama models. Check server connection.")
            }
        }
    }

    fun toggleDiffView() {
        val currentTab = if (_uiState.value.currentTab == ResultsTab.DIFF_VIEW) ResultsTab.GENERATED_CODE else ResultsTab.DIFF_VIEW
        _uiState.value = _uiState.value.copy(currentTab = currentTab)
    }

    fun updatePersonality(
        name: String = _uiState.value.personality.name,
        tone: String = _uiState.value.personality.tone,
        traits: String = _uiState.value.personality.traits,
        systemInstructions: String = _uiState.value.personality.systemInstructions,
        negativeConstraints: String = _uiState.value.personality.negativeConstraints,
        responseStyle: String = _uiState.value.personality.responseStyle,
        creativityTemperature: Float = _uiState.value.personality.creativityTemperature,
        avatarType: String = _uiState.value.personality.avatarType,
        avatarValue: String = _uiState.value.personality.avatarValue,
        avatarColorIndex: Int = _uiState.value.personality.avatarColorIndex
    ): Boolean {
        if (traits.trim().isBlank() || systemInstructions.trim().isBlank() || name.trim().isBlank()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Validation error: Name, traits, and system instructions cannot be empty."
            )
            return false
        }

        val updated = _uiState.value.personality.copy(
            name = name.trim(),
            tone = tone.trim(),
            traits = traits.trim(),
            systemInstructions = systemInstructions.trim(),
            negativeConstraints = negativeConstraints.trim(),
            responseStyle = responseStyle.trim(),
            creativityTemperature = creativityTemperature,
            avatarType = avatarType,
            avatarValue = avatarValue,
            avatarColorIndex = avatarColorIndex
        )
        _uiState.value = _uiState.value.copy(
            personality = updated,
            statusMessage = "Saved persona to DataStore: ${updated.name}",
            errorMessage = null
        )
        viewModelScope.launch {
            personalityDataStore.savePersonality(updated)
        }
        return true
    }

    fun updateAvatar(avatarType: String, avatarValue: String, avatarColorIndex: Int) {
        val updated = _uiState.value.personality.copy(
            avatarType = avatarType,
            avatarValue = avatarValue,
            avatarColorIndex = avatarColorIndex
        )
        _uiState.value = _uiState.value.copy(personality = updated)
        viewModelScope.launch {
            personalityDataStore.savePersonality(updated)
        }
    }

    fun applyPersonalityPreset(preset: BotPersonality) {
        _uiState.value = _uiState.value.copy(
            personality = preset,
            statusMessage = "Applied preset persona: ${preset.name}"
        )
        viewModelScope.launch {
            personalityDataStore.savePersonality(preset)
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
        viewModelScope.launch {
            personalityDataStore.saveThemeMode(mode)
        }
    }

    fun resetPersonalityToDefaults() {
        val defaultPersona = BotPersonality()
        _uiState.value = _uiState.value.copy(
            personality = defaultPersona,
            statusMessage = "Reset configuration to defaults"
        )
        viewModelScope.launch {
            personalityDataStore.clearPersonality()
        }
    }

    fun exportPersonalityAsJson(personality: BotPersonality = _uiState.value.personality): String {
        val traitsList = personality.traits.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        val traitsJsonArray = traitsList.joinToString(separator = ", ") { "\"${escapeJson(it)}\"" }

        return """{
  "schema_version": "1.0",
  "bot_config": {
    "id": "${escapeJson(personality.id)}",
    "name": "${escapeJson(personality.name)}",
    "tone": "${escapeJson(personality.tone)}",
    "traits": [${traitsJsonArray}],
    "traits_raw": "${escapeJson(personality.traits)}",
    "system_instructions": "${escapeJson(personality.systemInstructions)}",
    "negative_constraints": "${escapeJson(personality.negativeConstraints)}",
    "response_style": "${escapeJson(personality.responseStyle)}",
    "creativity_temperature": ${personality.creativityTemperature},
    "avatar": {
      "type": "${escapeJson(personality.avatarType)}",
      "value": "${escapeJson(personality.avatarValue)}",
      "color_index": ${personality.avatarColorIndex}
    }
  },
  "metadata": {
    "app": "Galaxy Guardian",
    "exported_timestamp": ${System.currentTimeMillis()},
    "export_format": "JSON_SCHEMA_V1"
  }
}""".trimIndent()
    }

    private fun escapeJson(str: String): String {
        return str
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\b", "\\b")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
    }

    fun generateCode() {
        val currentPrompt = _uiState.value.prompt.trim()
        if (currentPrompt.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Please enter an idea for your project.")
            return
        }

        activeJob?.cancel()
        activeJob = viewModelScope.launch {
            val startTime = System.currentTimeMillis()
            _uiState.value = _uiState.value.copy(
                isGenerating = true,
                activeOperation = ActiveOperation.GENERATING,
                errorMessage = null,
                statusMessage = "Synthesizing Architecture [${_uiState.value.targetLanguage.displayName}] with '${_uiState.value.personality.name}' persona..."
            )

            try {
                val effectiveModel = if (_uiState.value.providerType == LlmProviderType.OLLAMA) {
                    _uiState.value.ollamaModelName
                } else {
                    _uiState.value.selectedModel
                }

                val effectiveBaseUrl = if (_uiState.value.providerType == LlmProviderType.OLLAMA) {
                    _uiState.value.ollamaBaseUrl
                } else {
                    "https://generativelanguage.googleapis.com"
                }

                val config = GenerationConfig(
                    providerType = _uiState.value.providerType,
                    modelName = effectiveModel,
                    baseUrl = effectiveBaseUrl,
                    apiKey = _uiState.value.customApiKey,
                    temperature = _uiState.value.personality.creativityTemperature
                )

                val req = GenerationRequest(
                    prompt = currentPrompt,
                    language = _uiState.value.targetLanguage,
                    target = _uiState.value.generationTarget,
                    architecture = _uiState.value.architectureProfile,
                    personality = _uiState.value.personality,
                    config = config
                )

                val result = codeGenerator.generateRequest(req)

                val (rawCode, statusText) = when (result) {
                    is GenerationResult.Success -> {
                        Pair(result.code, "Synthesized via ${result.providerUsed.displayName} in ${result.durationMs}ms")
                    }
                    is GenerationResult.Failure -> {
                        if (!result.fallbackCode.isNullOrBlank()) {
                            Pair(result.fallbackCode, "Notice: ${result.error}")
                        } else {
                            throw RuntimeException(result.error)
                        }
                    }
                }

                _uiState.value = _uiState.value.copy(
                    activeOperation = ActiveOperation.ANALYZING,
                    statusMessage = "Analyzing code quality & pattern security..."
                )
                val analyzer = AnalyzerRegistry.getAnalyzer(_uiState.value.targetLanguage)
                val analysis = analyzer.analyze(rawCode)

                val latency = System.currentTimeMillis() - startTime
                DiagnosticsLogger.logMetric(
                    GenerationMetric(
                        id = UUID.randomUUID().toString(),
                        provider = _uiState.value.providerType,
                        model = effectiveModel,
                        language = _uiState.value.targetLanguage,
                        latencyMs = latency,
                        securityScore = analysis.securityScore,
                        isSuccess = true
                    )
                )

                _uiState.value = _uiState.value.copy(
                    generatedCode = rawCode,
                    formattedCode = analysis.formattedCode,
                    originalCodeForDiff = rawCode,
                    analysis = analysis,
                    statusMessage = statusText,
                    currentTab = ResultsTab.GENERATED_CODE
                )
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Generation error: ${e.message}",
                    statusMessage = null
                )
            } finally {
                _uiState.value = _uiState.value.copy(
                    isGenerating = false,
                    activeOperation = ActiveOperation.IDLE
                )
            }
        }
    }

    fun improveAndFixCode() {
        val currentState = _uiState.value
        val analysis = currentState.analysis ?: return
        val currentCode = currentState.formattedCode.ifBlank { currentState.generatedCode }
        if (currentCode.isBlank()) return

        val findingsText = buildString {
            append("Analyze and fix the following findings in the code:\n")
            analysis.lintIssues.forEach { append("- [Style ${it.ruleCode}] Line ${it.line}: ${it.message}\n") }
            analysis.typeIssues.forEach { append("- [Type] Line ${it.line}: ${it.message}\n") }
            analysis.securityVulnerabilities.forEach { append("- [Security ${it.id}] Line ${it.line}: ${it.title} - ${it.description}\n") }
        }

        val repairPrompt = "Original request: '${currentState.prompt}'\n\nExisting code:\n```\n$currentCode\n```\n\n$findingsText\n\nOutput only the complete repaired code."

        activeJob?.cancel()
        activeJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isGenerating = true,
                activeOperation = ActiveOperation.GENERATING,
                statusMessage = "Self-repairing code findings..."
            )

            try {
                val config = GenerationConfig(
                    providerType = currentState.providerType,
                    modelName = if (currentState.providerType == LlmProviderType.OLLAMA) currentState.ollamaModelName else currentState.selectedModel,
                    baseUrl = if (currentState.providerType == LlmProviderType.OLLAMA) currentState.ollamaBaseUrl else "https://generativelanguage.googleapis.com",
                    apiKey = currentState.customApiKey,
                    temperature = 0.2f
                )

                val req = GenerationRequest(
                    prompt = repairPrompt,
                    language = currentState.targetLanguage,
                    target = currentState.generationTarget,
                    architecture = currentState.architectureProfile,
                    personality = currentState.personality,
                    config = config
                )

                val result = codeGenerator.generateRequest(req)
                if (result is GenerationResult.Success) {
                    val repairedCode = result.code
                    val analyzer = AnalyzerRegistry.getAnalyzer(currentState.targetLanguage)
                    val newAnalysis = analyzer.analyze(repairedCode)

                    val prevCount = analysis.securityVulnerabilities.size + analysis.lintIssues.size
                    val newCount = newAnalysis.securityVulnerabilities.size + newAnalysis.lintIssues.size
                    val fixedCount = (prevCount - newCount).coerceAtLeast(0)

                    _uiState.value = _uiState.value.copy(
                        originalCodeForDiff = currentCode,
                        generatedCode = repairedCode,
                        formattedCode = newAnalysis.formattedCode,
                        analysis = newAnalysis,
                        statusMessage = "Self-repair complete: $prevCount findings → $newCount remaining ($fixedCount fixed!)",
                        currentTab = ResultsTab.DIFF_VIEW
                    )
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _uiState.value = _uiState.value.copy(errorMessage = "Repair failed: ${e.message}")
            } finally {
                _uiState.value = _uiState.value.copy(
                    isGenerating = false,
                    activeOperation = ActiveOperation.IDLE
                )
            }
        }
    }

    fun generateTests() {
        val currentState = _uiState.value
        val currentCode = currentState.formattedCode.ifBlank { currentState.generatedCode }
        if (currentCode.isBlank()) return

        val testPrompt = "Generate unit tests for the following source code:\n```\n$currentCode\n```\nOutput complete unit test code."

        activeJob?.cancel()
        activeJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isGenerating = true,
                activeOperation = ActiveOperation.GENERATING,
                statusMessage = "Generating unit test suite..."
            )

            try {
                val config = GenerationConfig(
                    providerType = currentState.providerType,
                    modelName = if (currentState.providerType == LlmProviderType.OLLAMA) currentState.ollamaModelName else currentState.selectedModel,
                    baseUrl = if (currentState.providerType == LlmProviderType.OLLAMA) currentState.ollamaBaseUrl else "https://generativelanguage.googleapis.com",
                    apiKey = currentState.customApiKey,
                    temperature = 0.3f
                )

                val req = GenerationRequest(
                    prompt = testPrompt,
                    language = currentState.targetLanguage,
                    target = currentState.generationTarget,
                    architecture = currentState.architectureProfile,
                    personality = currentState.personality,
                    config = config
                )

                val result = codeGenerator.generateRequest(req)
                if (result is GenerationResult.Success) {
                    val testFileName = if (currentState.targetLanguage == TargetLanguage.KOTLIN_ANDROID) "\n// File: test/UnitTest.kt\n" else "\n# File: test_main.py\n"
                    val combinedCode = "$currentCode\n$testFileName${result.code}"

                    val analyzer = AnalyzerRegistry.getAnalyzer(currentState.targetLanguage)
                    val newAnalysis = analyzer.analyze(combinedCode)

                    _uiState.value = _uiState.value.copy(
                        generatedCode = combinedCode,
                        formattedCode = newAnalysis.formattedCode,
                        analysis = newAnalysis,
                        statusMessage = "Unit test suite generated and attached to project!",
                        currentTab = ResultsTab.GENERATED_CODE
                    )
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _uiState.value = _uiState.value.copy(errorMessage = "Test generation failed: ${e.message}")
            } finally {
                _uiState.value = _uiState.value.copy(
                    isGenerating = false,
                    activeOperation = ActiveOperation.IDLE
                )
            }
        }
    }

    fun setSimulationScenario(scenario: com.example.galaxyguardian.data.service.SimulationScenario) {
        _uiState.value = _uiState.value.copy(
            selectedScenario = scenario,
            statusMessage = "Scenario set: ${scenario.displayName}"
        )
    }

    fun executeCode() {
        val codeToRun = _uiState.value.formattedCode.ifBlank { _uiState.value.generatedCode }
        if (codeToRun.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "No code available to simulate!")
            return
        }

        activeJob?.cancel()
        activeJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isExecuting = true,
                activeOperation = ActiveOperation.SIMULATING,
                currentTab = ResultsTab.SIMULATION,
                statusMessage = "Launching simulator scenario [${_uiState.value.selectedScenario.displayName}]..."
            )

            try {
                val result = SimulatedExecutionEngine.simulateExecution(
                    codeString = codeToRun,
                    scenario = _uiState.value.selectedScenario
                )
                _uiState.value = _uiState.value.copy(
                    executionResult = result,
                    statusMessage = if (result.isSuccess) "Simulation completed successfully." else "Simulation finished with alerts."
                )
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Simulation error: ${e.message}",
                    statusMessage = null
                )
            } finally {
                _uiState.value = _uiState.value.copy(
                    isExecuting = false,
                    activeOperation = ActiveOperation.IDLE
                )
            }
        }
    }

    fun exportDiagnostics(): String {
        return DiagnosticsLogger.exportSanitizedDiagnostics(_uiState.value.analysis)
    }

    fun exportProjectZip(context: android.content.Context, destinationUri: Uri) {
        val currentState = _uiState.value
        val code = currentState.formattedCode.ifBlank { currentState.generatedCode }
        val project = BotProject(
            id = UUID.randomUUID().toString(),
            title = currentState.prompt.take(30).trim().capitalizeFirst(),
            prompt = currentState.prompt,
            targetLanguage = currentState.targetLanguage.persistenceId,
            generatedCode = currentState.generatedCode,
            formattedCode = currentState.formattedCode,
            analysis = currentState.analysis ?: AnalyzerRegistry.getAnalyzer(currentState.targetLanguage).analyze(code),
            providerType = currentState.providerType.name,
            modelName = currentState.selectedModel
        )

        val success = ProjectExporter.exportProjectZip(context, project, destinationUri)
        if (success) {
            _uiState.value = _uiState.value.copy(statusMessage = "Exported project archive to destination!")
        } else {
            _uiState.value = _uiState.value.copy(errorMessage = "Export to ZIP failed.")
        }
    }

    fun loadTemplate(template: BotTemplate) {
        val analyzer = AnalyzerRegistry.getAnalyzer(_uiState.value.targetLanguage)
        val analysis = analyzer.analyze(template.sampleCode)
        _uiState.value = _uiState.value.copy(
            prompt = template.prompt,
            generatedCode = template.sampleCode,
            formattedCode = analysis.formattedCode,
            analysis = analysis,
            executionResult = null,
            currentTab = ResultsTab.GENERATED_CODE,
            statusMessage = "Loaded preset: ${template.title}"
        )
    }

    fun loadBot(bot: BotProject) {
        val restoredLang = bot.toTargetLanguage()
        val restoredProvider = runCatching { LlmProviderType.valueOf(bot.providerType) }.getOrDefault(LlmProviderType.GEMINI)
        val restoredTarget = GenerationTarget.fromString(bot.generationTarget)
        val restoredArch = ArchitectureProfile.fromString(bot.architectureProfile)

        _uiState.value = _uiState.value.copy(
            prompt = bot.prompt,
            targetLanguage = restoredLang,
            generationTarget = restoredTarget,
            architectureProfile = restoredArch,
            providerType = restoredProvider,
            selectedModel = if (restoredProvider == LlmProviderType.GEMINI) bot.modelName else _uiState.value.selectedModel,
            ollamaModelName = if (restoredProvider == LlmProviderType.OLLAMA) bot.modelName else _uiState.value.ollamaModelName,
            generatedCode = bot.generatedCode,
            formattedCode = bot.formattedCode,
            analysis = bot.analysis,
            executionResult = bot.lastExecution,
            currentTab = ResultsTab.GENERATED_CODE,
            statusMessage = "Loaded project: ${bot.title} [${restoredLang.displayName}]"
        )
    }

    fun saveCurrentBot(customTitle: String? = null) {
        val currentState = _uiState.value
        val code = currentState.formattedCode.ifBlank { currentState.generatedCode }
        if (code.isBlank()) return

        val title = customTitle?.takeIf { it.isNotBlank() }
            ?: currentState.prompt.take(30).trim().capitalizeFirst()

        val project = BotProject(
            id = UUID.randomUUID().toString(),
            title = title,
            prompt = currentState.prompt,
            targetLanguage = currentState.targetLanguage.persistenceId,
            generatedCode = currentState.generatedCode,
            formattedCode = currentState.formattedCode,
            analysis = currentState.analysis ?: AnalyzerRegistry.getAnalyzer(currentState.targetLanguage).analyze(code),
            lastExecution = currentState.executionResult,
            providerType = currentState.providerType.name,
            modelName = if (currentState.providerType == LlmProviderType.OLLAMA) currentState.ollamaModelName else currentState.selectedModel,
            generationTarget = currentState.generationTarget.name,
            architectureProfile = currentState.architectureProfile.name
        )

        viewModelScope.launch {
            repository.saveBot(project)
            _uiState.value = _uiState.value.copy(statusMessage = "Saved '${title}' to project library!")
        }
    }

    fun deleteBot(botId: String) {
        viewModelScope.launch {
            repository.deleteBot(botId)
        }
    }

    fun toggleFavorite(botId: String) {
        viewModelScope.launch {
            repository.toggleFavorite(botId)
        }
    }

    fun clearStatusMessage() {
        _uiState.value = _uiState.value.copy(statusMessage = null, errorMessage = null)
    }

    private fun String.capitalizeFirst(): String =
        if (isNotEmpty()) substring(0, 1).uppercase() + substring(1) else this
}
