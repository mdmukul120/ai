package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.GeminiAssistantService
import com.example.data.local.CharuDatabase
import com.example.data.local.CommandLogEntity
import com.example.data.model.ActionType
import com.example.data.model.AppInfo
import com.example.data.model.AssistantAction
import com.example.service.CharuAccessibilityService
import com.example.service.DeviceController
import com.example.speech.VoiceManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CharuUiState(
    val isListening: Boolean = false,
    val isSpeaking: Boolean = false,
    val isThinking: Boolean = false,
    val rmsDb: Float = 0f,
    val soundVolumePercent: Int = 50,
    val isTtsMuted: Boolean = false,
    val isAccessibilityEnabled: Boolean = false,
    val isMicPermissionGranted: Boolean = false,
    val installedApps: List<AppInfo> = emptyList(),
    val filteredApps: List<AppInfo> = emptyList(),
    val appsSearchQuery: String = "",
    val selectedAppForDetail: AppInfo? = null,
    val lastSpokenMessage: String = "বস মুকুল আহমেদ, আমি চারু। আমি সর্বদা আপনার নির্দেশ মেনে ডিভাইস নিয়ন্ত্রণ ও অ্যাপসের কাজ পরিচালনা করতে প্রস্তুত।",
    val lastUserQuery: String = "",
    val statusBanner: String = "চারু প্রস্তুত",
    val commandLogs: List<CommandLogEntity> = emptyList(),
    val selectedTab: Int = 0 // 0: ভয়েস ও কনসোল, 1: ডিভাইস অ্যাপস, 2: কন্ট্রোল
)

class CharuViewModel(application: Application) : AndroidViewModel(application) {

    private val db = CharuDatabase.getInstance(application)
    private val dao = db.commandLogDao()
    private val deviceController = DeviceController(application)
    private val geminiService = GeminiAssistantService()

    private val _uiState = MutableStateFlow(CharuUiState())
    val uiState: StateFlow<CharuUiState> = _uiState.asStateFlow()

    private var voiceManager: VoiceManager? = null

    init {
        // Observe database logs
        viewModelScope.launch {
            dao.getAllLogs().collect { logs ->
                _uiState.update { it.copy(commandLogs = logs) }
            }
        }

        // Initialize voice manager
        voiceManager = VoiceManager(
            context = application,
            onSpeechRecognized = { text ->
                handleUserInput(text)
            },
            onErrorOccurred = { errorMsg ->
                _uiState.update {
                    it.copy(
                        isListening = false,
                        statusBanner = errorMsg
                    )
                }
            }
        )

        // Observe voice manager states
        viewModelScope.launch {
            voiceManager?.isListening?.collect { listening ->
                _uiState.update { it.copy(isListening = listening) }
            }
        }

        viewModelScope.launch {
            voiceManager?.isSpeaking?.collect { speaking ->
                _uiState.update { it.copy(isSpeaking = speaking) }
            }
        }

        viewModelScope.launch {
            voiceManager?.rmsAudioDb?.collect { rms ->
                _uiState.update { it.copy(rmsDb = rms) }
            }
        }

        refreshDeviceState()
        loadInstalledApps()
    }

    fun refreshDeviceState() {
        val app = getApplication<Application>()
        val accEnabled = CharuAccessibilityService.isAccessibilitySettingsOn(app)
        val vol = deviceController.getCurrentVolumePercent()

        _uiState.update {
            it.copy(
                isAccessibilityEnabled = accEnabled,
                soundVolumePercent = vol
            )
        }
    }

    fun setMicPermissionGranted(granted: Boolean) {
        _uiState.update { it.copy(isMicPermissionGranted = granted) }
    }

    fun setTab(index: Int) {
        _uiState.update { it.copy(selectedTab = index) }
    }

    fun toggleVoiceListening() {
        if (_uiState.value.isListening) {
            voiceManager?.stopListening()
        } else {
            voiceManager?.startListening()
        }
    }

    fun toggleTtsMute() {
        val newMute = !_uiState.value.isTtsMuted
        if (newMute) {
            voiceManager?.stopSpeaking()
        }
        _uiState.update { it.copy(isTtsMuted = newMute) }
    }

