package com.example.studypartner

import android.content.Context
import android.util.Log
import aws.sdk.kotlin.runtime.auth.credentials.StaticCredentialsProvider
import aws.sdk.kotlin.services.bedrockruntime.BedrockRuntimeClient
import aws.sdk.kotlin.services.bedrockruntime.model.InvokeModelRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object AiRepository {

    private const val TAG          = "AiRepository"
    private const val BASE_URL     = "https://openrouter.ai/api/v1"
    private const val MODEL        = "google/gemma-2-9b-it:free"
    private const val TIMEOUT      = 30_000
    private const val NOVA_MODEL_ID = "amazon.nova-lite-v1:0"

    suspend fun getAdvice(tasks: List<StudyTask>, context: Context): Result<String> =
        withContext(Dispatchers.IO) {
            val openRouterResult = runCatching {
                val apiKey = UserPreferences.openRouterApiKey(context).first()
                if (apiKey.isBlank()) throw Exception("API Key is blank")

                val body = JSONObject().apply {
                    put("model", MODEL)
                    put("messages", JSONArray().apply {
                        put(JSONObject().apply {
                            put("role", "user")
                            put("content", buildPrompt(tasks))
                        })
                    })
                }.toString()

                val conn = (URL("$BASE_URL/chat/completions").openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    setRequestProperty("Content-Type", "application/json")
                    setRequestProperty("Authorization", "Bearer $apiKey")
                    setRequestProperty("HTTP-Referer", "https://github.com/rayen/StudyPartner")
                    setRequestProperty("X-Title", "StudyPartner")
                    connectTimeout = TIMEOUT
                    readTimeout    = TIMEOUT
                    doOutput       = true
                }

                conn.outputStream.use { it.write(body.toByteArray()) }
                
                if (conn.responseCode == 429) {
                    throw Exception("Rate limit reached (429)")
                }

                val response = if (conn.responseCode == 200) {
                    conn.inputStream.bufferedReader().use { it.readText() }
                } else {
                    val error = conn.errorStream?.bufferedReader()?.use { it.readText() } ?: "Unknown error"
                    throw Exception("API Error ${conn.responseCode}: $error")
                }
                
                JSONObject(response)
                    .getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content")
                    .trim()
            }

            if (openRouterResult.isSuccess) return@withContext openRouterResult

            Log.w(TAG, "OpenRouter failed, attempting AWS Bedrock fallback: ${openRouterResult.exceptionOrNull()?.message}")
            getBedrockFallback(buildPrompt(tasks), context)
        }

    suspend fun getRescuePlan(panicTasks: List<StudyTask>, allTasks: List<StudyTask>, context: Context): Result<String> =
        withContext(Dispatchers.IO) {
            val prompt = buildRescuePlanPrompt(panicTasks, allTasks)
            val openRouterResult = runCatching {
                val apiKey = UserPreferences.openRouterApiKey(context).first()
                if (apiKey.isBlank()) throw Exception("API Key is blank")

                val body = JSONObject().apply {
                    put("model",  MODEL)
                    put("messages", JSONArray().apply {
                        put(JSONObject().apply {
                            put("role", "user")
                            put("content", prompt)
                        })
                    })
                }.toString()

                val conn = (URL("$BASE_URL/chat/completions").openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    setRequestProperty("Content-Type", "application/json")
                    setRequestProperty("Authorization", "Bearer $apiKey")
                    connectTimeout = TIMEOUT
                    readTimeout    = TIMEOUT
                    doOutput       = true
                }
                conn.outputStream.use { it.write(body.toByteArray()) }

                if (conn.responseCode == 429) {
                    throw Exception("Rate limit reached (429)")
                }

                val response = if (conn.responseCode == 200) {
                    conn.inputStream.bufferedReader().use { it.readText() }
                } else {
                    val error = conn.errorStream?.bufferedReader()?.use { it.readText() } ?: "Unknown error"
                    throw Exception("API Error ${conn.responseCode}: $error")
                }

                JSONObject(response)
                    .getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content")
                    .trim()
            }

            if (openRouterResult.isSuccess) return@withContext openRouterResult

            Log.w(TAG, "OpenRouter failed, attempting AWS Bedrock fallback: ${openRouterResult.exceptionOrNull()?.message}")
            getBedrockFallback(prompt, context)
        }

    suspend fun getAssistantResponse(tasks: List<StudyTask>, question: String, context: Context): Result<String> =
        withContext(Dispatchers.IO) {
            val prompt = buildAssistantPrompt(tasks, question)
            val openRouterResult = runCatching {
                val apiKey = UserPreferences.openRouterApiKey(context).first()
                if (apiKey.isBlank()) throw Exception("API Key is blank")

                val body = JSONObject().apply {
                    put("model",  MODEL)
                    put("messages", JSONArray().apply {
                        put(JSONObject().apply {
                            put("role", "user")
                            put("content", prompt)
                        })
                    })
                }.toString()

                val conn = (URL("$BASE_URL/chat/completions").openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    setRequestProperty("Content-Type", "application/json")
                    setRequestProperty("Authorization", "Bearer $apiKey")
                    connectTimeout = TIMEOUT
                    readTimeout    = TIMEOUT
                    doOutput       = true
                }
                conn.outputStream.use { it.write(body.toByteArray()) }

                if (conn.responseCode == 429) {
                    throw Exception("Rate limit reached (429)")
                }

                val response = if (conn.responseCode == 200) {
                    conn.inputStream.bufferedReader().use { it.readText() }
                } else {
                    val error = conn.errorStream?.bufferedReader()?.use { it.readText() } ?: "Unknown error"
                    throw Exception("API Error ${conn.responseCode}: $error")
                }

                JSONObject(response)
                    .getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content")
                    .trim()
            }

            if (openRouterResult.isSuccess) return@withContext openRouterResult

            Log.w(TAG, "OpenRouter failed, attempting AWS Bedrock fallback: ${openRouterResult.exceptionOrNull()?.message}")
            getBedrockFallback(prompt, context)
        }

    private suspend fun getBedrockFallback(prompt: String, context: Context): Result<String> =
        runCatching {
            val accessKey = UserPreferences.awsAccessKey(context).first()
            val secretKey = UserPreferences.awsSecretKey(context).first()
            val regionStr = UserPreferences.awsRegion(context).first()

            if (accessKey.isBlank() || secretKey.isBlank()) {
                throw Exception("AWS credentials not configured")
            }

            val payload = JSONObject().apply {
                put("messages", JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "user")
                        put("content", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", prompt)
                            })
                        })
                    })
                })
                put("inferenceConfig", JSONObject().apply {
                    put("max_new_tokens", 500)
                    put("temperature", 0.7)
                })
            }

            val client = BedrockRuntimeClient {
                region = regionStr
                credentialsProvider = StaticCredentialsProvider {
                    accessKeyId = accessKey
                    secretAccessKey = secretKey
                }
            }

            val request = InvokeModelRequest {
                modelId = NOVA_MODEL_ID
                body = payload.toString().toByteArray()
                accept = "application/json"
                contentType = "application/json"
            }

            val response = client.invokeModel(request)
            val responseBody = response.body?.decodeToString() ?: throw Exception("Empty response body")

            JSONObject(responseBody)
                .getJSONObject("output")
                .getJSONObject("message")
                .getJSONArray("content")
                .getJSONObject(0)
                .getString("text")
                .trim()
        }.onFailure {
            Log.e(TAG, "Bedrock fallback failed", it)
        }

    private fun buildRescuePlanPrompt(panicTasks: List<StudyTask>, allTasks: List<StudyTask>): String {
        val list = panicTasks.joinToString("\n") { t ->
            val days = t.daysUntilDeadline()
            val daysStr = when {
                days == null -> "no deadline"
                days < 0    -> "OVERDUE"
                days == 0   -> "due TODAY"
                else        -> "due in ${days}d"
            }
            "- [${t.taskType.label}] \"${t.title}\" | ${t.subject} | " +
            "${t.progress}% done | grade impact: ${String.format("%.0f", t.gradeWeight * 100)}% | $daysStr"
        }
        return """
            You are an emergency academic planning assistant. A student is in last-minute panic mode.

            Critical tasks (survival-sorted):
            $list

            Total active tasks: ${allTasks.count { !it.isCompleted }}

            Create a brutally honest, realistic rescue plan:
            1. Hour-by-hour plan for tonight (include short breaks)
            2. What to skip or do minimally to save time
            3. Expected outcome if they follow this plan (e.g., "estimated 12–14/20")
            4. One motivating sentence

            Be direct. No markdown. Plain text only. Maximum 10 sentences.
        """.trimIndent()
    }

    private fun buildAssistantPrompt(tasks: List<StudyTask>, question: String): String {
        val active = tasks.filter { !it.isCompleted }
        val snapshot = active.take(8).joinToString("\n") { t ->
            val dl = t.deadlineLabel().let { if (it.isNotEmpty()) "| $it" else "" }
            "- [${t.taskType.label}] \"${t.title}\" | Score ${String.format("%.0f", t.score())}/100 | ${t.progress}% done $dl"
        }
        return """
            You are a smart academic planning assistant embedded in a student planner app.
            The student has ${active.size} active task(s):

            $snapshot

            Student asks: "$question"

            Answer directly and specifically using the task data above.
            Be concrete, encouraging, and realistic. Maximum 5 sentences. No markdown.
        """.trimIndent()
    }

    private fun buildPrompt(tasks: List<StudyTask>): String {
        if (tasks.isEmpty()) return "Tell the student to add some tasks to get personalised advice."

        val active   = tasks.filter { !it.isCompleted }
        val overdue  = active.filter { it.isOverdue() }
        val critical = active.filter { it.score() >= 80 }

        val list = active.take(10).joinToString("\n") { task ->
            val deadlineStr = task.deadlineLabel().let { if (it.isNotEmpty()) it else "no deadline" }
            "- [${task.taskType.label}] \"${task.title}\" | ${task.subject} " +
            "| Score ${String.format("%.0f", task.score())}/100 " +
            "| Progress ${task.progress}% | $deadlineStr"
        }

        val context = buildString {
            if (overdue.isNotEmpty())  append("⚠ ${overdue.size} OVERDUE task(s). ")
            if (critical.isNotEmpty()) append("🔴 ${critical.size} CRITICAL priority task(s). ")
            append("${active.size} active tasks total.")
        }

        return """
            You are an academic planning assistant. A student has these active tasks:

            $list

            Situation: $context

            Give a concrete action plan in exactly 3 parts:
            1. DO NOW: the most critical task and why (one sentence).
            2. RISK ALERT: one specific risk if any task looks dangerous (one sentence, or "None" if safe).
            3. TIME ESTIMATE: how long to spend on the top task tonight (one sentence).

            Be direct. No markdown. No lists. No more than 4 sentences total.
        """.trimIndent()
    }
}
