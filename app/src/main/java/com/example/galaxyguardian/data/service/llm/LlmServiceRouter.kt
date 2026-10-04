package com.example.galaxyguardian.data.service.llm

import com.example.galaxyguardian.data.model.BotPersonality

class LlmServiceRouter(
    private val localTemplateGenerator: LocalTemplateGenerator = LocalTemplateGenerator(),
    private val geminiGenerator: GeminiBotCodeGenerator = GeminiBotCodeGenerator(localTemplateGenerator),
    private val ollamaGenerator: OllamaBotCodeGenerator = OllamaBotCodeGenerator(localTemplateGenerator)
) : BotCodeGenerator {

    override suspend fun generate(
        prompt: String,
        targetLanguage: String,
        personality: BotPersonality?,
        config: GenerationConfig
    ): GenerationResult {
        return when (config.providerType) {
            LlmProviderType.GEMINI -> {
                geminiGenerator.generate(prompt, targetLanguage, personality, config)
            }
            LlmProviderType.OLLAMA -> {
                ollamaGenerator.generate(prompt, targetLanguage, personality, config)
            }
            LlmProviderType.OFFLINE_ONLY -> {
                localTemplateGenerator.generate(prompt, targetLanguage, personality, config)
            }
        }
    }
}
