package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.components.AppDetailDialog
import com.example.ui.components.AppsExplorerView
import com.example.ui.components.AssistantDialogueCard
import com.example.ui.components.BossHeader
import com.example.ui.components.CommandHistoryLogList
import com.example.ui.components.DeviceControlPanel
import com.example.ui.components.PermissionStatusCard
import com.example.ui.components.QuickActionsRow
import com.example.ui.components.VoiceCommandBar
import com.example.ui.theme.CharuCyan
import com.example.ui.theme.CharuDarkBg
import com.example.ui.theme.CharuDarkCard
import com.example.ui.theme.CharuDarkSurface
import com.example.ui.theme.CharuIndigo
import com.example.ui.theme.CharuTextPrimary
import com.example.ui.theme.CharuTextSecondary
import com.example.ui.viewmodel.CharuViewModel

@Composable
fun CharuMainScreen(
    viewModel: CharuViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Mic Permission Request Launcher
    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.setMicPermissionGranted(isGranted)
    }

    LaunchedEffect(Unit) {
        val hasMic = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
        viewModel.setMicPermissionGranted(hasMic)
        viewModel.refreshDeviceState()
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("charu_main_screen"),
        containerColor = CharuDarkBg,
        bottomBar = {
            Column {
                // Persistent Voice / Text Input Bar
                VoiceCommandBar(
                    isListening = uiState.isListening,
                    onMicToggle = {
                        if (!uiState.isMicPermissionGranted) {
                            micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        } else {
                            viewModel.toggleVoiceListening()
                        }
                    },
                    onSendText = { text ->
                        viewModel.handleUserInput(text)
                    }
                )

                // Bottom Tab Bar
                NavigationBar(
                    containerColor = CharuDarkSurface,
                    modifier = Modifier.testTag("charu_navigation_bar")
                ) {
                    NavigationBarItem(
                        selected = uiState.selectedTab == 0,
                        onClick = { viewModel.setTab(0) },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.GraphicEq,
                                contentDescription = "ভয়েস সহকারী",
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        label = { Text("সহকারী", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CharuCyan,
                            selectedTextColor = CharuCyan,
                            unselectedIconColor = CharuTextSecondary,
                            unselectedTextColor = CharuTextSecondary,
                            indicatorColor = CharuDarkCard
                        ),
                        modifier = Modifier.testTag("tab_assistant")
                    )

                    NavigationBarItem(
                        selected = uiState.selectedTab == 1,
                        onClick = { viewModel.setTab(1) },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Apps,
                                contentDescription = "ডিভাইস অ্যাপস",
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        label = { Text("সকল অ্যাপস", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CharuCyan,
                            selectedTextColor = CharuCyan,
                            unselectedIconColor = CharuTextSecondary,
                            unselectedTextColor = CharuTextSecondary,
                            indicatorColor = CharuDarkCard
                        ),
                        modifier = Modifier.testTag("tab_apps")
                    )

                    NavigationBarItem(
                        selected = uiState.selectedTab == 2,
                        onClick = { viewModel.setTab(2) },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = "কন্ট্রোল",
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        label = { Text("কন্ট্রোল সেন্টার", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CharuCyan,
                            selectedTextColor = CharuCyan,
                            unselectedIconColor = CharuTextSecondary,
                            unselectedTextColor = CharuTextSecondary,
                            indicatorColor = CharuDarkCard
                        ),
                        modifier = Modifier.testTag("tab_controls")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // Top Boss Profile Header
                BossHeader(
                    bossName = "মুকুল আহমেদ",
                    assistantName = "চারু",
                    isSpeaking = uiState.isSpeaking,
                    isListening = uiState.isListening,
                    isTtsMuted = uiState.isTtsMuted,
                    onToggleTtsMute = { viewModel.toggleTtsMute() }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Content depending on selected tab
                when (uiState.selectedTab) {
                    0 -> {
                        // Voice Assistant Console
                        PermissionStatusCard(
                            isAccessibilityEnabled = uiState.isAccessibilityEnabled,
                            isMicGranted = uiState.isMicPermissionGranted,
                            onEnableAccessibilityClick = { viewModel.openAccessibilitySettings() },
                            onRequestMicClick = {
                                micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        QuickActionsRow(
                            onVolumeUp = { viewModel.volumeUpDirect() },
                            onVolumeDown = { viewModel.volumeDownDirect() },
                            onFacebookLiteMessages = { viewModel.launchFacebookLiteMessagesDirect() },
                            onGoHome = { viewModel.triggerHomeDirect() }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        AssistantDialogueCard(
                            lastUserQuery = uiState.lastUserQuery,
                            lastSpokenMessage = uiState.lastSpokenMessage,
                            statusBanner = uiState.statusBanner,
                            isThinking = uiState.isThinking,
                            isSpeaking = uiState.isSpeaking,
                            isListening = uiState.isListening,
                            audioDb = uiState.rmsDb
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        CommandHistoryLogList(
                            logs = uiState.commandLogs,
                            onClearHistory = { viewModel.clearHistory() },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    1 -> {
                        // Device Installed Apps Explorer
                        AppsExplorerView(
                            apps = uiState.filteredApps,
                            searchQuery = uiState.appsSearchQuery,
                            onSearchQueryChanged = { viewModel.onSearchQueryChanged(it) },
                            onAppClicked = { app -> viewModel.selectAppForDetail(app) },
                            onLaunchApp = { app -> viewModel.launchAppDirect(app) }
                        )
                    }

                    2 -> {
                        // Device Control & Automation Center
                        DeviceControlPanel(
                            volumePercent = uiState.soundVolumePercent,
                            onVolumeChanged = { viewModel.setVolumeDirect(it) },
                            onVolumeUp = { viewModel.volumeUpDirect() },
                            onVolumeDown = { viewModel.volumeDownDirect() },
                            onFacebookLiteMessage = { msg ->
                                viewModel.handleUserInput(
                                    if (msg.isNotBlank()) "ফেসবুক লাইট ওপেন করে মেসেজে '$msg' পাঠাও"
                                    else "ফেসবুক লাইট ওপেন করে মেসেজ অপশনে যাও"
                                )
                            },
                            onHomeClick = { viewModel.triggerHomeDirect() },
                            onBackClick = { viewModel.triggerBackDirect() }
                        )
                    }
                }
            }

            // App Explanation Dialog
            AppDetailDialog(
                app = uiState.selectedAppForDetail,
                onDismiss = { viewModel.selectAppForDetail(null) },
                onLaunch = { app -> viewModel.launchAppDirect(app) },
                onSpeakExplanation = { app -> viewModel.explainApp(app) }
            )
        }
    }
}
