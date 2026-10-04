# Galaxy Guardian vNext
## Repo-Grounded Kotlin-First Android AI Builder PRD

**Repository:** `moonrox420/Galaxy-Guardian`  
**Baseline commit:** `0c99137fc3106e47cde78338bf38e0d10585f60d`  
**Baseline CI:** Android CI & Verification — successful  
**Strategic direction:** Kotlin/Android-first development agent with Python retained as a first-class secondary capability.

---

# 1. Executive Decision

Galaxy Guardian should evolve from:

> Android application that generates, analyzes, and simulates Python bots

into:

> Android-native AI development workbench capable of generating complete Kotlin/Jetpack Compose Android features and applications, analyzing them, repairing them, versioning them, and exporting them as real project artifacts.

Python stays.

The product should therefore become **language-aware**, not replace Python with Kotlin.

The important difference after reviewing the repository is that Galaxy Guardian already contains the beginning of this architecture.

`BotProject` already has:

```kotlin
val targetLanguage: String = "python"
```

and `BotCodeGenerator.generate()` already accepts:

```kotlin
targetLanguage: String
```

The provider router already forwards it through Gemini, Ollama, and the offline generator.

The problem is that the rest of the application effectively ignores that abstraction and forces Python anyway.

This means the Kotlin pivot is not a rewrite.

It is the completion of an unfinished multi-language seam that already exists.

---

# 2. Repository State — What Is Already Done

The following features from the previous PRD already exist and should **not** be assigned to an AI coding agent as new work.

| Capability | Current State |
|---|---|
| Android-native Kotlin/Compose application | ✅ Implemented |
| Kotlin 2.2.10 / SDK 36 / Java 21 | ✅ Implemented |
| Gemini generation | ✅ Implemented |
| Ollama generation | ✅ Implemented |
| Offline generator | ✅ Implemented |
| Provider-independent `BotCodeGenerator` | ✅ Implemented |
| `LlmServiceRouter` | ✅ Implemented |
| `targetLanguage` parameter | ⚠️ Exists but is effectively unused |
| `targetLanguage` persistence | ⚠️ Exists as an untyped String |
| Secure Gemini header authentication | ✅ Implemented |
| OkHttp `.use {}` response closing | ✅ Implemented |
| Encrypted credential storage | ✅ Implemented using custom AES-GCM + Android Keystore |
| SQLite non-destructive v2 migration | ✅ Implemented |
| Persisted analysis results | ✅ Implemented |
| Persisted execution results | ✅ Implemented |
| Simulated execution instead of Python bytecode execution | ✅ Implemented |
| Cancellation via `activeJob` | ⚠️ Implemented with a state bug |
| Personality presets | ✅ Six presets already implemented |
| Personality persistence | ✅ Implemented |
| Library search | ✅ Implemented for title/prompt |
| Library category filters | ✅ Implemented |
| Favorites | ✅ Implemented |
| Share generated code | ✅ Implemented |
| Navigation Rail for ≥600dp screens | ✅ Implemented |
| Phone bottom navigation | ✅ Implemented |
| Android CI | ✅ Implemented |
| Current CI on baseline commit | ✅ Passing |
| Security pattern scanner | ✅ Implemented, Python-centric |
| IoT template | ✅ Implemented |
| Kubernetes/DevOps template | ✅ Implemented |
| HTTP cleartext scanner | ✅ Implemented |
| hardcoded JWT scanner | ✅ Implemented |

The repository is therefore substantially farther along than the historical phases in `PRD.md` suggest.

---

# 3. Important Repository Corrections

Several tasks from the earlier roadmap need to be rewritten.

## Do NOT create `targetLanguage` from scratch

It already exists in:

- `BotProject`
- `BotCodeGenerator`
- `LlmServiceRouter`
- Gemini generator
- Ollama generator
- offline generator
- SQLite persistence

The real task is:

> Replace the loosely typed/string-based, Python-hardcoded implementation with a first-class typed language model and actually honor it throughout the application.

---

## Do NOT create personality presets from scratch

`GalaxyGuardianViewModel.kt` already ships:

- Helpful Assistant
- Sarcastic Companion
- Serious Analyst
- Security Guardian
- Code Architect
- Ghost Infiltrator

The problem is that several of them explicitly instruct the model to generate Python.

The task becomes:

> Make personality behavior language-neutral or language-aware.

---

## Do NOT create library search from scratch

`BotLibraryScreen.kt` already searches title and prompt and now contains category filters.

The task becomes:

> Expand search into generated source, language, tags, architecture, project type, and file names.

---

## Do NOT create cancellation from scratch

`GalaxyGuardianViewModel` already has:

```kotlin
private var activeJob: Job? = null
```

and both generation and simulation call:

```kotlin
activeJob?.cancel()
```

The task becomes:

> Fix cancellation state correctness and model operations explicitly.

---

## Do NOT rebuild SQLite v2

The repository already performs non-destructive v1→v2 migration and persists analysis/execution JSON.

Future persistence work should target **project/multi-file/version architecture**, not redo migration v2.

---

# 4. P0 — Bugs and Reliability Fixes Before the Kotlin Pivot

These should be fixed first because they affect the correctness of the existing product.

---

## GG-P0-001 — Fix cancellation leaving stale busy state

### Current implementation

Generation:

```kotlin
activeJob?.cancel()
activeJob = viewModelScope.launch {
    isGenerating = true

    try {
        ...
    } catch (e: Exception) {
        if (e is CancellationException) throw e
        isGenerating = false
    }
}
```

