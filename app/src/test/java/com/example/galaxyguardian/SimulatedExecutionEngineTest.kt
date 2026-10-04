package com.example.galaxyguardian

import com.example.galaxyguardian.data.service.SimulatedExecutionEngine
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SimulatedExecutionEngineTest {

    @Test
    fun testSimulate_safeBotExecutesSuccessfully() = runBlocking {
        val safeBot = """
            import logging
            
            def main():
                print("Starting WeatherBot...")
                print("Simulation completed cleanly.")

            if __name__ == '__main__':
                main()
        """.trimIndent()

        val result = SimulatedExecutionEngine.simulateExecution(safeBot)

        assertTrue("Execution should be successful for safe bot", result.isSuccess)
        assertEquals(0, result.exitCode)
        assertTrue("Output should capture printed text", result.stdout.contains("Starting WeatherBot"))
    }

    @Test
    fun testSimulate_dangerousBotRejectsExecution() = runBlocking {
        val dangerousBot = """
            import os
            
            def wipe():
                os.system("rm -rf /")
        """.trimIndent()

        val result = SimulatedExecutionEngine.simulateExecution(dangerousBot)

        assertFalse("Dangerous bot execution must be refused", result.isSuccess)
        assertEquals(126, result.exitCode)
        assertTrue(
            "Output must explain security rejection reason",
            result.stderr.contains("SIMULATION REJECTED", ignoreCase = true)
        )
    }

    @Test
    fun testSimulate_emptyCodeHandledGracefully() = runBlocking {
        val result = SimulatedExecutionEngine.simulateExecution("")

        assertTrue("Empty code should finish simulation gracefully", result.isSuccess || result.exitCode == 0)
    }
}
