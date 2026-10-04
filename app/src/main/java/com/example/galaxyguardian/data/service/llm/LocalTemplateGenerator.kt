package com.example.galaxyguardian.data.service.llm

import com.example.galaxyguardian.data.model.BotPersonality
import com.example.galaxyguardian.data.model.TargetLanguage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LocalTemplateGenerator : BotCodeGenerator {

    override suspend fun generate(
        prompt: String,
        targetLanguage: String,
        personality: BotPersonality?,
        config: GenerationConfig
    ): GenerationResult = withContext(Dispatchers.Default) {
        val startTime = System.currentTimeMillis()
        val lowerPrompt = prompt.lowercase()
        val lang = TargetLanguage.fromPersistenceId(targetLanguage)

        val generatedCode = if (lang == TargetLanguage.KOTLIN_ANDROID) {
            generateKotlinTemplate(lowerPrompt, prompt, personality)
        } else {
            generatePythonTemplate(lowerPrompt, prompt, personality)
        }

        val duration = System.currentTimeMillis() - startTime
        GenerationResult.Success(
            code = generatedCode,
            providerUsed = LlmProviderType.OFFLINE_ONLY,
            modelUsed = "local-template-v3",
            durationMs = duration
        )
    }

    private fun generateKotlinTemplate(lowerPrompt: String, rawPrompt: String, personality: BotPersonality?): String {
        return when {
            lowerPrompt.contains("viewmodel") || lowerPrompt.contains("state") -> {
                generateKotlinViewModelFeature(rawPrompt)
            }
            lowerPrompt.contains("room") || lowerPrompt.contains("db") || lowerPrompt.contains("database") || lowerPrompt.contains("entity") -> {
                generateKotlinRoomFeature(rawPrompt)
            }
            lowerPrompt.contains("network") || lowerPrompt.contains("retrofit") || lowerPrompt.contains("api") || lowerPrompt.contains("weather") -> {
                generateKotlinRetrofitFeature(rawPrompt)
            }
            lowerPrompt.contains("worker") || lowerPrompt.contains("background") || lowerPrompt.contains("job") -> {
                generateKotlinWorkManagerFeature(rawPrompt)
            }
            else -> {
                generateKotlinComposeScreen(rawPrompt)
            }
        }
    }

    private fun generateKotlinComposeScreen(prompt: String): String = """
// File: ui/GuardianFeatureScreen.kt
package com.example.galaxyguardian.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

data class GuardianUiState(
    val title: String = "Galaxy Guardian Android Feature",
    val promptText: String = "$prompt",
    val isLoading: Boolean = false,
    val items: List<String> = listOf("Item Alpha", "Item Beta", "Item Gamma")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuardianFeatureScreen(
    state: GuardianUiState = GuardianUiState(),
    onActionClicked: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = state.title) }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Prompt: " + state.promptText,
                style = MaterialTheme.typography.bodyMedium
            )

            Button(
                onClick = onActionClicked,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Execute Action")
            }

            Spacer(modifier = Modifier.height(16.dp))

            state.items.forEach { item ->
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = item,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }
    }
}
""".trimIndent()

    private fun generateKotlinViewModelFeature(prompt: String): String = """
// File: ui/GuardianViewModel.kt
package com.example.galaxyguardian.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class FeatureState(
    val query: String = "$prompt",
    val isLoading: Boolean = false,
    val data: List<String> = emptyList(),
    val errorMessage: String? = null
)

class GuardianViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(FeatureState())
    val uiState: StateFlow<FeatureState> = _uiState.asStateFlow()

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                // Simulated repository call
                val result = listOf("Result 1 for $prompt", "Result 2", "Result 3")
                _uiState.value = _uiState.value.copy(isLoading = false, data = result)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = e.message)
            }
        }
    }
}
""".trimIndent()

    private fun generateKotlinRoomFeature(prompt: String): String = """
// File: data/GuardianDatabase.kt
package com.example.galaxyguardian.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "guardian_records")
data class GuardianRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val prompt: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Dao
interface GuardianRecordDao {
    @Query("SELECT * FROM guardian_records ORDER BY timestamp DESC")
    fun getAllRecords(): Flow<List<GuardianRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: GuardianRecordEntity)

    @Delete
    suspend fun deleteRecord(record: GuardianRecordEntity)
}

@Database(entities = [GuardianRecordEntity::class], version = 1, exportSchema = false)
abstract class GuardianAppDatabase : RoomDatabase() {
    abstract fun recordDao(): GuardianRecordDao
}
""".trimIndent()

    private fun generateKotlinRetrofitFeature(prompt: String): String = """
// File: network/GuardianApiService.kt
package com.example.galaxyguardian.network

import retrofit2.http.GET
import retrofit2.http.Query

data class ApiResponseDto(
    val status: String,
    val query: String,
    val results: List<String>
)

interface GuardianApiService {
    @GET("v1/query")
    suspend fun executeQuery(
        @Query("q") query: String
    ): ApiResponseDto
}
""".trimIndent()

    private fun generateKotlinWorkManagerFeature(prompt: String): String = """
// File: worker/GuardianSyncWorker.kt
package com.example.galaxyguardian.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class GuardianSyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            // Background sync logic
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
""".trimIndent()

    private fun generatePythonTemplate(lowerPrompt: String, rawPrompt: String, personality: BotPersonality?): String {
        val personaHeader = if (personality != null) {
            buildString {
                append("\"\"\"\n")
                append("Bot Persona Designation: ${personality.name}\n")
                append("Tone & Demeanor: ${personality.tone}\n")
                append("Core Traits: ${personality.traits}\n")
                append("Directives: ${personality.systemInstructions}\n")
                if (personality.negativeConstraints.isNotBlank()) {
                    append("Strict Negative Constraints: ${personality.negativeConstraints}\n")
                }
                append("Style: ${personality.responseStyle}\n")
                append("\"\"\"\n\n")
            }
        } else ""

        return when {
            lowerPrompt.contains("weather") || lowerPrompt.contains("forecast") -> generateWeatherBot(personaHeader)
            lowerPrompt.contains("discord") || lowerPrompt.contains("moder") -> generateDiscordAutoModBot(personaHeader)
            lowerPrompt.contains("crypto") || lowerPrompt.contains("price") -> generateCryptoTrackerBot(personaHeader)
            lowerPrompt.contains("github") || lowerPrompt.contains("webhook") -> generateGitHubSentinelBot(personaHeader)
            lowerPrompt.contains("backup") || lowerPrompt.contains("database") -> generateDatabaseBackupBot(personaHeader)
            lowerPrompt.contains("iot") || lowerPrompt.contains("sensor") -> generateIoTSensorBot(personaHeader)
            lowerPrompt.contains("kubernetes") || lowerPrompt.contains("k8s") -> generateDevOpsK8sBot(personaHeader)
            else -> generateGenericAutonomousBot(rawPrompt, personaHeader)
        }
    }

    private fun generateWeatherBot(header: String): String = header + """
import os
from typing import Dict, Any, Optional

class WeatherGuardianBot:
    def __init__(self, api_key: Optional[str] = None) -> None:
        self.api_key: str = api_key or os.environ.get("OPENWEATHER_API_KEY", "DEMO_KEY_SANDBOX")

    def fetch_weather(self, city_name: str) -> Dict[str, Any]:
        print(f">> [WeatherGuardian] Fetching weather forecast for: {city_name}...")
        return {"city": city_name, "temperature_celsius": 22.4, "condition": "Partly Cloudy"}

def main() -> None:
    bot = WeatherGuardianBot()
    res = bot.fetch_weather("Neo Tokyo")
    print(res)

if __name__ == "__main__":
    main()
""".trimIndent()

    private fun generateDiscordAutoModBot(header: String): String = header + """
import os
from typing import List, Dict, Any

class DiscordAutoModBot:
    def __init__(self) -> None:
        self.prohibited_keywords: List[str] = ["phishing", "scam", "free_nitro"]

    def evaluate_message(self, author: str, content: str) -> Dict[str, Any]:
        print(f">> [AutoMod] Scanning message from @{author}: '{content}'")
        return {"status": "SAFE", "action": "ALLOW"}

if __name__ == "__main__":
    bot = DiscordAutoModBot()
    bot.evaluate_message("User", "Hello crew!")
""".trimIndent()

    private fun generateCryptoTrackerBot(header: String): String = header + """
from typing import Dict

class CryptoTrackerBot:
    def poll_prices(self) -> Dict[str, float]:
        print(">> [CryptoTracker] Polling spot market feeds...")
        return {"BTC": 95400.00, "ETH": 3450.50}

if __name__ == "__main__":
    bot = CryptoTrackerBot()
    bot.poll_prices()
""".trimIndent()

    private fun generateGitHubSentinelBot(header: String): String = header + """
from typing import Dict, Any

class GitHubSentinelBot:
    def process_webhook_event(self, event_type: str, payload: Dict[str, Any]) -> Dict[str, Any]:
        print(f">> [GitHubSentinel] Processing '{event_type}' event...")
        return {"status": "SUCCESS"}

if __name__ == "__main__":
    bot = GitHubSentinelBot()
    bot.process_webhook_event("pull_request", {})
""".trimIndent()

    private fun generateDatabaseBackupBot(header: String): String = header + """
import time
from typing import Dict, Any

class DatabaseBackupBot:
    def create_snapshot(self) -> Dict[str, Any]:
        print(">> [BackupBot] Initiating snapshot...")
        return {"filename": "backup.bak", "status": "VERIFIED"}

if __name__ == "__main__":
    bot = DatabaseBackupBot()
    bot.create_snapshot()
""".trimIndent()

    private fun generateIoTSensorBot(header: String): String = header + """
from typing import Dict, Any

class IoTSensorMonitorBot:
    def poll_telemetry(self) -> Dict[str, Any]:
        print(">> [IoTSensor] Polling sensor unit-42...")
        return {"temperature": 72.5, "status": "NOMINAL"}

if __name__ == "__main__":
    bot = IoTSensorMonitorBot()
    bot.poll_telemetry()
""".trimIndent()

    private fun generateDevOpsK8sBot(header: String): String = header + """
from typing import Dict, Any

class DevOpsK8sSentinelBot:
    def audit_cluster_health(self) -> Dict[str, Any]:
        print(">> [K8sSentinel] Auditing cluster...")
        return {"health": "100%"}

if __name__ == "__main__":
    bot = DevOpsK8sSentinelBot()
    bot.audit_cluster_health()
""".trimIndent()

    private fun generateGenericAutonomousBot(prompt: String, header: String): String = header + """
import os
from typing import Dict, Any

class GalaxyBot:
    def __init__(self) -> None:
        self.prompt = "$prompt"

    def execute_task(self) -> Dict[str, Any]:
        print(f">> [GalaxyBot] Executing task: {self.prompt}")
        return {"status": "COMPLETED"}

if __name__ == "__main__":
    bot = GalaxyBot()
    bot.execute_task()
""".trimIndent()
}
