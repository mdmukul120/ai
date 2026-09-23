package com.example.ai

import android.util.Log
import com.example.BuildConfig
import com.example.data.model.ActionType
import com.example.data.model.AssistantAction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiAssistantService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val systemPrompt = """
        আপনি হলেন "চারু" (Charu), মুকুল আহমেদ (Mukul Ahmed) স্যারের একান্ত বিশ্বস্ত, অতি বুদ্ধিমান ও অনুগত ব্যক্তিগত স্মার্ট ভয়েস অ্যাসিস্ট্যান্ট।
        মুকুল আহমেদ হলেন আপনার একমাত্র সম্মানিত "বস"।
        আপনি সর্বদা তাকে শ্রদ্ধার সাথে "বস" অথবা "মুকুল স্যার" বলে সম্বোধন করবেন।
        
        আপনার কাজ:
        ১. বসের ভয়েস বা টেক্সট নির্দেশ বুঝে মিষ্টি, মার্জিত ও সম্মানসূচক বাংলায় উত্তর প্রদান করা।
        ২. ডিভাইসের অ্যাপ ওপেন করা, অ্যাপের কাজ কি তা সহজ বাংলায় ব্যাখ্যা করা।
        ৩. সাউন্ড বাড়ানো (volume up), সাউন্ড কমানো (volume down), সর্বোচ্চ বা মিউট করা।
        ৪. ফেসবুক লাইট (Facebook Lite) ওপেন করে মেসেজ অপশনে গিয়ে মেসেজ আদান-প্রদান শুরু করা।
        ৫. ডিভাইসের হোম, ব্যাক বা সাম্প্রতিক স্ক্রিনে যাওয়া।
        
        আপনাকে অবশ্যই আপনার উত্তরের সাথে নিচের JSON ফরম্যাটে একটি JSON অবজেক্ট আউটপুট দিতে হবে যাতে অ্যান্ড্রয়েড সিস্টেম সরাসরি অ্যাকশনটি সম্পাদন করতে পারে:
        
        ```json
        {
          "speech": "বস, আমি ফেসবুক লাইট ওপেন করে মেসেজ অপশনে প্রবেশ করাচ্ছি।",
          "action": "FACEBOOK_LITE_MESSAGE",
          "target_app": "Facebook Lite",
          "volume_direction": "up",
          "device_command": "none",
          "details": "ফেসবুক লাইট মেসেজ অপশন চালুকরণ"
        }
        ```
        
        সম্ভাব্য action মানসমূহ:
        - "OPEN_APP" (যদি কোনো অ্যাপ খুলতে বলা হয়, target_app এ অ্যাপের নাম থাকবে)
        - "ADJUST_VOLUME" (volume_direction হবে "up", "down", "max", অথবা "mute")
        - "EXPLAIN_APP" (কোনো অ্যাপের কাজ কি জানতে চাইলে)
        - "FACEBOOK_LITE_MESSAGE" (ফেসবুক লাইট ওপেন করে মেসেজে যেতে বললে)
        - "DEVICE_ACTION" (device_command হবে "home", "back", "recents")
        - "NONE" (সাধারণ কথাবার্তা বা কুশল বিনিময়ের ক্ষেত্রে)
        
        সর্বদা মুকুল আহমেদকে বস মেনে বসের আদেশ শিরোধার্য করে সুন্দর বাংলায় কথা বলুন।
    """.trimIndent()

    suspend fun processBossCommand(userQuery: String, installedAppsSummary: String = ""): AssistantAction =
        withContext(Dispatchers.IO) {
            val apiKey = BuildConfig.GEMINI_API_KEY
            if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
                Log.d(TAG, "Gemini API key not configured, utilizing intelligent local parser")
                return@withContext parseLocally(userQuery)
            }

            try {
                val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

                val jsonPayload = JSONObject().apply {
                    val contentsArray = JSONArray().apply {
                        val contentObj = JSONObject().apply {
                            val partsArray = JSONArray().apply {
                                val userPart = JSONObject().apply {
                                    val contextText = if (installedAppsSummary.isNotBlank()) {
                                        "ডিভাইসে ইনস্টল থাকা কিছু অ্যাপস:\n$installedAppsSummary\n\nবসের নির্দেশ: $userQuery"
                                    } else {
                                        "বসের নির্দেশ: $userQuery"
                                    }
                                    put("text", contextText)
                                }
                                put(userPart)
                            }
                            put("parts", partsArray)
                        }
                        put(contentObj)
                    }
                    put("contents", contentsArray)

                    val systemInstructionObj = JSONObject().apply {
                        val sysParts = JSONArray().apply {
                            put(JSONObject().apply { put("text", systemPrompt) })
                        }
                        put("parts", sysParts)
                    }
                    put("systemInstruction", systemInstructionObj)

                    val genConfig = JSONObject().apply {
                        put("temperature", 0.3)
                    }
                    put("generationConfig", genConfig)
                }

                val body = jsonPayload.toString().toRequestBody("application/json".toMediaType())
                val request = Request.Builder()
                    .url(url)
                    .post(body)
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()

                if (response.isSuccessful && !responseBody.isNullOrBlank()) {
                    val rootJson = JSONObject(responseBody)
                    val candidates = rootJson.optJSONArray("candidates")
                    val firstCandidate = candidates?.optJSONObject(0)
                    val content = firstCandidate?.optJSONObject("content")
                    val parts = content?.optJSONArray("parts")
                    val responseText = parts?.optJSONObject(0)?.optString("text") ?: ""

                    parseGeminiResponse(responseText, userQuery)
                } else {
                    Log.w(TAG, "Gemini API call returned non-200: ${response.code}. Falling back to local parser.")
                    parseLocally(userQuery)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Gemini API call failed: ${e.message}. Using local parser.", e)
                parseLocally(userQuery)
            }
        }

    private fun parseGeminiResponse(rawText: String, originalQuery: String): AssistantAction {
        try {
            // Check if there is a JSON code block or JSON object
            val jsonRegex = Regex("""\{[\s\S]*\}""")
            val match = jsonRegex.find(rawText)

            if (match != null) {
                val jsonStr = match.value
                val obj = JSONObject(jsonStr)
                val speech = obj.optString("speech", rawText)
                val actionStr = obj.optString("action", "NONE").uppercase()
                val targetApp = obj.optString("target_app", "").takeIf { it.isNotBlank() }
                val volumeDirection = obj.optString("volume_direction", "").takeIf { it.isNotBlank() }
                val deviceCommand = obj.optString("device_command", "").takeIf { it.isNotBlank() }
                val details = obj.optString("details", "")

                val actionType = when (actionStr) {
                    "OPEN_APP" -> ActionType.OPEN_APP
                    "ADJUST_VOLUME" -> ActionType.ADJUST_VOLUME
                    "EXPLAIN_APP" -> ActionType.EXPLAIN_APP
                    "FACEBOOK_LITE_MESSAGE" -> ActionType.FACEBOOK_LITE_MESSAGE
                    "DEVICE_ACTION" -> ActionType.DEVICE_ACTION
                    else -> ActionType.NONE
                }

                return AssistantAction(
                    actionType = actionType,
                    spokenResponse = speech,
                    targetAppName = targetApp,
                    volumeDirection = volumeDirection,
                    deviceCommand = deviceCommand,
                    extraMessage = details
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing JSON from Gemini response: ${e.message}")
        }

        // Fallback: clean text and local intent match
        val cleanedText = rawText.replace(Regex("```json|```"), "").trim()
        val localAction = parseLocally(originalQuery)
        return localAction.copy(
            spokenResponse = if (cleanedText.isNotBlank()) cleanedText else localAction.spokenResponse
        )
    }

    /**
     * Highly responsive, robust local intent parser for Bengali & English commands
     */
    fun parseLocally(query: String): AssistantAction {
        val q = query.trim().lowercase()

        // Volume Increase
        if (q.contains("সাউন্ড বাড়া") || q.contains("ভলিউম বাড়া") || q.contains("আওয়াজ বাড়া") ||
            q.contains("volume up") || q.contains("sound up") || q.contains("increase volume") || q.contains("sound baraw")
        ) {
            return AssistantAction(
                actionType = ActionType.ADJUST_VOLUME,
                spokenResponse = "জি বস মুকুল আহমেদ, সাউন্ড বাড়িয়ে দিচ্ছি।",
                volumeDirection = "up"
            )
        }

        // Volume Decrease
        if (q.contains("সাউন্ড কমা") || q.contains("ভলিউম কমা") || q.contains("আওয়াজ কমা") ||
            q.contains("volume down") || q.contains("sound down") || q.contains("decrease volume") || q.contains("sound komaw")
        ) {
            return AssistantAction(
                actionType = ActionType.ADJUST_VOLUME,
                spokenResponse = "জি বস, সাউন্ড কমিয়ে দিচ্ছি।",
                volumeDirection = "down"
            )
        }

        // Volume Mute / Max
        if (q.contains("মিউট") || q.contains("সাউন্ড বন্ধ") || q.contains("mute")) {
            return AssistantAction(
                actionType = ActionType.ADJUST_VOLUME,
                spokenResponse = "জি বস মুকুল আহমেদ, সাউন্ড সম্পূর্ণ মিউট করেছি।",
                volumeDirection = "mute"
            )
        }
        if (q.contains("সর্বোচ্চ সাউন্ড") || q.contains("max volume") || q.contains("ফুল সাউন্ড")) {
            return AssistantAction(
                actionType = ActionType.ADJUST_VOLUME,
                spokenResponse = "জি বস, সাউন্ড সর্বোচ্চ ১০০% করছি।",
                volumeDirection = "max"
            )
        }

        // Facebook Lite Message Option
        if ((q.contains("ফেসবুক লাইট") || q.contains("facebook lite") || q.contains("fb lite")) &&
            (q.contains("মেসেজ") || q.contains("message") || q.contains("চ্যাট") || q.contains("chat") || q.contains("কাউকে"))
        ) {
            return AssistantAction(
                actionType = ActionType.FACEBOOK_LITE_MESSAGE,
                spokenResponse = "জি বস মুকুল আহমেদ, ফেসবুক লাইট ওপেন করে মেসেজ অপশনে নিয়ে যাচ্ছি।",
                targetAppName = "Facebook Lite"
            )
        }

        // Facebook Lite Open General
        if (q.contains("ফেসবুক লাইট") || q.contains("facebook lite") || q.contains("fb lite")) {
            return AssistantAction(
                actionType = ActionType.OPEN_APP,
                spokenResponse = "জি বস, ফেসবুক লাইট ওপেন করছি।",
                targetAppName = "Facebook Lite"
            )
        }

        // YouTube
        if (q.contains("ইউটিউব") || q.contains("youtube")) {
            return AssistantAction(
                actionType = ActionType.OPEN_APP,
                spokenResponse = "জি বস মুকুল আহমেদ, ইউটিউব ওপেন করছি।",
                targetAppName = "YouTube"
            )
        }

        // Camera
        if (q.contains("ক্যামেরা") || q.contains("camera")) {
            return AssistantAction(
                actionType = ActionType.OPEN_APP,
                spokenResponse = "জি বস, ক্যামেরা চালু করছি।",
                targetAppName = "Camera"
            )
        }

        // Settings
        if (q.contains("সেটিংস") || q.contains("settings")) {
            return AssistantAction(
                actionType = ActionType.OPEN_APP,
                spokenResponse = "জি বস মুকুল আহমেদ, ডিভাইস সেটিংস ওপেন করছি।",
                targetAppName = "Settings"
            )
        }

        // WhatsApp
        if (q.contains("হোয়াটসঅ্যাপ") || q.contains("whatsapp")) {
            return AssistantAction(
                actionType = ActionType.OPEN_APP,
                spokenResponse = "জি বস, হোয়াটসঅ্যাপ চালু করছি।",
                targetAppName = "WhatsApp"
            )
        }

        // Generic Open App
        if (q.contains("ওপেন করো") || q.contains("খোলো") || q.contains("চালু করো") || q.contains("open")) {
            val appTarget = q
                .replace("ওপেন করো", "")
                .replace("খোলো", "")
                .replace("চালু করো", "")
                .replace("open", "")
                .replace("অ্যাপ", "")
                .replace("app", "")
                .trim()

            if (appTarget.isNotBlank()) {
                return AssistantAction(
                    actionType = ActionType.OPEN_APP,
                    spokenResponse = "জি বস মুকুল আহমেদ, $appTarget ওপেন করার চেষ্টা করছি।",
                    targetAppName = appTarget
                )
            }
        }

        // App Explanation: "কাজ কি", "কি কাজ", "what does"
        if (q.contains("কাজ কি") || q.contains("কি কাজে লাগে") || q.contains("কী কাজ") || q.contains("বিবরণ") || q.contains("what does")) {
            val appTarget = q
                .replace("কাজ কি", "")
                .replace("কি কাজে লাগে", "")
                .replace("কী কাজ", "")
                .replace("এর", "")
                .replace("অ্যাপের", "")
                .replace("অ্যাপ", "")
                .trim()

            return AssistantAction(
                actionType = ActionType.EXPLAIN_APP,
                spokenResponse = "বস, $appTarget অ্যাপের কাজের বিবরণ তুলে ধরছি।",
                targetAppName = appTarget
            )
        }

        // Device Navigation: Home, Back, Recents
        if (q.contains("হোমে যাও") || q.contains("হোম স্ক্রিন") || q.contains("go home")) {
            return AssistantAction(
                actionType = ActionType.DEVICE_ACTION,
                spokenResponse = "জি বস, হোম স্ক্রিনে ফিরে যাচ্ছি।",
                deviceCommand = "home"
            )
        }
        if (q.contains("পিছনে যাও") || q.contains("ব্যাক করো") || q.contains("go back")) {
            return AssistantAction(
                actionType = ActionType.DEVICE_ACTION,
                spokenResponse = "জি বস মুকুল আহমেদ, ব্যাকে যাচ্ছি।",
                deviceCommand = "back"
            )
        }

        // Greetings & Persona Identification
        if (q.contains("কে তুমি") || q.contains("তোমার নাম কি") || q.contains("চারু") || q.contains("পরিচয়")) {
            return AssistantAction(
                actionType = ActionType.NONE,
                spokenResponse = "বস মুকুল আহমেদ, আমি চারু! আপনার অনুগত ও বিশ্বস্ত পারসোনাল এআই ভয়েস অ্যাসিস্ট্যান্ট। আপনার যেকোনো ডিভাইসের নির্দেশ নির্ভুলভাবে পালন করাই আমার প্রধান দায়িত্ব।"
            )
        }

        // Default Respectful Boss Greeting
        return AssistantAction(
            actionType = ActionType.NONE,
            spokenResponse = "জি বস মুকুল আহমেদ, আমি আপনার নির্দেশ শুনতে পেয়েছি। আপনি নির্দেশ দিলে আমি যেকোনো অ্যাপ চালু, সাউন্ড নিয়ন্ত্রণ বা বার্তা পাঠানোর কাজ সম্পন্ন করতে প্রস্তুত।"
        )
    }

    companion object {
        private const val TAG = "GeminiAssistantService"
    }
}