Simulation follows the same structure.

### Problem

A cancellation throws `CancellationException`, which is immediately rethrown.

No `finally` resets the corresponding state.

Example:

1. Generation begins.
2. `isGenerating = true`.
3. User presses Run.
4. `executeCode()` cancels generation.
5. Generation rethrows `CancellationException`.
6. `isGenerating` never becomes false.
7. Simulation sets `isExecuting = true`.
8. UI can now remain in a contradictory/stuck state.

The reverse can happen when generation cancels execution.

### Fix

Prefer a typed operation state:

```kotlin
sealed interface ActiveOperation {
    data object Idle : ActiveOperation
    data object Generating : ActiveOperation
    data object Analyzing : ActiveOperation
    data object Simulating : ActiveOperation
    data object Exporting : ActiveOperation
}
```

or at minimum use guaranteed `finally` cleanup.

### Acceptance criteria

- Cancelled generation cannot leave `isGenerating=true`.
- Cancelled simulation cannot leave `isExecuting=true`.
- Rapid Generate → Run → Generate cannot produce stale state.
- Unit tests cover cancellation.
- UI controls recover after cancellation.

---

# 5. GG-P0-002 — Never report failed analysis as “100/100 secure”

## Current implementation

`CodeAnalysisEngine.analyze()` catches `Throwable`.

On failure it returns:

```kotlin
securityVulnerabilities = emptyList()
securitySummaryText = "Security Pattern Scanner: Clean pattern scan."
securityScore = 100
```

### Problem

An analyzer crash is interpreted as:

> No vulnerabilities, perfect score.

That is unsafe and logically incorrect.

### Required fix

Introduce analysis state:

```kotlin
enum class AnalysisStatus {
    SUCCESS,
    PARTIAL,
    FAILED
}
```

`QualityAnalysis` should contain:

```kotlin
val status: AnalysisStatus
val analysisErrors: List<String>
```

When security analysis fails:

```text
Security score: unavailable
Analysis status: FAILED
```

Never:

```text
100/100 clean
```

### Acceptance criteria

- Internal analysis failure cannot produce a clean security verdict.
- UI visibly distinguishes CLEAN from ANALYSIS FAILED.
- export metadata includes analysis status.
- tests intentionally trigger analyzer failure.

---

# 6. GG-P0-003 — Fix physical-device Ollama cleartext networking

The README instructs physical-device users to configure:

```text
http://192.168.x.x:11434
```

But `network_security_config.xml` permits cleartext only to:

```text
10.0.2.2
localhost
127.0.0.1
```

while:

```xml
<base-config cleartextTrafficPermitted="false">
```

remains active.

### Result

The documented physical-device LAN configuration can be rejected by Android cleartext policy.

### Requirement

Reconcile actual networking policy with the product requirement.

Possible supported solutions include:

- require HTTPS for arbitrary LAN hosts, or
- offer an explicitly enabled development/LAN mode with an appropriate Android networking strategy.

Do **not** silently claim all `192.168.x.x` addresses are currently allowed.

### Acceptance criteria

- README matches real Android behavior.
- physical device → Ollama LAN connection is tested.
- network policy remains strict for public endpoints.
- Settings explains HTTP/HTTPS expectations.

---

# 7. GG-P0-004 — Correct security documentation

README currently describes:

> AndroidX Security Crypto / EncryptedSharedPreferences

The implementation actually uses:

```text
Android Keystore AES-256 key
        ↓
AES/GCM/NoPadding
        ↓
Base64 ciphertext + IV
        ↓
normal SharedPreferences
```

This is not necessarily a bad design.

It is simply not `EncryptedSharedPreferences`.

### Fix

Either:

A. update README/PRD to describe the actual custom AES-GCM implementation,

or:

B. deliberately migrate to a maintained encrypted-storage abstraction.

Do not document technology that the repository does not use.

---

# 8. GG-P0-005 — Correct analyzer terminology in README

README currently describes security scanning as:

> AST-level pattern matching

`CodeAnalysisEngine` currently operates primarily through line/string/regex inspection.

### Fix

Use accurate language:

> deterministic static pattern scanner

unless a real parser/AST implementation is introduced.

This is important because Galaxy Guardian's existing product philosophy explicitly values honest capability descriptions.

---

# 9. GG-P0-006 — Stop swallowing migration errors

`BotRepository.DatabaseHelper.onUpgrade()` contains several patterns like:

```kotlin
try {
    ...
} catch (_: Exception) {}
```

### Problem

A partially failed migration can become invisible.

The database may then appear upgraded despite missing columns/indexes.

### Fix

- only suppress a known “already exists” condition where appropriate
- log unexpected SQLite errors
- use explicit migration functions
- wrap migration in a transaction where possible
- add migration tests

### Acceptance criteria

v1 → v2 migration test verifies:

- projects survive
- analysis columns exist
- execution column exists
- provider/model columns exist
- indexes exist
- migration failures are visible

---

# 10. GG-P0-007 — Move initial database loading off the construction path

`BotRepository` currently calls:

```kotlin
init {
    loadBots()
}
```

and `loadBots()` performs SQLite reads synchronously.

The repository is instantiated during ViewModel creation.

### Requirement

Move disk I/O onto `Dispatchers.IO`.

Prefer:

```kotlin
suspend fun refreshBots()
```

or an internally scoped asynchronous repository flow.

### Acceptance criteria

