package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CharuAccentGreen
import com.example.ui.theme.CharuBorder
import com.example.ui.theme.CharuCyan
import com.example.ui.theme.CharuDarkCard
import com.example.ui.theme.CharuDarkSurface
import com.example.ui.theme.CharuIndigo
import com.example.ui.theme.CharuTextPrimary
import com.example.ui.theme.CharuTextSecondary

@Composable
fun AssistantDialogueCard(
    lastUserQuery: String,
    lastSpokenMessage: String,
    statusBanner: String,
    isThinking: Boolean,
    isSpeaking: Boolean,
    isListening: Boolean,
    audioDb: Float,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("assistant_dialogue_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = CharuDarkCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, CharuBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Orb in center
            AssistantOrbVisualizer(
                isListening = isListening,
                isSpeaking = isSpeaking,
                isThinking = isThinking,
                audioDb = audioDb
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Active Assistant State Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (isThinking) {
                    CircularProgressIndicator(
                        color = CharuCyan,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "বসের নির্দেশ বিশ্লেষণ হচ্ছে...",
                        color = CharuCyan,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                } else if (isListening) {
                    Icon(
                        imageVector = Icons.Default.RecordVoiceOver,
                        contentDescription = null,
                        tint = CharuCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "বস মুকুল আহমেদ, বলুন আমি শুনছি...",
                        color = CharuCyan,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = CharuIndigo,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "চারু ভয়েস অ্যাসিস্ট্যান্ট",
                        color = CharuTextSecondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // User Query Bubble (if present)
            AnimatedVisibility(visible = lastUserQuery.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(CharuDarkSurface)
                        .border(1.dp, CharuBorder.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Text(
                            text = "বস মুকুল আহমেদ:",
                            color = CharuCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "\"$lastUserQuery\"",
                            color = CharuTextPrimary,
                            fontSize = 14.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            // Charu Speech Output Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                CharuIndigo.copy(alpha = 0.15f),
                                CharuDarkSurface
                            )
                        )
                    )
                    .border(1.dp, CharuCyan.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                    .padding(14.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "চারু:",
                            color = CharuCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )

                        if (isSpeaking) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(CharuCyan)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "কথা বলছে",
                                    color = CharuCyan,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = lastSpokenMessage,
                        color = CharuTextPrimary,
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Status Banner Pill
            if (statusBanner.isNotBlank()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(CharuDarkSurface)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = CharuAccentGreen,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = statusBanner,
                        color = CharuTextSecondary,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}
