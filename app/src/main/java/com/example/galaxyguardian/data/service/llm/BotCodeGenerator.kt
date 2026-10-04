package com.example.galaxyguardian.data.service.llm

import com.example.galaxyguardian.data.model.BotPersonality

enum class LlmProviderType(val displayName: String, val defaultModel: String) {
    GEMINI("Google Gemini (Cloud)", "gemini-3.5-flash"),
    OLLAMA("Ollama (Local / Self-Hosted)", "qwen2.5-coder:7b"),
    OFFLINE_ONLY("Offline Baseline Synthesizer", "local-heuristics")
}

data class GenerationConfig(
    val providerType: LlmProviderType = LlmProviderType.GEMINI,
    val modelName: String = "gemini-3.5-flash",
    val baseUrl: String = "https://generativelanguage.googleapis.com",
    val apiKey: String = "",
    val timeoutSeconds: Int = 45,
    val temperature: Float = 0.3f
)

sealed interface GenerationResult {
    data class Success(
        val code: String,
        val providerUsed: LlmProviderType,
        val modelUsed: String,
        val durationMs: Long
    ) : GenerationResult

    data class Failure(
        val error: String,
        val providerAttempted: LlmProviderType,
        val fallbackCode: String? = null
    ) : GenerationResult
}

interface BotCodeGenerator {
    suspend fun generate(
        prompt: String,
        targetLanguage: String = "python",
        personality: BotPersonality? = null,
        config: GenerationConfig
    ): GenerationResult

    suspend fun generateRequest(
        request: com.example.galaxyguardian.data.model.GenerationRequest
    ): GenerationResult {
        val policy = PromptPolicyFactory.getPolicy(request.language)
        val systemInstruction = policy.buildSystemInstruction(request)
        val userPrompt = policy.buildUserPrompt(request)

        return generate(
            prompt = userPrompt,
            targetLanguage = request.language.persistenceId,
            personality = request.personality,
            config = request.config
        )
    }
}