- no SQLite read occurs synchronously during ViewModel construction
- saved-bot loading exposes Loading / Ready / Error
- no startup UI freeze
- repository tests cover load behavior

---

# 11. GG-P0-008 — Delete unused compatibility bridges

The repository still contains:

```text
CodeToolsEngine.kt
SandboxedExecutionEngine.kt
```

Both are backwards-compatibility delegates.

Repository search shows no current application usage other than their own definitions/documentation.

### Action

Remove them once CI confirms no references.

### Acceptance criteria

- zero runtime references
- files deleted
- no obsolete “sandbox” terminology remains in implementation identifiers where avoidable

Also consider renaming:

```kotlin
ResultsTab.SANDBOX_EXECUTION
```

to:

```kotlin
ResultsTab.SIMULATION
```

for consistency.

---

# 12. GG-P0-009 — Fix generic code-fence extraction

Gemini and Ollama currently special-case:

```text
```python
```

and then generic:

```text
```
```

For:

```text
```kotlin
class Foo ...
```

the generic branch removes only the three backticks and can leave:

```text
kotlin
class Foo ...
```

in the source.

### Fix

Implement language-aware Markdown fence extraction.

Examples that must work:

```text
```python
```kotlin
```kt
```typescript
```go
```
```

Prefer a reusable parser shared by providers.

---

# 13. GG-P0-010 — Restore saved project generation context

`BotProject` stores:

```text
targetLanguage
providerType
modelName
```

but `loadBot()` only restores:

```text
prompt
code
analysis
execution
```

It does not restore language/provider/model configuration into active UI state.

### Fix

Loading a project should restore its relevant generation context.

### Acceptance criteria

Save with Ollama → load → provider/model restored.

Save Kotlin → load → Kotlin restored.

---

# 14. Kotlin Pivot Architecture

After P0, convert the existing language seam into a proper domain model.

---

# 15. GG-KOT-001 — Replace language Strings with `TargetLanguage`

Add:

```kotlin
enum class TargetLanguage(
    val persistenceId: String,
    val displayName: String,
    val fenceNames: Set<String>
) {
    KOTLIN_ANDROID(
        persistenceId = "kotlin_android",
        displayName = "Kotlin / Android",
        fenceNames = setOf("kotlin", "kt")
    ),

    PYTHON(
        persistenceId = "python",
        displayName = "Python",
        fenceNames = setOf("python", "py")
    )
}
```

### Modify

- `BotProject`
- `BotTemplate`
- `BotCodeGenerator`
- `GenerationRequest`
- `UiState`
- generator implementations
- repository serialization/deserialization
- tests

### Backward compatibility

Existing SQLite records containing:

```text
python
```

must deserialize as:

```text
TargetLanguage.PYTHON
```

Unknown future values should degrade safely rather than crash.

---

# 16. GG-KOT-002 — Add language state to the ViewModel

Current generation hardcodes:

```kotlin
targetLanguage = "python"
```

Current saving also hardcodes Python.

Replace both with:

```kotlin
_uiState.value.targetLanguage
```

Add:

```kotlin
fun setTargetLanguage(language: TargetLanguage)
```

### Acceptance criteria

The selected language flows through:

```text
HomeScreen
→ UiState
→ ViewModel
→ BotCodeGenerator
→ LlmServiceRouter
→ provider
→ analysis
→ persistence
→ library
→ load
```

without guessing.

---

# 17. GG-KOT-003 — Add language selector to Home

Current Home UI contains:

```text
Persona
Provider
Prompt
Generate
Run
Save
Share
```

Add:

```text
Language
[Kotlin / Android] [Python]
```

Kotlin should become the default **for Android/app-development use**.

Python remains clearly accessible.

---

# 18. GG-KOT-004 — Make Home terminology contextual

Current UI says:

```text
Enter your idea for the bot
Generate Code
Saved Sentinel Bots
```

When `KOTLIN_ANDROID` is selected, language should become:

```text
What do you want to build?
Android App / Feature
Generated Project
Saved Projects
```

Do not call every Kotlin application a “bot”.

Python mode can retain bot terminology.

---

# 19. GG-KOT-005 — Introduce `GenerationTarget`

Language is not enough.

Create:

```kotlin
enum class GenerationTarget {
    AUTO,
    CODE_SNIPPET,

    COMPOSABLE_SCREEN,
    ANDROID_FEATURE,
    ANDROID_APP,
    VIEW_MODEL,
    DATA_LAYER,
    WORK_MANAGER_WORKER,
    BACKGROUND_SERVICE,

