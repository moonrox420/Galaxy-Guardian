package com.example.galaxyguardian.data.service.llm

import com.example.galaxyguardian.data.model.BotPersonality
import com.example.galaxyguardian.data.model.GenerationRequest
import com.example.galaxyguardian.data.model.TargetLanguage

interface GenerationPromptPolicy {
    fun buildSystemInstruction(request: GenerationRequest): String
    fun buildUserPrompt(request: GenerationRequest): String
}

class PythonPromptPolicy : GenerationPromptPolicy {

    override fun buildSystemInstruction(request: GenerationRequest): String {
        val personality: BotPersonality? = request.personality
        return buildString {
            append("You are Galaxy Guardian, an expert secure Python architect.\n")
            if (personality != null) {
                append("Designation: ${personality.name}\n")
                append("Tone: ${personality.tone}\n")
                append("Traits: ${personality.traits}\n")
                append("Directives: ${personality.systemInstructions}\n")
                if (personality.negativeConstraints.isNotBlank()) {
                    append("Strict Constraints: ${personality.negativeConstraints}\n")
                }
                append("Output Style: ${personality.responseStyle}\n")
            }
            append("Generate clean, production-ready, PEP 8 conformant Python source code for the user's request.\n")
            append("Include type annotations, comprehensive docstrings, and defensive error handling.")
        }
    }

    override fun buildUserPrompt(request: GenerationRequest): String {
        return "Generate complete Python code for: ${request.prompt}"
    }
}

class KotlinAndroidPromptPolicy : GenerationPromptPolicy {

    override fun buildSystemInstruction(request: GenerationRequest): String {
        val personality: BotPersonality? = request.personality
        return buildString {
            append("You are Galaxy Guardian, an expert Android AI architect specializing in idiomatic Kotlin, Jetpack Compose, Material 3, and modern Android architecture.\n")
            if (personality != null) {
                append("Persona: ${personality.name}\n")
                append("Tone: ${personality.tone}\n")
                append("Traits: ${personality.traits}\n")
                append("Design Priorities: ${personality.systemInstructions}\n")
                if (personality.negativeConstraints.isNotBlank()) {
                    append("Strict Constraints: ${personality.negativeConstraints}\n")
                }
            }
            append("Architecture Target: ${request.architecture.displayName} | Generation Target: ${request.target.displayName}\n")
            append("Requirements for Kotlin/Android generation:\n")
            append("- Use idiomatic Kotlin with explicit type safety and immutable data structures.\n")
            append("- Use Jetpack Compose and Material 3 for UI components.\n")
            append("- Use ViewModel, StateFlow, and Coroutines for state and concurrency.\n")
            append("- Avoid GlobalScope, unnecessary double-bang (!!), and Context leaks.\n")
            append("- Format multi-file output with clear file paths, e.g. `// File: ui/HomeScreen.kt`.\n")
            append("- Include clean, defensive exception handling and lifecycle awareness.")
        }
    }

    override fun buildUserPrompt(request: GenerationRequest): String {
        return "Generate complete, idiomatic Kotlin / Android code for target [${request.target.displayName}] under [${request.architecture.displayName}] architecture for request: ${request.prompt}"
    }
}

object PromptPolicyFactory {
    fun getPolicy(language: TargetLanguage): GenerationPromptPolicy {
        return when (language) {
            TargetLanguage.KOTLIN_ANDROID -> KotlinAndroidPromptPolicy()
            TargetLanguage.PYTHON -> PythonPromptPolicy()
        }
    }
}