    fun handleUserInput(query: String) {
        if (query.isBlank()) return

        _uiState.update {
            it.copy(
                lastUserQuery = query,
                isThinking = true,
                statusBanner = "নির্দেশ বিশ্লেষণ করছি..."
            )
        }

        viewModelScope.launch {
            // Log boss's input
            dao.insertLog(
                CommandLogEntity(
                    sender = "boss",
                    messageText = query,
                    actionType = "USER_VOICE_COMMAND"
                )
            )

            // Prepare summary of apps for Gemini context
            val appSummary = _uiState.value.installedApps
                .take(15)
                .joinToString(", ") { "${it.appName} (${it.category})" }

            // Process with Gemini AI or intelligent local parser
            val action = geminiService.processBossCommand(query, appSummary)

            // Execute the resolved action
            executeAssistantAction(action)
        }
    }

    private suspend fun executeAssistantAction(action: AssistantAction) {
        var actionExecutionDetails = ""
        var isSuccess = true

        when (action.actionType) {
            ActionType.ADJUST_VOLUME -> {
                when (action.volumeDirection?.lowercase()) {
                    "up" -> {
                        val result = deviceController.volumeUp()
                        _uiState.update { it.copy(soundVolumePercent = result.first) }
                        actionExecutionDetails = result.second
                    }
                    "down" -> {
                        val result = deviceController.volumeDown()
                        _uiState.update { it.copy(soundVolumePercent = result.first) }
                        actionExecutionDetails = result.second
                    }
                    "mute" -> {
                        val result = deviceController.muteVolume()
                        _uiState.update { it.copy(soundVolumePercent = result.first) }
                        actionExecutionDetails = result.second
                    }
                    "max" -> {
                        val result = deviceController.maxVolume()
                        _uiState.update { it.copy(soundVolumePercent = result.first) }
                        actionExecutionDetails = result.second
                    }
                }
            }

            ActionType.FACEBOOK_LITE_MESSAGE -> {
                val fbResult = deviceController.openFacebookLiteOrFallback(action.extraMessage)
                isSuccess = fbResult.first
                actionExecutionDetails = fbResult.second
            }

            ActionType.OPEN_APP -> {
                val target = action.targetAppName ?: action.targetPackage ?: ""
                val launchResult = deviceController.launchAppByName(target)
                isSuccess = launchResult.first
                actionExecutionDetails = launchResult.second
            }

            ActionType.EXPLAIN_APP -> {
                val target = action.targetAppName ?: ""
                val matchedApp = _uiState.value.installedApps.firstOrNull {
                    it.appName.contains(target, ignoreCase = true) ||
                            it.packageName.contains(target, ignoreCase = true)
                }
                if (matchedApp != null) {
                    _uiState.update { it.copy(selectedAppForDetail = matchedApp) }
                    actionExecutionDetails = "${matchedApp.appName}: ${matchedApp.description}"
                } else {
                    actionExecutionDetails = "বস, '$target' নামের অ্যাপের বিবরণ খোঁজা হচ্ছে।"
                }
            }

            ActionType.DEVICE_ACTION -> {
                when (action.deviceCommand?.lowercase()) {
                    "home" -> {
                        isSuccess = CharuAccessibilityService.triggerHome()
                        actionExecutionDetails = if (isSuccess) "হোম স্ক্রিনে যাওয়া হয়েছে" else "হোম স্ক্রিন যেতে পারমিশন প্রয়োজন"
                    }
                    "back" -> {
                        isSuccess = CharuAccessibilityService.triggerBack()
                        actionExecutionDetails = if (isSuccess) "ব্যাক স্ক্রিনে যাওয়া হয়েছে" else "ব্যাক যেতে পারমিশন প্রয়োজন"
                    }
                    "recents" -> {
                        isSuccess = CharuAccessibilityService.triggerRecents()
                        actionExecutionDetails = "সাম্প্রতিক অ্যাপস দেখানো হয়েছে"
                    }
                    "notifications" -> {
                        isSuccess = CharuAccessibilityService.triggerNotifications()
                        actionExecutionDetails = "নোটিফিকেশন প্যানেল খোলা হয়েছে"
                    }
                }
            }

            ActionType.NONE -> {
                actionExecutionDetails = "সাধারণ আলাপচারিতা"
            }
        }

        val speechToSpeak = action.spokenResponse

        _uiState.update {
            it.copy(
                isThinking = false,
                lastSpokenMessage = speechToSpeak,
                statusBanner = "কার্যকর হয়েছে: $actionExecutionDetails"
            )
        }

        // Voice output
        voiceManager?.speak(speechToSpeak, isMuted = _uiState.value.isTtsMuted)

        // Log assistant reply
        dao.insertLog(
            CommandLogEntity(
                sender = "charu",
                messageText = speechToSpeak,
                actionType = action.actionType.name,
                actionDetails = actionExecutionDetails,
                isSuccess = isSuccess
            )
        )
    }

