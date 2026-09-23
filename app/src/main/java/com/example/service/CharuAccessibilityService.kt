package com.example.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.lang.ref.WeakReference

class CharuAccessibilityService : AccessibilityService() {

    override fun onServiceConnected() {
        super.onServiceConnected()
        instanceRef = WeakReference(this)
        _isServiceActive.value = true
        Log.d(TAG, "Charu Accessibility Service Connected and Active!")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        val pkgName = event.packageName?.toString() ?: ""
        val pending = pendingTask
        if (pending != null && (pkgName.contains("facebook", ignoreCase = true) || pkgName == pending.targetPackage)) {
            handleAutomatedAppInteraction(event, pending)
        }
    }

    private fun handleAutomatedAppInteraction(event: AccessibilityEvent, task: PendingAutomationTask) {
        val rootNode = rootInActiveWindow ?: return
        try {
            when (task.action) {
                AutomationAction.CLICK_MESSAGES -> {
                    // Try to find message / chat button or tab in Facebook Lite or any social app
                    val messageKeywords = listOf(
                        "message", "messages", "chat", "chats", "inbox",
                        "বার্তা", "মেসেজ", "ইনবক্স", "নতুন বার্তা", "মেসেঞ্জার"
                    )

                    var clicked = false
                    for (keyword in messageKeywords) {
                        val nodes = rootNode.findAccessibilityNodeInfosByText(keyword)
                        for (node in nodes) {
                            if (clickNodeOrParent(node)) {
                                clicked = true
                                Log.d(TAG, "Successfully clicked message node matching '$keyword'")
                                break
                            }
                        }
                        if (clicked) break
                    }

                    // Fallback to view ID search if available
                    if (!clicked) {
                        val viewIds = listOf(
                            "com.facebook.lite:id/messages_tab",
                            "com.facebook.lite:id/jewel_button_messages",
                            "com.facebook.lite:id/message_icon",
                            "com.facebook.katana:id/messages_tab"
                        )
                        for (viewId in viewIds) {
                            val nodes = rootNode.findAccessibilityNodeInfosByViewId(viewId)
                            for (node in nodes) {
                                if (clickNodeOrParent(node)) {
                                    clicked = true
                                    break
                                }
                            }
                            if (clicked) break
                        }
                    }

                    if (clicked) {
                        pendingTask = null
                    }
                }
                AutomationAction.INPUT_TEXT -> {
                    val targetText = task.textToInput ?: return
                    val focusNode = findEditableNode(rootNode)
                    if (focusNode != null) {
                        val arguments = Bundle()
                        arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, targetText)
                        focusNode.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
                        pendingTask = null
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error executing automation: ${e.message}")
        }
    }

    private fun clickNodeOrParent(node: AccessibilityNodeInfo?): Boolean {
        var current = node
        while (current != null) {
            if (current.isClickable) {
                return current.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            }
            current = current.parent
        }
        return false
    }

    private fun findEditableNode(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        if (node.isEditable) return node
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val result = findEditableNode(child)
            if (result != null) return result
        }
        return null
    }

    override fun onInterrupt() {
        Log.w(TAG, "Charu Accessibility Service Interrupted")
    }

    override fun onDestroy() {
        super.onDestroy()
        _isServiceActive.value = false
        if (instanceRef?.get() == this) {
            instanceRef = null
        }
    }

    companion object {
        private const val TAG = "CharuAccessibility"
        private var instanceRef: WeakReference<CharuAccessibilityService>? = null

        private val _isServiceActive = MutableStateFlow(false)
        val isServiceActive: StateFlow<Boolean> = _isServiceActive.asStateFlow()

        private var pendingTask: PendingAutomationTask? = null

        fun getService(): CharuAccessibilityService? = instanceRef?.get()

        fun isAccessibilitySettingsOn(context: Context): Boolean {
            val expectedServiceName = "${context.packageName}/${CharuAccessibilityService::class.java.canonicalName}"
            val enabledServices = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            ) ?: return false

            val colonSplitter = TextUtils.SimpleStringSplitter(':')
            colonSplitter.setString(enabledServices)
            while (colonSplitter.hasNext()) {
                val componentName = colonSplitter.next()
                if (componentName.equals(expectedServiceName, ignoreCase = true) ||
                    componentName.contains("CharuAccessibilityService", ignoreCase = true)
                ) {
                    return true
                }
            }
            return false
        }

        fun triggerHome(): Boolean {
            return getService()?.performGlobalAction(GLOBAL_ACTION_HOME) ?: false
        }

        fun triggerBack(): Boolean {
            return getService()?.performGlobalAction(GLOBAL_ACTION_BACK) ?: false
        }

        fun triggerRecents(): Boolean {
            return getService()?.performGlobalAction(GLOBAL_ACTION_RECENTS) ?: false
        }

        fun triggerNotifications(): Boolean {
            return getService()?.performGlobalAction(GLOBAL_ACTION_NOTIFICATIONS) ?: false
        }

        fun queueFacebookLiteMessageAutomation(messageText: String? = null) {
            pendingTask = PendingAutomationTask(
                targetPackage = "com.facebook.lite",
                action = AutomationAction.CLICK_MESSAGES,
                textToInput = messageText
            )
        }
    }
}

data class PendingAutomationTask(
    val targetPackage: String,
    val action: AutomationAction,
    val textToInput: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

enum class AutomationAction {
    CLICK_MESSAGES,
    INPUT_TEXT
}
