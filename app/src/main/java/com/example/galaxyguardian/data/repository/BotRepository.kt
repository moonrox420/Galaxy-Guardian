package com.example.galaxyguardian.data.repository

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.galaxyguardian.data.model.BotProject
import com.example.galaxyguardian.data.model.BotTemplate
import com.example.galaxyguardian.data.model.ExecutionResult
import com.example.galaxyguardian.data.model.LintIssue
import com.example.galaxyguardian.data.model.QualityAnalysis
import com.example.galaxyguardian.data.model.SecurityVulnerability
import com.example.galaxyguardian.data.model.Severity
import com.example.galaxyguardian.data.model.TypeIssue
import com.example.galaxyguardian.data.service.CodeAnalysisEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class BotRepository(context: Context) {

    private val dbHelper = DatabaseHelper(context.applicationContext)
    private val _savedBots = MutableStateFlow<List<BotProject>>(emptyList())
    val savedBots: StateFlow<List<BotProject>> = _savedBots.asStateFlow()

    private val repositoryScope = CoroutineScope(Dispatchers.IO)

    init {
        repositoryScope.launch {
            loadBotsInternal()
        }
    }

    suspend fun refreshBots() = withContext(Dispatchers.IO) {
        loadBotsInternal()
    }

    private suspend fun loadBotsInternal() = withContext(Dispatchers.IO) {
        val list = mutableListOf<BotProject>()
        try {
            val db = dbHelper.readableDatabase
            val cursor = db.query(
                "bots",
                null,
                null,
                null,
                null,
                null,
                "created_at DESC"
            )
            cursor.use {
                while (it.moveToNext()) {
                    val id = it.getString(it.getColumnIndexOrThrow("id"))
                    val title = it.getString(it.getColumnIndexOrThrow("title"))
                    val prompt = it.getString(it.getColumnIndexOrThrow("prompt"))
                    val lang = it.getString(it.getColumnIndexOrThrow("language"))
                    val generatedCode = it.getString(it.getColumnIndexOrThrow("generated_code"))
                    val formattedCode = it.getString(it.getColumnIndexOrThrow("formatted_code"))
                    val createdAt = it.getLong(it.getColumnIndexOrThrow("created_at"))
                    val isFavorite = it.getInt(it.getColumnIndexOrThrow("is_favorite")) == 1

                    // v2 / v3 columns with fallback defaults
                    val analysisJsonIndex = it.getColumnIndex("quality_analysis_json")
                    val analysisJson = if (analysisJsonIndex >= 0) it.getString(analysisJsonIndex) else null

                    val execJsonIndex = it.getColumnIndex("last_execution_json")
                    val execJson = if (execJsonIndex >= 0) it.getString(execJsonIndex) else null

                    val providerIndex = it.getColumnIndex("provider_type")
                    val providerType = if (providerIndex >= 0) it.getString(providerIndex) ?: "GEMINI" else "GEMINI"

                    val modelIndex = it.getColumnIndex("model_name")
                    val modelName = if (modelIndex >= 0) it.getString(modelIndex) ?: "gemini-3.5-flash" else "gemini-3.5-flash"

                    val targetIdx = it.getColumnIndex("generation_target")
                    val genTarget = if (targetIdx >= 0) it.getString(targetIdx) ?: "AUTO" else "AUTO"

                    val archIdx = it.getColumnIndex("architecture_profile")
                    val archProf = if (archIdx >= 0) it.getString(archIdx) ?: "MVVM" else "MVVM"

                    val filesIdx = it.getColumnIndex("files_json")
                    val filesJsonStr = if (filesIdx >= 0) it.getString(filesIdx) ?: "[]" else "[]"

                    val verIdx = it.getColumnIndex("version_number")
                    val verNum = if (verIdx >= 0) it.getInt(verIdx) else 1

                    val analysis = if (!analysisJson.isNullOrBlank() && analysisJson != "{}") {
                        deserializeQualityAnalysis(analysisJson, formattedCode.ifBlank { generatedCode })
                    } else {
                        CodeAnalysisEngine.analyze(formattedCode.ifBlank { generatedCode })
                    }

                    val lastExecution = if (!execJson.isNullOrBlank()) {
                        deserializeExecutionResult(execJson)
                    } else null

                    list.add(
                        BotProject(
                            id = id,
                            title = title,
                            prompt = prompt,
                            targetLanguage = lang,
                            generatedCode = generatedCode,
                            formattedCode = formattedCode,
                            analysis = analysis,
                            lastExecution = lastExecution,
                            providerType = providerType,
                            modelName = modelName,
                            createdAt = createdAt,
                            isFavorite = isFavorite,
                            generationTarget = genTarget,
                            architectureProfile = archProf,
                            filesJson = filesJsonStr,
                            versionNumber = verNum
                        )
                    )
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("BotRepository", "Error loading bots from database", e)
        }
        _savedBots.value = list
    }

    suspend fun saveBot(bot: BotProject) = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("id", bot.id)
            put("title", bot.title)
            put("prompt", bot.prompt)
            put("language", bot.targetLanguage)
            put("generated_code", bot.generatedCode)
            put("formatted_code", bot.formattedCode)
            put("quality_analysis_json", serializeQualityAnalysis(bot.analysis))
            put("last_execution_json", bot.lastExecution?.let { serializeExecutionResult(it) })
            put("provider_type", bot.providerType)
            put("model_name", bot.modelName)
            put("created_at", bot.createdAt)
            put("is_favorite", if (bot.isFavorite) 1 else 0)
            put("generation_target", bot.generationTarget)
            put("architecture_profile", bot.architectureProfile)
            put("files_json", bot.filesJson)
            put("version_number", bot.versionNumber)
        }
        db.insertWithOnConflict("bots", null, values, SQLiteDatabase.CONFLICT_REPLACE)
        loadBotsInternal()
    }

    suspend fun deleteBot(botId: String) = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        db.delete("bots", "id = ?", arrayOf(botId))
        loadBotsInternal()
    }

    suspend fun toggleFavorite(botId: String) = withContext(Dispatchers.IO) {
        val current = _savedBots.value.find { it.id == botId } ?: return@withContext
        val newFav = !current.isFavorite
        val db = dbHelper.writableDatabase
        val cv = ContentValues().apply {
            put("is_favorite", if (newFav) 1 else 0)
        }
        db.update("bots", cv, "id = ?", arrayOf(botId))
        loadBotsInternal()
    }

    fun getTemplates(): List<BotTemplate> = listOf(
        BotTemplate(
            id = "tmpl_weather",
            title = "Weather Guardian Bot",
            description = "Fetches meteorological forecasts, calculates heat index, and alerts on storm warnings.",
            prompt = "a bot that fetches weather data and provides a formatted forecast report",
            iconName = "cloud",
            sampleCode = """
import os
from typing import Dict, Any

class WeatherGuardianBot:
    def __init__(self) -> None:
        self.api_key: str = os.environ.get("OPENWEATHER_API_KEY", "DEMO_KEY")
    
    def fetch_weather(self, city: str) -> Dict[str, Any]:
        print(f">> [Weather] Querying metrics for: {city}...")
        return {"city": city, "temp_c": 21.5, "conditions": "Clear"}

if __name__ == "__main__":
    bot = WeatherGuardianBot()
    res = bot.fetch_weather("Tokyo")
    print(f">> Output: {res}")
""".trimIndent()
        ),
        BotTemplate(
            id = "tmpl_discord",
            title = "Discord Security Automod",
            description = "Inspects chat channels for spam, malicious links, and unauthorized advertising.",
            prompt = "a discord auto-moderation bot that scans messages for phishing links and spam",
            iconName = "security",
            sampleCode = """
import os
from typing import List

class DiscordAutoModBot:
    def __init__(self) -> None:
        self.bad_words: List[str] = ["phishing", "scam_link", "free_crypto"]
    
    def scan(self, user: str, msg: str) -> bool:
        print(f">> [AutoMod] Scanning message from {user}: '{msg}'")
        for bad in self.bad_words:
            if bad in msg.lower():
                print(f">> [ALERT] Violation detected from {user}: {bad}")
                return False
        return True

if __name__ == "__main__":
    bot = DiscordAutoModBot()
    bot.scan("PilotZero", "Welcome everyone to Galaxy Guardian!")
""".trimIndent()
        ),
        BotTemplate(
            id = "tmpl_crypto",
            title = "Crypto Market Surveillance",
            description = "Tracks spot valuations across exchanges, identifies RSI breakouts, and sends webhooks.",
            prompt = "a crypto tracker bot that monitors Bitcoin price volatility and triggers alerts",
            iconName = "trending_up",
            sampleCode = """
from typing import Dict

class CryptoSurveillanceBot:
    def check_volatility(self) -> Dict[str, float]:
        print(">> [Crypto] Connecting to price feeds...")
        prices = {"BTC": 95200.0, "ETH": 3420.0}
        print(">> [Spot] BTC: " + str(prices["BTC"]) + ", ETH: " + str(prices["ETH"]))
        return prices

if __name__ == "__main__":
    bot = CryptoSurveillanceBot()
    bot.check_volatility()
""".trimIndent()
        ),
        BotTemplate(
            id = "tmpl_github",
            title = "GitHub Issue Triage Sentinel",
            description = "Auto-labels incoming pull requests and issues, verifies test pipelines and licenses.",
            prompt = "a github bot that triages incoming issues, checks labels, and assigns reviewers",
            iconName = "code",
            sampleCode = """
import os
from typing import List, Dict

class GitHubTriageBot:
    def triage(self, issue_title: str) -> List[str]:
        print(f">> [Triage] Inspecting issue: '{issue_title}'")
        labels = ["triage"]
        if "bug" in issue_title.lower() or "error" in issue_title.lower():
            labels.append("bug")
        print(f">> [Labels Applied] {labels}")
        return labels

if __name__ == "__main__":
    bot = GitHubTriageBot()
    bot.triage("Critical bug in auth workflow")
""".trimIndent()
        )
    )

    private fun serializeQualityAnalysis(analysis: QualityAnalysis): String {
        val root = JSONObject()
        root.put("formattedCode", analysis.formattedCode)
        root.put("lintSummaryText", analysis.lintSummaryText)
        root.put("typeSummaryText", analysis.typeSummaryText)
        root.put("securitySummaryText", analysis.securitySummaryText)
        root.put("securityScore", analysis.securityScore)
        root.put("status", analysis.status.name)

        val lintArr = JSONArray()
        analysis.lintIssues.forEach {
            val obj = JSONObject()
            obj.put("line", it.line)
            obj.put("ruleCode", it.ruleCode)
            obj.put("message", it.message)
            obj.put("severity", it.severity.name)
            lintArr.put(obj)
        }
        root.put("lintIssues", lintArr)

        val typeArr = JSONArray()
        analysis.typeIssues.forEach {
            val obj = JSONObject()
            obj.put("line", it.line)
            obj.put("symbol", it.symbol)
            obj.put("message", it.message)
            obj.put("severity", it.severity.name)
            typeArr.put(obj)
        }
        root.put("typeIssues", typeArr)

        val secArr = JSONArray()
        analysis.securityVulnerabilities.forEach {
            val obj = JSONObject()
            obj.put("id", it.id)
            obj.put("cwe", it.cwe)
            obj.put("title", it.title)
            obj.put("description", it.description)
            obj.put("line", it.line)
            obj.put("severity", it.severity.name)
            obj.put("recommendation", it.recommendation)
            secArr.put(obj)
        }
        root.put("securityVulnerabilities", secArr)

        return root.toString()
    }

    private fun deserializeQualityAnalysis(jsonStr: String, fallbackCode: String): QualityAnalysis {
        return try {
            val root = JSONObject(jsonStr)
            val formattedCode = root.optString("formattedCode", fallbackCode)
            val lintSummaryText = root.optString("lintSummaryText", "")
            val typeSummaryText = root.optString("typeSummaryText", "")
            val securitySummaryText = root.optString("securitySummaryText", "")
            val securityScore = root.optInt("securityScore", 100)
            val statusStr = root.optString("status", "SUCCESS")
            val status = runCatching { com.example.galaxyguardian.data.model.AnalysisStatus.valueOf(statusStr) }.getOrDefault(com.example.galaxyguardian.data.model.AnalysisStatus.SUCCESS)

            val lintIssues = mutableListOf<LintIssue>()
            val lintArr = root.optJSONArray("lintIssues")
            if (lintArr != null) {
                for (i in 0 until lintArr.length()) {
                    val obj = lintArr.getJSONObject(i)
                    lintIssues.add(
                        LintIssue(
                            line = obj.optInt("line", 1),
                            ruleCode = obj.optString("ruleCode", ""),
                            message = obj.optString("message", ""),
                            severity = runCatching { Severity.valueOf(obj.optString("severity", "LOW")) }.getOrDefault(Severity.LOW)
                        )
                    )
                }
            }

            val typeIssues = mutableListOf<TypeIssue>()
            val typeArr = root.optJSONArray("typeIssues")
            if (typeArr != null) {
                for (i in 0 until typeArr.length()) {
                    val obj = typeArr.getJSONObject(i)
                    typeIssues.add(
                        TypeIssue(
                            line = obj.optInt("line", 1),
                            symbol = obj.optString("symbol", ""),
                            message = obj.optString("message", ""),
                            severity = runCatching { Severity.valueOf(obj.optString("severity", "MEDIUM")) }.getOrDefault(Severity.MEDIUM)
                        )
                    )
                }
            }

            val securityVulnerabilities = mutableListOf<SecurityVulnerability>()
            val secArr = root.optJSONArray("securityVulnerabilities")
            if (secArr != null) {
                for (i in 0 until secArr.length()) {
                    val obj = secArr.getJSONObject(i)
                    securityVulnerabilities.add(
                        SecurityVulnerability(
                            id = obj.optString("id", "SEC-001"),
                            cwe = obj.optString("cwe", ""),
                            title = obj.optString("title", ""),
                            description = obj.optString("description", ""),
                            line = obj.optInt("line", 1),
                            severity = runCatching { Severity.valueOf(obj.optString("severity", "LOW")) }.getOrDefault(Severity.LOW),
                            recommendation = obj.optString("recommendation", "")
                        )
                    )
                }
            }

            QualityAnalysis(
                formattedCode = formattedCode,
                lintIssues = lintIssues,
                lintSummaryText = lintSummaryText,
                typeIssues = typeIssues,
                typeSummaryText = typeSummaryText,
                securityVulnerabilities = securityVulnerabilities,
                securitySummaryText = securitySummaryText,
                securityScore = securityScore,
                status = status
            )
        } catch (e: Exception) {
            CodeAnalysisEngine.analyze(fallbackCode)
        }
    }

    private fun serializeExecutionResult(result: ExecutionResult): String {
        val obj = JSONObject()
        obj.put("id", result.id)
        obj.put("timestamp", result.timestamp)
        obj.put("stdout", result.stdout)
        obj.put("stderr", result.stderr)
        obj.put("exitCode", result.exitCode)
        obj.put("durationMs", result.durationMs)
        obj.put("isSuccess", result.isSuccess)
        return obj.toString()
    }

    private fun deserializeExecutionResult(jsonStr: String): ExecutionResult? {
        return try {
            val obj = JSONObject(jsonStr)
            ExecutionResult(
                id = obj.optString("id", ""),
                timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                stdout = obj.optString("stdout", ""),
                stderr = obj.optString("stderr", ""),
                exitCode = obj.optInt("exitCode", 0),
                durationMs = obj.optLong("durationMs", 0),
                isSuccess = obj.optBoolean("isSuccess", true)
            )
        } catch (e: Exception) {
            null
        }
    }

    private class DatabaseHelper(context: Context) :
        SQLiteOpenHelper(context, "galaxy_guardian.db", null, 3) {

        override fun onCreate(db: SQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE bots (
                    id TEXT PRIMARY KEY,
                    title TEXT NOT NULL,
                    prompt TEXT NOT NULL,
                    language TEXT NOT NULL,
                    generated_code TEXT NOT NULL,
                    formatted_code TEXT NOT NULL,
                    quality_analysis_json TEXT NOT NULL DEFAULT '{}',
                    last_execution_json TEXT DEFAULT NULL,
                    provider_type TEXT NOT NULL DEFAULT 'GEMINI',
                    model_name TEXT NOT NULL DEFAULT 'gemini-3.5-flash',
                    created_at INTEGER NOT NULL,
                    is_favorite INTEGER NOT NULL DEFAULT 0,
                    generation_target TEXT NOT NULL DEFAULT 'AUTO',
                    architecture_profile TEXT NOT NULL DEFAULT 'MVVM',
                    files_json TEXT NOT NULL DEFAULT '[]',
                    version_number INTEGER NOT NULL DEFAULT 1
                )
                """.trimIndent()
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_bots_created_at ON bots(created_at DESC)")
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_bots_favorite ON bots(is_favorite)")
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            if (oldVersion < 2) {
                safelyAddColumn(db, "bots", "quality_analysis_json TEXT NOT NULL DEFAULT '{}'")
                safelyAddColumn(db, "bots", "last_execution_json TEXT DEFAULT NULL")
                safelyAddColumn(db, "bots", "provider_type TEXT NOT NULL DEFAULT 'GEMINI'")
                safelyAddColumn(db, "bots", "model_name TEXT NOT NULL DEFAULT 'gemini-3.5-flash'")
                safelyExecuteSql(db, "CREATE INDEX IF NOT EXISTS idx_bots_created_at ON bots(created_at DESC)")
                safelyExecuteSql(db, "CREATE INDEX IF NOT EXISTS idx_bots_favorite ON bots(is_favorite)")
            }
            if (oldVersion < 3) {
                safelyAddColumn(db, "bots", "generation_target TEXT NOT NULL DEFAULT 'AUTO'")
                safelyAddColumn(db, "bots", "architecture_profile TEXT NOT NULL DEFAULT 'MVVM'")
                safelyAddColumn(db, "bots", "files_json TEXT NOT NULL DEFAULT '[]'")
                safelyAddColumn(db, "bots", "version_number INTEGER NOT NULL DEFAULT 1")
            }
        }

        private fun safelyAddColumn(db: SQLiteDatabase, table: String, columnDef: String) {
            try {
                db.execSQL("ALTER TABLE $table ADD COLUMN $columnDef")
            } catch (e: Exception) {
                android.util.Log.w("DatabaseHelper", "Column migration note for $columnDef: ${e.message}")
            }
        }

        private fun safelyExecuteSql(db: SQLiteDatabase, sql: String) {
            try {
                db.execSQL(sql)
            } catch (e: Exception) {
                android.util.Log.w("DatabaseHelper", "SQL migration note for $sql: ${e.message}")
            }
        }
    }
}
