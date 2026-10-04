package com.example.galaxyguardian

import com.example.galaxyguardian.data.model.ArchitectureProfile
import com.example.galaxyguardian.data.model.GenerationRequest
import com.example.galaxyguardian.data.model.GenerationTarget
import com.example.galaxyguardian.data.model.TargetLanguage
import com.example.galaxyguardian.data.service.llm.PromptPolicyFactory
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PromptPolicyTest {

    @Test
    fun testKotlinPolicy_containsNoPythonTerms() {
        val policy = PromptPolicyFactory.getPolicy(TargetLanguage.KOTLIN_ANDROID)
        val req = GenerationRequest(
            prompt = "Build a habit tracker feature",
            language = TargetLanguage.KOTLIN_ANDROID,
            target = GenerationTarget.ANDROID_FEATURE,
            architecture = ArchitectureProfile.MVVM
        )

        val systemPrompt = policy.buildSystemInstruction(req)

        assertFalse("Kotlin prompt should not mention PEP 8", systemPrompt.contains("PEP 8"))
        assertFalse("Kotlin prompt should not mention asyncio", systemPrompt.contains("asyncio"))
        assertTrue("Kotlin prompt should mention Jetpack Compose", systemPrompt.contains("Jetpack Compose"))
        assertTrue("Kotlin prompt should mention ViewModel", systemPrompt.contains("ViewModel"))
    }

    @Test
    fun testPythonPolicy_containsPEP8Requirements() {
        val policy = PromptPolicyFactory.getPolicy(TargetLanguage.PYTHON)
        val req = GenerationRequest(
            prompt = "Build a weather bot",
            language = TargetLanguage.PYTHON,
            target = GenerationTarget.BOT
        )

        val systemPrompt = policy.buildSystemInstruction(req)

        assertTrue("Python prompt should mention PEP 8", systemPrompt.contains("PEP 8"))
    }
}
