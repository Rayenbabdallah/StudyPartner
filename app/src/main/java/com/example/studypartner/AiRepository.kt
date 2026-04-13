package com.example.studypartner

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object AiRepository {

    private const val TAG      = "AiRepository"
    private const val BASE_URL = "https://openrouter.ai/api/v1"
    private const val MODEL    = "meta-llama/llama-3.2-3b-instruct:free"
    private const val TIMEOUT  = 30_000

    suspend fun getAdvice(tasks: List<StudyTask>, context: Context): Result<String> =
        withContext(Dispatchers.IO) {
            runCatching {
                val apiKey = UserPreferences.openRouterApiKey(context).first()
                if (apiKey.isBlank()) {
                    Log.w(TAG, "getAdvice: API Key is blank")
                    return@runCatching "Please set your OpenRouter API Key in settings."
                }

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
                val response = if (conn.responseCode == 200) {
                    conn.inputStream.bufferedReader().use { it.readText() }
                } else {
                    val error = conn.errorStream?.bufferedReader()?.use { it.readText() } ?: "Unknown error"
                    Log.e(TAG, "getAdvice: API Error ${conn.responseCode}: $error")
                    throw Exception("API Error ${conn.responseCode}: $error")
                }
                
                JSONObject(response)
                    .getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content")
                    .trim()
            }.onFailure {
                Log.e(TAG, "getAdvice: Failed to fetch AI advice", it)
            }
        }

    suspend fun getRescuePlan(panicTasks: List<StudyTask>, allTasks: List<StudyTask>, context: Context): Result<String> =
        withContext(Dispatchers.IO) {
            runCatching {
                val apiKey = UserPreferences.openRouterApiKey(context).first()
                if (apiKey.isBlank()) {
                    Log.w(TAG, "getRescuePlan: API Key is blank")
                    return@runCatching "Please set your OpenRouter API Key in settings."
                }

                val body = JSONObject().apply {
                    put("model",  MODEL)
                    put("messages", JSONArray().apply {
                        put(JSONObject().apply {
                            put("role", "user")
                            put("content", buildRescuePlanPrompt(panicTasks, allTasks))
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
                val response = if (conn.responseCode == 200) {
                    conn.inputStream.bufferedReader().use { it.readText() }
                } else {
                    val error = conn.errorStream?.bufferedReader()?.use { it.readText() } ?: "Unknown error"
                    Log.e(TAG, "getRescuePlan: API Error ${conn.responseCode}: $error")
                    throw Exception("API Error ${conn.responseCode}: $error")
                }

                JSONObject(response)
                    .getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content")
                    .trim()
            }.onFailure {
                Log.e(TAG, "getRescuePlan: Failed to fetch AI rescue plan", it)
            }
        }

    suspend fun getAssistantResponse(tasks: List<StudyTask>, question: String, context: Context): Result<String> =
        withContext(Dispatchers.IO) {
            runCatching {
                val apiKey = UserPreferences.openRouterApiKey(context).first()
                if (apiKey.isBlank()) {
                    Log.w(TAG, "getAssistantResponse: API Key is blank")
                    return@runCatching "Please set your OpenRouter API Key in settings."
                }

                val body = JSONObject().apply {
                    put("model",  MODEL)
                    put("messages", JSONArray().apply {
                        put(JSONObject().apply {
                            put("role", "user")
                            put("content", buildAssistantPrompt(tasks, question))
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
                val response = if (conn.responseCode == 200) {
                    conn.inputStream.bufferedReader().use { it.readText() }
                } else {
                    val error = conn.errorStream?.bufferedReader()?.use { it.readText() } ?: "Unknown error"
                    Log.e(TAG, "getAssistantResponse: API Error ${conn.responseCode}: $error")
                    throw Exception("API Error ${conn.responseCode}: $error")
                }

                JSONObject(response)
                    .getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content")
                    .trim()
            }.onFailure {
                Log.e(TAG, "getAssistantResponse: Failed to fetch AI assistant response", it)
            }
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
