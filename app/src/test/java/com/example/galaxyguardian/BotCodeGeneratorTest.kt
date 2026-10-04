package com.example.galaxyguardian

import com.example.galaxyguardian.data.model.BotPersonality
import com.example.galaxyguardian.data.service.llm.GenerationConfig
import com.example.galaxyguardian.data.service.llm.LlmProviderType
import com.example.galaxyguardian.data.service.llm.LocalTemplateGenerator
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BotCodeGeneratorTest {

    private val localGenerator = LocalTemplateGenerator()

    private val defaultPersonality = BotPersonality(
        name = "Cyberpunk Sentinel",
        tone = "Sharp, defensive, vigilant",
        systemInstructions = "Ensure all endpoints are verified and memory leaks are avoided."
    )

    @Test
    fun testLocalTemplate_weatherDomainMatches() = runBlocking {
        val config = GenerationConfig(
            modelName = "gemini-1.5-pro",
            temperature = 0.7f,
            apiKey = "",
            providerType = LlmProviderType.OFFLINE_ONLY
        )
        val result = localGenerator.generate(
            prompt = "A bot that fetches weather forecasts for cities",
            targetLanguage = "python",
            personality = defaultPersonality,
            config = config
        )

        assertTrue("Weather bot should succeed", result is com.example.galaxyguardian.data.service.llm.GenerationResult.Success)
        val success = result as com.example.galaxyguardian.data.service.llm.GenerationResult.Success
        assertNotNull(success.code)
        assertTrue("Weather code should contain WeatherGuardianBot", success.code.contains("WeatherGuardianBot"))
        assertFalse("Generated code must not contain placeholders", success.code.contains("TODO"))
    }
}