    // Direct Quick Actions for Mukul Ahmed Boss
    fun volumeUpDirect() {
        val result = deviceController.volumeUp()
        _uiState.update {
            it.copy(
                soundVolumePercent = result.first,
                lastSpokenMessage = result.second
            )
        }
        voiceManager?.speak(result.second, isMuted = _uiState.value.isTtsMuted)
    }

    fun volumeDownDirect() {
        val result = deviceController.volumeDown()
        _uiState.update {
            it.copy(
                soundVolumePercent = result.first,
                lastSpokenMessage = result.second
            )
        }
        voiceManager?.speak(result.second, isMuted = _uiState.value.isTtsMuted)
    }

    fun setVolumeDirect(percent: Int) {
        val result = deviceController.setVolumePercent(percent)
        _uiState.update {
            it.copy(
                soundVolumePercent = result.first,
                lastSpokenMessage = result.second
            )
        }
        voiceManager?.speak(result.second, isMuted = _uiState.value.isTtsMuted)
    }

    fun launchFacebookLiteMessagesDirect() {
        handleUserInput("ফেসবুক লাইট ওপেন করে মেসেজ অপশনে যাও")
    }

    fun launchAppDirect(app: AppInfo) {
        val launched = deviceController.launchApp(app.packageName)
        val msg = if (launched) {
            "বস মুকুল আহমেদ, ${app.appName} চালু করা হয়েছে।"
        } else {
            "বস, ${app.appName} চালু করা সম্ভব হয়নি।"
        }
        _uiState.update { it.copy(lastSpokenMessage = msg) }
        voiceManager?.speak(msg, isMuted = _uiState.value.isTtsMuted)
    }

    fun selectAppForDetail(app: AppInfo?) {
        _uiState.update { it.copy(selectedAppForDetail = app) }
        if (app != null) {
            val speech = "বস, ${app.appName} হলো একটি ${app.category}। ${app.description}"
            voiceManager?.speak(speech, isMuted = _uiState.value.isTtsMuted)
        }
    }

    fun explainApp(app: AppInfo) {
        val speech = "বস মুকুল আহমেদ, ${app.appName} এর মূল কাজ হলো: ${app.description}"
        _uiState.update { it.copy(lastSpokenMessage = speech) }
        voiceManager?.speak(speech, isMuted = _uiState.value.isTtsMuted)
    }

    fun triggerHomeDirect() {
        if (!CharuAccessibilityService.triggerHome()) {
            deviceController.openAccessibilitySettings()
        }
    }

    fun triggerBackDirect() {
        if (!CharuAccessibilityService.triggerBack()) {
            deviceController.openAccessibilitySettings()
        }
    }

    fun openAccessibilitySettings() {
        deviceController.openAccessibilitySettings()
    }

    fun loadInstalledApps() {
        viewModelScope.launch {
            val apps = deviceController.getInstalledApps(includeSystemApps = true)
            _uiState.update {
                it.copy(
                    installedApps = apps,
                    filteredApps = filterApps(apps, it.appsSearchQuery)
                )
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update {
            it.copy(
                appsSearchQuery = query,
                filteredApps = filterApps(it.installedApps, query)
            )
        }
    }

    private fun filterApps(list: List<AppInfo>, query: String): List<AppInfo> {
        if (query.isBlank()) return list
        val q = query.trim().lowercase()
        return list.filter {
            it.appName.lowercase().contains(q) ||
                    it.packageName.lowercase().contains(q) ||
                    it.category.lowercase().contains(q)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            dao.clearAllLogs()
        }
    }

    override fun onCleared() {
        super.onCleared()
        voiceManager?.destroy()
    }
}
