package com.example.data.model

enum class ActionType {
    OPEN_APP,
    ADJUST_VOLUME,
    EXPLAIN_APP,
    DEVICE_ACTION,
    FACEBOOK_LITE_MESSAGE,
    NONE
}

data class AssistantAction(
    val actionType: ActionType = ActionType.NONE,
    val spokenResponse: String,
    val targetPackage: String? = null,
    val targetAppName: String? = null,
    val subAction: String? = null,
    val volumeDirection: String? = null, // "up", "down", "mute", "max"
    val deviceCommand: String? = null, // "home", "back", "recents", "notifications"
    val extraMessage: String? = null,
    val isSuccess: Boolean = true
)
