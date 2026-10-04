package com.example.galaxyguardian.data.service.llm

object CodeFenceExtractor {

    fun extractCleanCode(text: String, expectedLanguageName: String? = null): String {
        var cleaned = text.trim()

        if (cleaned.startsWith("```")) {
            val firstLineEnd = cleaned.indexOf('\n')
            if (firstLineEnd != -1) {
                cleaned = cleaned.substring(firstLineEnd + 1).trim()
            } else {
                cleaned = cleaned.removePrefix("```").trim()
            }
        }

        if (cleaned.endsWith("```")) {
            val lastFenceIndex = cleaned.lastIndexOf("```")
            if (lastFenceIndex != -1) {
                cleaned = cleaned.substring(0, lastFenceIndex).trim()
            }
        }

        return cleaned
    }
}