    BOT,
    AUTOMATION
}
```

### Important

Not every target is valid for every language.

Examples:

```text
KOTLIN_ANDROID + COMPOSABLE_SCREEN   valid
KOTLIN_ANDROID + ANDROID_APP         valid
PYTHON + BOT                         valid
PYTHON + WORK_MANAGER_WORKER         invalid
```

Expose supported target combinations through domain logic.

---

# 20. GG-KOT-006 — Replace provider-specific prompt composition with prompt policies

Both Gemini and Ollama currently contain Python-specific prompt text.

Examples include:

```text
PEP 8 conformant
comprehensive docstrings
type annotations
bot request
```

That cannot simply interpolate:

```text
$targetLanguage
```

because:

> “Generate PEP 8 conformant Kotlin”

is nonsense.

Create:

```kotlin
interface GenerationPromptPolicy {
    fun systemPrompt(request: GenerationRequest): String
    fun userPrompt(request: GenerationRequest): String
}
```

Implement:

```text
PythonPromptPolicy
KotlinAndroidPromptPolicy
```

Providers should transport prompts.

They should not decide language semantics.

---

# 21. Kotlin Android Prompt Requirements

The Kotlin policy should prefer:

- idiomatic Kotlin
- Jetpack Compose
- Material 3
- immutable UI state
- ViewModel
- StateFlow
- lifecycle-aware state collection
- structured coroutines
- proper dispatcher usage
- no `GlobalScope`
- no unnecessary `!!`
- no Activity/Context leaks
- correct Android component declarations
- modern permission handling
- Room/DataStore where appropriate
- WorkManager where appropriate
- Retrofit/OkHttp or Ktor as explicitly selected
- no embedded secrets
- clean package boundaries
- Gradle dependencies declared explicitly
- Manifest requirements declared explicitly

---

# 22. GG-KOT-007 — Make personalities language-aware

Current presets include instructions such as:

```text
Heavily documented educational Python
Hardened enterprise Python
Asyncio cyberpunk
PEP 8 conformant Python
```

Personality and implementation language should be separate concepts.

### Preferred model

Personality owns:

```text
tone
behavior
design priorities
explanation style
risk tolerance
architecture preference
```

Language policy owns:

```text
syntax
frameworks
style rules
runtime constraints
```

### Example

Security Guardian:

```text
Validate external input.
Prefer least privilege.
Avoid embedded credentials.
Use defensive error handling.
```

Kotlin policy adds:

```text
Use lifecycle-aware coroutines and secure Android storage.
```

Python policy adds:

```text
Avoid eval/exec/shell=True.
```

---

# 23. GG-KOT-008 — Kotlin offline templates

`LocalTemplateGenerator` accepts `targetLanguage` but currently ignores it and always returns Python.

That must be fixed.

### Short-term architecture

```text
LocalTemplateGenerator
├── PythonTemplateGenerator
└── KotlinAndroidTemplateGenerator
```

### Required Kotlin templates

1. Material 3 Compose screen
2. ViewModel + StateFlow
3. Navigation Compose feature
4. Room entity/DAO/database
5. DataStore preferences
6. Retrofit repository
7. WorkManager worker
8. foreground service
9. notification workflow
10. simple multi-screen application

### Acceptance criteria

Offline Kotlin mode actually produces Kotlin.

Unsupported language/target combinations produce a typed failure instead of secretly generating Python.

---

# 24. Analysis Engine Pivot

The current `CodeAnalysisEngine` is fundamentally Python-specific.

For Kotlin code, rules such as:

```text
bare except
from x import *
def ...
PEP 8
Python type annotation syntax
pickle
hashlib.md5
```

cannot be reused.

---

# 25. GG-AN-001 — Introduce analyzer interface

Create:

```kotlin
interface CodeAnalyzer {
    val supportedLanguages: Set<TargetLanguage>

    fun analyze(
        artifact: GeneratedArtifact,
        context: AnalysisContext
    ): AnalysisReport
}
```

Suggested analyzers:

```text
PythonStyleAnalyzer
PythonTypeAnalyzer
PythonSecurityAnalyzer

KotlinStyleAnalyzer
KotlinCorrectnessAnalyzer
ComposeAnalyzer
AndroidSecurityAnalyzer

DependencyAnalyzer
```

---

# 26. GG-AN-002 — Preserve existing Python analyzer

Do not throw away `CodeAnalysisEngine`.

Extract its existing rules into Python-specific analyzers.

This minimizes regression risk.

---

# 27. GG-AN-003 — Kotlin correctness analyzer

Initial rules should detect:

- `GlobalScope`
- suspicious `!!`
- blocking calls in coroutine/main paths
- improper dispatcher usage
- mutable state exposed publicly
- lifecycle-insensitive Flow collection
- Activity/Context retention
- broad exception swallowing
- unstructured coroutine creation
- potentially leaking callbacks
- blocking network calls on main thread

---

# 28. GG-AN-004 — Compose analyzer

Check for:

- incorrect state ownership
- avoidable internal state instead of state hoisting
- expensive work during composition
- misuse of `remember`
- misuse of `rememberSaveable`
- misuse of `LaunchedEffect`
- unstable keys
- side effects performed directly during composition
- ViewModel creation in inappropriate places
- missing accessibility semantics
- missing content descriptions where needed

---

# 29. GG-AN-005 — Android security scanner

Add Android-specific findings for:

- unnecessary exported components
- exported receivers/services without protection
- cleartext network usage
- unsafe WebView configuration
- hardcoded credentials
- unsafe intent handling
- mutable PendingIntent mistakes
- unsafe file URI exposure
- weak local secret storage
- sensitive logging
- overly broad permissions
- insecure deep links
- unvalidated content/file URIs

---

# 30. GG-AN-006 — Dependency/import analyzer

For Python:

```text
import / from
requirements
risky modules
```

For Android/Kotlin:

```text
Gradle dependencies
imports
required AndroidX libraries
version compatibility
missing dependencies
```

Generated project output should know which dependencies its source requires.

---

# 31. GG-AN-007 — Configurable severity gate

Add:

```kotlin
enum class QualityGate {
    NONE,
    CRITICAL,
    HIGH,
    MEDIUM
}
```

Examples:

```text
Fail export on CRITICAL
Fail export on HIGH+
Warn only
```

The quality gate must distinguish:

```text
FAILED_ANALYSIS
```

from:

```text
NO_FINDINGS
```

---

# 32. The Major Architecture Change — Stop Treating Generated Software as One String

The current provider result is:

```kotlin
GenerationResult.Success(
    code: String
)
```

That is adequate for a Python bot.

It is not adequate for an Android application.

A real Android feature has multiple files.

This is the central architecture change required for Galaxy Guardian to become an app builder.

---

# 33. GG-PROJ-001 — Introduce generated project/artifact model

Suggested direction:

```kotlin
data class GeneratedProject(
    val id: String,
    val name: String,
    val language: TargetLanguage,
    val target: GenerationTarget,
    val packageName: String?,
    val files: List<GeneratedFile>,
    val metadata: ProjectMetadata
)

