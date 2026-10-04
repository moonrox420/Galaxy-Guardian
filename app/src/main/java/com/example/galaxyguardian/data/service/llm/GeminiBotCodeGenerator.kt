package com.example.galaxyguardian.data.service.llm

import com.example.galaxyguardian.BuildConfig
import com.example.galaxyguardian.data.model.BotPersonality
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class GeminiBotCodeGenerator(
    private val fallbackGenerator: LocalTemplateGenerator = LocalTemplateGenerator()
) : BotCodeGenerator {

    private val client = OkHttpClient.Builder()
        .connectTimeout(45, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .writeTimeout(45, TimeUnit.SECONDS)
        .build()

    override suspend fun generate(
        prompt: String,
        targetLanguage: String,
        personality: BotPersonality?,
        config: GenerationConfig
    ): GenerationResult = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()

        val buildConfigKey = runCatching {
            val field = BuildConfig::class.java.getField("GEMINI_API_KEY")
            field.get(null) as? String
        }.getOrNull()
        val apiKey = config.apiKey.takeIf { it.isNotBlank() } ?: buildConfigKey?.takeIf { it.isNotBlank() }

        if (apiKey.isNullOrBlank()) {
            val fallback = fallbackGenerator.generate(prompt, targetLanguage, personality, config)
            val fallbackCode = if (fallback is GenerationResult.Success) fallback.code else ""
            return@withContext GenerationResult.Failure(
                error = "Gemini API key is not configured. Falling back to offline synthesis.",
                providerAttempted = LlmProviderType.GEMINI,
                fallbackCode = fallbackCode
            )
        }

        try {
            val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/${config.modelName}:generateContent"

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

            val requestJson = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", userPromptText)
                            })
                        })
                    })
                })
                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", systemInstructionText)
                        })
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("temperature", personality?.creativityTemperature?.toDouble() ?: config.temperature.toDouble())
                    put("topP", 0.95)
                    put("maxOutputTokens", 4096)
                })
            }

            val requestBody = requestJson.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url(endpoint)
                .addHeader("x-goog-api-key", apiKey)
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                val responseStr = response.body?.string() ?: ""

                if (!response.isSuccessful) {
                    val fallback = fallbackGenerator.generate(prompt, targetLanguage, personality, config)
                    val fallbackCode = if (fallback is GenerationResult.Success) fallback.code else null
                    return@withContext GenerationResult.Failure(
                        error = "Gemini API error (${response.code})",
                        providerAttempted = LlmProviderType.GEMINI,
                        fallbackCode = fallbackCode
                    )
                }

                val json = JSONObject(responseStr)
                val candidates = json.optJSONArray("candidates")
                val candidate = candidates?.optJSONObject(0)
                val content = candidate?.optJSONObject("content")
                val parts = content?.optJSONArray("parts")
                val rawText = parts?.optJSONObject(0)?.optString("text") ?: ""

                val cleanCode = CodeFenceExtractor.extractCleanCode(rawText, targetLanguage)
                val duration = System.currentTimeMillis() - startTime

                GenerationResult.Success(
                    code = cleanCode,
                    providerUsed = LlmProviderType.GEMINI,
                    modelUsed = config.modelName,
                    durationMs = duration
                )
            }
        } catch (e: IOException) {
            val fallback = fallbackGenerator.generate(prompt, targetLanguage, personality, config)
            val fallbackCode = if (fallback is GenerationResult.Success) fallback.code else null
            GenerationResult.Failure(
                error = "Network connection failure: ${e.message}",
                providerAttempted = LlmProviderType.GEMINI,
                fallbackCode = fallbackCode
            )
        } catch (e: Exception) {
            val fallback = fallbackGenerator.generate(prompt, targetLanguage, personality, config)
            val fallbackCode = if (fallback is GenerationResult.Success) fallback.code else null
            GenerationResult.Failure(
                error = "Generation failed: ${e.message}",
                providerAttempted = LlmProviderType.GEMINI,
                fallbackCode = fallbackCode
            )
        }
    }
}
