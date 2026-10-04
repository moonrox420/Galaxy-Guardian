package com.example.galaxyguardian.data.service.llm

import com.example.galaxyguardian.data.model.BotPersonality
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class OllamaBotCodeGenerator(
    private val fallbackGenerator: LocalTemplateGenerator = LocalTemplateGenerator()
) : BotCodeGenerator {

    private val client = OkHttpClient.Builder()
        .connectTimeout(90, TimeUnit.SECONDS)
        .readTimeout(90, TimeUnit.SECONDS)
        .writeTimeout(90, TimeUnit.SECONDS)
        .build()

    override suspend fun generate(
        prompt: String,
        targetLanguage: String,
        personality: BotPersonality?,
        config: GenerationConfig
    ): GenerationResult = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        val rawBaseUrl = config.baseUrl.trim().removeSuffix("/")
        val endpoint = "$rawBaseUrl/api/generate"

        val targetLangEnum = com.example.galaxyguardian.data.model.TargetLanguage.fromPersistenceId(targetLanguage)
        val policy = PromptPolicyFactory.getPolicy(targetLangEnum)
        val reqModel = com.example.galaxyguardian.data.model.GenerationRequest(
            prompt = prompt,
            language = targetLangEnum,
            personality = personality,
            config = config
        )
        val systemInstructionText = policy.buildSystemInstruction(reqModel)
        val userPromptText = policy.buildUserPrompt(reqModel)

        try {
            val optionsJson = JSONObject().apply {
                put("temperature", personality?.creativityTemperature?.toDouble() ?: config.temperature.toDouble())
                put("top_p", 0.95)
            }

            val requestJson = JSONObject().apply {
                put("model", config.modelName)
                put("prompt", userPromptText)
                put("system", systemInstructionText)
                put("stream", false)
                put("options", optionsJson)
            }

            val requestBody = requestJson.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url(endpoint)
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                val responseStr = response.body?.string() ?: ""

                if (!response.isSuccessful) {
                    val fallback = fallbackGenerator.generate(prompt, targetLanguage, personality, config)
                    val fallbackCode = if (fallback is GenerationResult.Success) fallback.code else null
                    return@withContext GenerationResult.Failure(
                        error = "Ollama returned HTTP ${response.code}: $responseStr",
                        providerAttempted = LlmProviderType.OLLAMA,
                        fallbackCode = fallbackCode
                    )
                }

                val json = JSONObject(responseStr)
                val rawText = json.optString("response", "")
                val cleanCode = CodeFenceExtractor.extractCleanCode(rawText, targetLanguage)
                val duration = System.currentTimeMillis() - startTime

                GenerationResult.Success(
                    code = cleanCode,
                    providerUsed = LlmProviderType.OLLAMA,
                    modelUsed = config.modelName,
                    durationMs = duration
                )
            }
        } catch (e: IOException) {
            val fallback = fallbackGenerator.generate(prompt, targetLanguage, personality, config)
            val fallbackCode = if (fallback is GenerationResult.Success) fallback.code else null
            GenerationResult.Failure(
                error = "Cannot connect to Ollama at $rawBaseUrl. Verify that Ollama is running and accessible: ${e.message}",
                providerAttempted = LlmProviderType.OLLAMA,
                fallbackCode = fallbackCode
            )
        } catch (e: Exception) {
            val fallback = fallbackGenerator.generate(prompt, targetLanguage, personality, config)
            val fallbackCode = if (fallback is GenerationResult.Success) fallback.code else null
            GenerationResult.Failure(
                error = "Ollama generation error: ${e.message}",
                providerAttempted = LlmProviderType.OLLAMA,
                fallbackCode = fallbackCode
            )
        }
    }

    suspend fun fetchAvailableModels(baseUrl: String): List<String> = withContext(Dispatchers.IO) {
        val rawBaseUrl = baseUrl.trim().removeSuffix("/")
        val endpoint = "$rawBaseUrl/api/tags"

        try {
            val request = Request.Builder().url(endpoint).get().build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext emptyList()
                val body = response.body?.string() ?: return@withContext emptyList()
                val json = JSONObject(body)
                val modelsArr = json.optJSONArray("models") ?: return@withContext emptyList()
                val list = mutableListOf<String>()
                for (i in 0 until modelsArr.length()) {
                    val m = modelsArr.getJSONObject(i)
                    val name = m.optString("name", "")
                    if (name.isNotBlank()) list.add(name)
                }
                list
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