data class GeneratedFile(
    val path: String,
    val content: String,
    val kind: GeneratedFileKind
)
```

Example:

```text
app/
├── build.gradle.kts
└── src/main/
    ├── AndroidManifest.xml
    └── java/com/example/habits/
        ├── HabitScreen.kt
        ├── HabitViewModel.kt
        ├── HabitUiState.kt
        ├── HabitRepository.kt
        ├── HabitEntity.kt
        ├── HabitDao.kt
        └── HabitDatabase.kt
```

---

# 34. GG-PROJ-002 — Introduce structured generation request

Replace a growing argument list with:

```kotlin
data class GenerationRequest(
    val prompt: String,
    val language: TargetLanguage,
    val target: GenerationTarget,
    val architecture: ArchitectureProfile,
    val personality: BotPersonality?,
    val provider: GenerationConfig
)
```

---

# 35. GG-PROJ-003 — Add pre-generation planning

Before writing code, derive:

```text
project intent
screens
architecture
models
persistence
networking
background work
permissions
dependencies
expected files
```

Example:

```json
{
  "target": "ANDROID_FEATURE",
  "architecture": "MVVM",
  "files": [
    "HabitScreen.kt",
    "HabitViewModel.kt",
    "HabitUiState.kt",
    "HabitRepository.kt",
    "HabitDao.kt"
  ]
}
```

The plan becomes the contract used to verify generation completeness.

---

# 36. GG-PROJ-004 — Multi-stage generation

Do not ask the model to produce an entire serious Android app in one 4,096-token response.

Use:

```text
Requirements
   ↓
Project Plan
   ↓
File Manifest
   ↓
Generate file 1
   ↓
Generate file 2
   ↓
...
   ↓
Cross-file consistency pass
   ↓
Analysis
```

This will be considerably more reliable than giant monolithic responses.

---

# 37. GG-PROJ-005 — Android Feature Generator

Required output categories:

- Composable UI
- UiState
- ViewModel
- repository
- data models
- persistence
- network client if needed
- navigation
- Gradle dependency declarations
- Manifest changes
- resources
- basic tests

---

# 38. GG-PROJ-006 — Full application scaffold

Support:

```text
settings.gradle.kts
build.gradle.kts
app/build.gradle.kts
AndroidManifest.xml
MainActivity.kt
App.kt
navigation/
ui/
data/
domain/
res/
README.md
```

---

# 39. GG-PROJ-007 — Architecture profiles

Implement:

```kotlin
enum class ArchitectureProfile {
    SIMPLE,
    MVVM,
    MVI,
    CLEAN_ARCHITECTURE,
    OFFLINE_FIRST
}
```

Default:

```text
MVVM + Compose
```

unless user intent indicates otherwise.

---

# 40. Project Persistence v3

Current SQLite v2 is suitable for single-code-file bots.

Multi-file projects and version history require a stronger schema.

---

# 41. GG-DB-001 — Design v3 project schema

Recommended:

```text
projects
project_versions
project_files
analysis_reports
```

Conceptually:

```text
projects
--------
id
title
prompt
language
generation_target
architecture
active_version_id
created_at
updated_at
favorite

project_versions
----------------
id
project_id
version_number
source
provider
model
created_at

project_files
-------------
id
version_id
path
content
file_type
```

---

# 42. GG-DB-002 — Migrate existing bots into projects

Every existing `bots` record becomes:

```text
Project
  language = PYTHON
  target = BOT
  version = 1
  generated file = main.py
```

No user data loss.

---

# 43. GG-DB-003 — Version history

Every meaningful operation creates a project version:

```text
Initial generation
Regeneration
AI repair
User edit
Import
```

Do not overwrite history.

---

# 44. GG-DB-004 — Diff viewer

Support:

```text
Version 1 → Version 2
Generated → AI repaired
```

For multi-file projects show:

```text
added files
deleted files
modified files
per-file diff
```

---

# 45. Self-Repair System

This is one of the highest-value features for Galaxy Guardian.

---

# 46. GG-AI-001 — “Improve / Fix” action

Pipeline:

```text
Generate
   ↓
Analyze
   ↓
Build structured finding list
   ↓
Send affected files + findings to model
   ↓
Repair
   ↓
Re-analyze
   ↓
Compare
   ↓
