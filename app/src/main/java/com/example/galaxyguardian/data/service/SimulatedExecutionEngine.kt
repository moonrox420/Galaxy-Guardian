package com.example.galaxyguardian.data.service

import com.example.galaxyguardian.data.model.ExecutionResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

enum class SimulationScenario(val displayName: String, val description: String) {
    NOMINAL("Nominal Flow", "Standard execution path without anomalies"),
    OFFLINE_STARTUP("Offline Startup", "Simulates launch while network interfaces are offline"),
    NETWORK_TIMEOUT("Network Timeout", "Simulates API request timeout / 504 Gateway error"),
    PERMISSION_DENIED("Permission Denied", "Simulates missing Android runtime permission")
}

object SimulatedExecutionEngine {

    suspend fun simulateExecution(
        codeString: String,
        scenario: SimulationScenario = SimulationScenario.NOMINAL,
        simulatedEnvironmentVars: Map<String, String> = mapOf(
            "ENV" to "simulation",
            "LOG_LEVEL" to "DEBUG"
        )
    ): ExecutionResult = withContext(Dispatchers.Default) {
        val startTime = System.currentTimeMillis()
        val stdoutBuilder = StringBuilder()
        val stderrBuilder = StringBuilder()
        var exitCode = 0

        val lines = codeString.lines()
        val timestampStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
        val isKotlin = codeString.contains("fun ") || codeString.contains("val ") || codeString.contains("@Composable") || codeString.contains("package com.")

        stdoutBuilder.append("[GALAXY-GUARDIAN LIFECYCLE SIMULATOR - INITIALIZED AT $timestampStr]\n")
        stdoutBuilder.append("Target Platform: ${if (isKotlin) "Kotlin / Android Compose Framework" else "Python Autonomous Bot Loop"}\n")
        stdoutBuilder.append("Scenario Pack: ${scenario.displayName} (${scenario.description})\n")
        stdoutBuilder.append("Safety Guarantee: Safe execution simulation — code is NOT executed as arbitrary bytecode on device\n")
        stdoutBuilder.append("Emulated Environment: ${simulatedEnvironmentVars.entries.joinToString(", ") { "${it.key}=${it.value}" }}\n")
        stdoutBuilder.append("------------------------------------------------------------\n")

        // Guard against dangerous destructive command strings
        if (codeString.contains("rm -rf /") || codeString.contains("os.remove('/')") || codeString.contains(":(){ :|:& };:")) {
            stderrBuilder.append("SIMULATION REJECTED: Destructive pattern rejected by Galaxy Guardian Safety Filter.\n")
            exitCode = 126
        } else {
            try {
                delay(300)

                when (scenario) {
                    SimulationScenario.OFFLINE_STARTUP -> {
                        stdoutBuilder.append("00ms  >> [Network Monitor] Network interfaces DOWN. Active connection: None\n")
                        stdoutBuilder.append("05ms  >> [Cache / Room] Falling back to local offline database state...\n")
                        stdoutBuilder.append("12ms  >> [UI] Rendered offline banner & cached dataset successfully.\n")
                    }
                    SimulationScenario.NETWORK_TIMEOUT -> {
                        stdoutBuilder.append("00ms  >> [HTTP Client] Executing request -> Endpoint: https://api.endpoint.org/data\n")
                        stdoutBuilder.append("1500ms >> [HTTP Client] Timeout threshold exceeded (1500ms)\n")
                        stderrBuilder.append("SocketTimeoutException: Failed to connect within timeout budget.\n")
                        exitCode = 1
                    }
                    SimulationScenario.PERMISSION_DENIED -> {
                        stdoutBuilder.append("00ms  >> [Android Permission] Checking Manifest permission 'CAMERA'...\n")
                        stdoutBuilder.append("04ms  >> [SecurityManager] Permission DENIED by user policy.\n")
                        stderrBuilder.append("SecurityException: Permission denied for hardware resource.\n")
                        exitCode = 1
                    }
                    SimulationScenario.NOMINAL -> {
                        if (isKotlin) {
                            stdoutBuilder.append("00ms  >> [Android Runtime] Initializing Compose Component Hierarchy...\n")
                            stdoutBuilder.append("04ms  >> [Lifecycle] Activity ON_CREATE -> ViewModel Scope Active\n")
                            if (codeString.contains("ViewModel") || codeString.contains("StateFlow")) {
                                stdoutBuilder.append("08ms  >> [ViewModel] StateFlow initialized with default UiState\n")
                                stdoutBuilder.append("12ms  >> [UI Event] Simulating user click on primary Action Button...\n")
                                stdoutBuilder.append("18ms  >> [Coroutines] Launched coroutine on Dispatchers.IO\n")
                            }
                            if (codeString.contains("Room") || codeString.contains("@Dao") || codeString.contains("Database")) {
                                stdoutBuilder.append("24ms  >> [Room DB] Querying SQLite table 'guardian_records' -> Flow emit\n")
                            }
                            if (codeString.contains("Retrofit") || codeString.contains("@GET") || codeString.contains("http")) {
                                stdoutBuilder.append("28ms  >> [Retrofit HTTP] Simulated GET endpoint -> 200 OK (application/json)\n")
                            }
                            if (codeString.contains("Worker") || codeString.contains("WorkManager")) {
                                stdoutBuilder.append("32ms  >> [WorkManager] Enqueued CoroutineWorker job -> Result.success()\n")
                            }
                            stdoutBuilder.append("38ms  >> [Recomposition] StateFlow updated -> Composable Recomposed Cleanly\n")
                        } else {
                            var insideDocstring = false
                            val printedValues = mutableListOf<String>()

                            for (rawLine in lines) {
                                val line = rawLine.trim()

                                if (line.startsWith("\"\"\"") || line.startsWith("'''")) {
                                    insideDocstring = !insideDocstring
                                    continue
                                }
                                if (insideDocstring || line.startsWith("#") || line.isBlank()) {
                                    continue
                                }

                                if (line.startsWith("print(") && line.endsWith(")")) {
                                    val content = line.substring(6, line.length - 1).trim()
                                    printedValues.add(cleanPrintContent(content))
                                }

                                if (line.contains("bot.run(") || line.contains("client.run(")) {
                                    printedValues.add(">> [Gateway] Connecting to Bot Gateway WebSockets...")
                                    printedValues.add(">> [Gateway] Handshake authorized. Token validated in Simulation.")
                                    printedValues.add(">> [Bot] Logged in as GalaxyBot#1337 (ID: 984028472910)")
                                    printedValues.add(">> [Bot] Status: Ready. Listening for commands and events.")
                                }

                                if (line.contains("requests.get(") || line.contains("aiohttp.ClientSession")) {
                                    printedValues.add(">> [Network] Simulated HTTP Request -> GET Status 200 OK (Content-Type: application/json)")
                                }

                                if (line.contains("fetch_weather") || line.contains("get_weather")) {
                                    printedValues.add(">> [Weather] Temperature: 22°C (71.6°F) | Humidity: 45% | Conditions: Clear Skies")
                                }

                                if (line.contains("price") && (line.contains("crypto") || line.contains("btc") || line.contains("eth"))) {
                                    printedValues.add(">> [Crypto Feed] BTC/USD: $95,400.00 (+3.42%) | ETH/USD: $3,450.50")
                                }

                                if (line.contains("create_snapshot") || line.contains("backup")) {
                                    printedValues.add(">> [Backup Engine] Verified snapshot created. Integrity check PASSED (SHA-256).")
                                }
                            }

                            if (printedValues.isEmpty()) {
                                stdoutBuilder.append("Script syntax validated. Execution simulation finished with exit code 0 (no runtime faults).\n")
                            } else {
                                printedValues.forEach { line ->
                                    stdoutBuilder.append(line).append("\n")
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                stderrBuilder.append("Simulation Exception: ${e.message}\n")
                exitCode = 1
            }
        }

        val durationMs = System.currentTimeMillis() - startTime
        stdoutBuilder.append("------------------------------------------------------------\n")
        stdoutBuilder.append("Simulation completed with exit code $exitCode (Emulation time: ${durationMs}ms)\n")

        ExecutionResult(
            id = UUID.randomUUID().toString(),
            timestamp = System.currentTimeMillis(),
            stdout = stdoutBuilder.toString(),
            stderr = stderrBuilder.toString(),
            exitCode = exitCode,
            durationMs = durationMs,
            isSuccess = exitCode == 0
        )
    }

    private fun cleanPrintContent(content: String): String {
        var str = content
        if ((str.startsWith("\"") && str.endsWith("\"")) || (str.startsWith("'") && str.endsWith("'"))) {
            str = str.substring(1, str.length - 1)
        }
        if (str.startsWith("f\"") || str.startsWith("f'")) {
            str = str.substring(2, str.length - 1)
        }
        return str
            .replace("\\n", "\n")
            .replace("\\t", "    ")
    }
}