Create new version
```

---

# 47. GG-AI-002 — Targeted repairs

Do not regenerate an entire project because one file has a problem.

Repair affected files whenever possible.

Send:

```text
original user request
architecture
file
dependent interfaces
analyzer findings
constraints
```

---

# 48. GG-AI-003 — Repair verification

Never say:

> Fixed

just because the model returned another answer.

Verify it.

Display:

```text
5 findings before
2 findings after
3 fixed
0 new HIGH/CRITICAL
```

---

# 49. GG-AI-004 — Generate tests

Add action:

```text
Generate Tests
```

For Kotlin:

- ViewModel unit tests
- repository tests
- state reducer tests
- Compose tests where reasonable

For Python:

- pytest-style tests

Generated tests become project files.

---

# 50. Streaming Generation

Both current providers are non-streaming.

Ollama explicitly sends:

```json
"stream": false
```

Gemini uses a normal generate-content request.

---

# 51. GG-STREAM-001 — Streaming generation contract

Add a stream-capable interface such as:

```kotlin
sealed interface GenerationEvent {
    data class Planning(...) : GenerationEvent
    data class FileStarted(...) : GenerationEvent
    data class ContentDelta(...) : GenerationEvent
    data class FileCompleted(...) : GenerationEvent
    data class AnalysisStarted(...) : GenerationEvent
    data class Completed(...) : GenerationEvent
}
```

Expose through `Flow<GenerationEvent>`.

---

# 52. GG-STREAM-002 — Streaming UI

Display:

```text
Planning...
Creating HabitScreen.kt...
Creating HabitViewModel.kt...
Analyzing...
```

Rather than a frozen:

```text
Synthesizing...
```

---

# 53. GG-OLLAMA-001 — Ollama model discovery

Current Settings requires the user to type a model name.

Add discovery through Ollama's model-list API.

UI:

```text
Available Models
○ qwen-coder
○ codellama
○ deepseek-coder
...
```

Keep manual entry as fallback.

---

# 54. Simulation Redesign

`SimulatedExecutionEngine` currently understands Python-flavored patterns:

```text
print(...)
bot.run(...)
requests.get(...)
aiohttp
fetch_weather
```

It should not pretend to simulate Kotlin applications using those rules.

---

# 55. GG-SIM-001 — Make simulation language/target aware

Introduce:

```text
PythonBotSimulation
AndroidFeatureSimulation
```

For Kotlin/Android, simulation should be conceptual:

```text
User Event
→ Composable callback
→ ViewModel action
→ Repository
→ Room/API
→ StateFlow update
→ recomposition
```

Do not claim actual application execution.

---

# 56. GG-SIM-002 — Scenario packs

Support:

### Python

```text
Discord spam flood
API rate limit
crypto price spike
webhook burst
network timeout
```

### Android

```text
offline startup
network timeout
Room failure
permission denied
process recreation
background-work retry
empty database
malformed server response
```

---

# 57. GG-SIM-003 — Timeline output

Example:

```text
00ms  UI event: SaveHabit
03ms  ViewModel validation
05ms  Repository.write()
08ms  Room insert simulated
10ms  StateFlow updated
13ms  UI state rendered
```

---

# 58. Live Compose Preview

This is valuable, but it should come **after** good multi-file generation.

The repository currently has no Kotlin compiler/runtime sandbox designed to safely compile arbitrary generated application code.

Therefore:

> “Live preview” must not become “load arbitrary generated code into Galaxy Guardian with host privileges.”

---

# 59. GG-PREV-001 — Safe preview eligibility

Only preview constrained components that satisfy defined requirements.

Reject previews requiring uncontrolled:

- reflection
- arbitrary network access
- arbitrary file access
- host credentials
- services
- receivers
- native libraries

---

# 60. GG-PREV-002 — Preview fixtures

Generate safe preview state independently from production dependencies.

Example:

```kotlin
HabitScreen(
    state = HabitUiState.preview(),
    onAction = {}
)
```

---

# 61. Export System

---

# 62. GG-EXP-001 — Android Studio project export

Export:

```text
complete project directory
```

including generated configuration.

---

# 63. GG-EXP-002 — Android module export

For feature generation export:

```text
feature/
├── build.gradle.kts
└── src/
```

---

# 64. GG-EXP-003 — README generation

Include:

```text
Project purpose
Architecture
Build instructions
Dependencies
Permissions
Required credentials
Generated files
Known limitations
Analysis summary
```

---

# 65. GG-EXP-004 — Python deployment artifacts

Retain Python usefulness.

Generate when requested:

```text
requirements.txt
Dockerfile
.env.example
README.md
```

Docker defaults should avoid:

- root where practical
- embedded credentials
- unsafe shell entrypoints

---

# 66. Library vNext

Current library already provides:

- search
- category filters
- favorites
- saved bots
- preset templates

Build on it.

---

# 67. GG-LIB-001 — Search source code

Expand search to:

```text
title
prompt
file paths
source content
language
target
architecture
tags
```

---

# 68. GG-LIB-002 — Replace heuristic categories with metadata

Current category classification examines prompt words such as:

```text
weather
discord
k8s
iot
database
```

That works for the current bot library but won't scale.

Persist explicit:

```text
category
tags
language
target
architecture
```

instead.

---

# 69. GG-LIB-003 — Language/project badges

Example cards:

```text
Habit Tracker
KOTLIN • ANDROID APP • MVVM
Security 94
```

or:

```text
Discord Sentinel
PYTHON • BOT
Security 97
```

---

# 70. GG-LIB-004 — Folders/collections

Allow grouping by:

```text
project
client
experiment
template
```

---

# 71. Diagnostics

---

# 72. GG-DIAG-001 — Generation metrics

Capture:

```text
provider
model
target
first-token latency
total generation latency
analysis latency
repair latency
number of files
source size
```

Never persist prompt/secret information into diagnostics unless explicitly intended.

---

# 73. GG-DIAG-002 — Error diagnostics

Provide user-exportable logs with credential redaction.

---

# 74. Test Strategy

The current repository only contains three unit-test classes:

```text
BotCodeGeneratorTest
CodeAnalysisEngineTest
SimulatedExecutionEngineTest
```

Coverage needs to grow before the architecture pivot.

---

# 75. GG-TEST-001 — ViewModel tests

Cover:

- language selection
- provider selection
- cancellation
- stale operation state
- loading a saved project
- save metadata
- generation success
- generation fallback
- analyzer failure

---

# 76. GG-TEST-002 — Repository migration tests

Cover:

```text
v1 → v2
v2 → v3
bot → project migration
version preservation
```

---

# 77. GG-TEST-003 — Generator routing tests

Matrix:

| Language | Target | Provider | Expected |
|---|---|---|---|
| Python | Bot | Offline | Python |
| Python | Bot | Gemini | Python |
| Python | Bot | Ollama | Python |
| Kotlin | Compose Screen | Offline | Kotlin |
| Kotlin | Android Feature | Gemini | Kotlin |
| Kotlin | Android Feature | Ollama | Kotlin |

---

# 78. GG-TEST-004 — Prompt-policy tests

Assert Kotlin prompts contain no accidental:

```text
PEP 8
asyncio
Python type annotations
```

unless those strings legitimately appear in the user's request.

Assert Python prompts contain no Android-specific framework requirements unless requested.

---

# 79. GG-TEST-005 — Analyzer tests

Python and Kotlin test suites must be independent.

A Kotlin file passed to the Python analyzer should not generate misleading Python findings.

---

# 80. CI Gate

Every AI implementation task must finish with:

```bash
./gradlew lintDebug --no-daemon
./gradlew testDebugUnitTest --no-daemon
./gradlew assembleDebug --no-daemon
```

No task is complete while CI is red.

The baseline repository already has a working Android GitHub Actions workflow, so future work should extend it rather than replace it.

---

# 81. AI Engineering Execution Queue

This is the recommended exact implementation sequence.

## PHASE A — Correctness first

- [ ] `GG-P0-001` cancellation state fix
- [ ] `GG-P0-002` failed analysis must not score 100
- [ ] `GG-P0-003` physical Ollama network policy
- [ ] `GG-P0-004` encryption documentation correction
- [ ] `GG-P0-005` analyzer terminology correction
- [ ] `GG-P0-006` migration error handling
- [ ] `GG-P0-007` asynchronous repository loading
- [ ] `GG-P0-008` remove legacy bridge classes
- [ ] `GG-P0-009` language-aware fence extraction
- [ ] `GG-P0-010` restore saved provider/language/model state

## PHASE B — Make the existing language seam real

- [ ] `GG-KOT-001` `TargetLanguage`
- [ ] `GG-KOT-002` ViewModel language state
- [ ] `GG-KOT-003` language selector
- [ ] `GG-KOT-004` contextual bot/app UI
- [ ] `GG-KOT-005` `GenerationTarget`
- [ ] `GG-KOT-006` prompt policy abstraction
- [ ] `GG-KOT-007` language-aware personas
- [ ] `GG-KOT-008` Kotlin offline templates

## PHASE C — Analyzer architecture

- [ ] `GG-AN-001` analyzer interface/registry
- [ ] `GG-AN-002` preserve Python analyzers
- [ ] `GG-AN-003` Kotlin analyzer
- [ ] `GG-AN-004` Compose analyzer
- [ ] `GG-AN-005` Android security analyzer
- [ ] `GG-AN-006` dependency analyzer
- [ ] `GG-AN-007` severity gates

## PHASE D — Turn code responses into projects

- [ ] `GG-PROJ-001` multi-file project model
- [ ] `GG-PROJ-002` structured generation request
- [ ] `GG-PROJ-003` planning pass
- [ ] `GG-PROJ-004` multi-stage generation
- [ ] `GG-PROJ-005` Android feature generation
- [ ] `GG-PROJ-006` Android application scaffold
- [ ] `GG-PROJ-007` architecture profiles

## PHASE E — Persistence and versioning

- [ ] `GG-DB-001` database v3
- [ ] `GG-DB-002` migrate existing Python bots
- [ ] `GG-DB-003` project version history
- [ ] `GG-DB-004` diff viewer

## PHASE F — Self-healing generation

- [ ] `GG-AI-001` Improve/Fix action
- [ ] `GG-AI-002` targeted file repair
- [ ] `GG-AI-003` repair verification
- [ ] `GG-AI-004` test generation

## PHASE G — Perceived intelligence

- [ ] `GG-STREAM-001` streaming provider API
- [ ] `GG-STREAM-002` streaming UI
- [ ] `GG-OLLAMA-001` model discovery

## PHASE H — Simulation and preview

- [ ] `GG-SIM-001` language-aware simulation
- [ ] `GG-SIM-002` scenario packs
- [ ] `GG-SIM-003` execution timeline
- [ ] `GG-PREV-001` preview eligibility
- [ ] `GG-PREV-002` safe preview fixtures

## PHASE I — Export

- [ ] `GG-EXP-001` complete Android Studio project
- [ ] `GG-EXP-002` feature/module package
- [ ] `GG-EXP-003` README generation
- [ ] `GG-EXP-004` Python requirements/Dockerfile

## PHASE J — Library and diagnostics

- [ ] `GG-LIB-001` full-content project search
- [ ] `GG-LIB-002` persisted tags/categories
- [ ] `GG-LIB-003` language/project badges
- [ ] `GG-LIB-004` collections
- [ ] `GG-DIAG-001` performance metrics
- [ ] `GG-DIAG-002` sanitized diagnostics

---

# 82. First Vertical Slice

Do **not** start by attempting a full generated Android application.

Build this exact vertical slice first:

```text
Home
  ↓
Select Kotlin / Android
  ↓
Select "Compose Screen"
  ↓
Prompt Gemini/Ollama using Kotlin policy
  ↓
Receive Kotlin
  ↓
Strip Kotlin Markdown fence correctly
  ↓
Analyze with Kotlin analyzer
  ↓
Save with KOTLIN_ANDROID metadata
  ↓
Load from library
  ↓
Language restored correctly
```

Then add:

```text
Improve
   ↓
Repair findings
   ↓
Analyze again
```

Then expand:

```text
single Kotlin file
        ↓
multi-file Android feature
        ↓
complete Android app
```

This minimizes risk and validates the architectural seam before the project model becomes large.

---

# 83. First Kotlin Offline Templates to Implement

Recommended order:

### 1. Compose Screen

```text
Screen.kt
UiState
callbacks
preview state
```

### 2. ViewModel Feature

```text
Screen.kt
ViewModel.kt
UiState.kt
```

### 3. Persistent Feature

```text
Screen.kt
ViewModel.kt
Repository.kt
Entity.kt
Dao.kt
Database.kt
```

### 4. Network Feature

```text
Screen.kt
ViewModel.kt
Repository.kt
ApiService.kt
Dto.kt
```

### 5. Background Automation

```text
Worker.kt
WorkRequest factory
notification helper
Manifest/permission metadata
```

Once those are reliable, move to complete applications.

---

# 84. Product Milestones

## Milestone 1 — Kotlin Actually Works

Definition of done:

- Kotlin selector
- typed language
- no hardcoded Python generation path
- Kotlin-specific prompts
- Kotlin offline templates
- Kotlin analysis
- correct save/load
- Python regression tests green

At this point Galaxy Guardian can legitimately say:

> Supports Kotlin/Android and Python.

---

## Milestone 2 — Android Feature Forge

Definition of done:

User:

> “Build me a habit tracker feature with streaks and local persistence.”

Galaxy Guardian returns:

```text
HabitScreen.kt
HabitViewModel.kt
HabitUiState.kt
HabitRepository.kt
HabitEntity.kt
HabitDao.kt
HabitDatabase.kt
navigation requirements
Gradle requirements
Manifest requirements
```

All files are analyzed as one project.

---

## Milestone 3 — Self-Improving Forge

Definition of done:

```text
Generate
Analyze
Repair
Analyze again
Diff
Version
```

No destructive overwrite.

---

## Milestone 4 — Android Application Builder

Definition of done:

User asks:

> “Build an Android app.”

Galaxy Guardian generates an Android Studio-ready project structure rather than one Kotlin snippet.

---

## Milestone 5 — Interactive Forge

Definition of done:

- scenario simulation
- architecture timeline
- constrained preview
- diagnostics
- export
- Git integration

---

# 85. Safety Boundary

Keep the strongest design choice in the existing product:

> Generated untrusted code does not automatically execute with Galaxy Guardian's application privileges.

Kotlin does not change that.

The app may:

```text
generate
analyze
simulate
preview constrained known structures
export
```

It must not casually:

```text
compile arbitrary generated code
load it into the host process
grant it Galaxy Guardian permissions
```

Dynamic modules/plugins can remain an experimental future research track.

---

# 86. AI Implementation Rules

Any AI coding agent working this backlog must follow these rules:

1. Inspect current source before changing a class.
2. Treat commit `0c99137...` or its descendant as the implementation baseline.
3. Preserve Python behavior.
4. Do not rebuild features that already exist.
5. Use typed domain models instead of additional String flags.
6. Separate provider logic from language logic.
7. Separate language logic from personality.
8. Separate analyzers by supported language.
9. Never silently fall back from requested Kotlin to generated Python.
10. Never report failed analysis as clean.
11. Never destroy existing SQLite data.
12. Every schema change requires migration tests.
13. Preserve existing saved Python bots.
14. Every architectural change requires unit coverage.
15. Do not introduce TODO/stub implementations and call a task finished.
16. Do not claim simulation is execution.
17. Do not log secrets.
18. Do not weaken public-network security merely to make Ollama easier.
19. Run lint/tests/build after every implementation unit.
20. Keep main buildable after each completed phase.

---

# 87. Most Important Architectural Insight From the Actual Repository

Galaxy Guardian is closer to this pivot than it looked from the pasted conversation.

The code already has:

```text
targetLanguage
provider abstraction
router
offline generation
analysis
simulation
persistence
personality
responsive Compose UI
CI
```

The real blocker is that all of those systems still terminate in a **single Python code string**.

So the most important transformation is:

```text
String targetLanguage
        ↓
TargetLanguage domain model

Single generated String
        ↓
GeneratedProject + GeneratedFile[]

Python-only analyzer
        ↓
Analyzer registry

Python bot prompt
        ↓
Language + target prompt policies

BotProject
        ↓
General project/version model
```

Once those boundaries exist, Galaxy Guardian stops being:

> a Python bot generator that happens to run on Android

and becomes:

> an Android AI engineering environment that can still build excellent Python bots.

That should be the vNext architecture.