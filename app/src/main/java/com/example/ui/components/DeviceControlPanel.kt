package com.example.ui.components

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeDown
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CharuBorder
import com.example.ui.theme.CharuCyan
import com.example.ui.theme.CharuDarkCard
import com.example.ui.theme.CharuDarkSurface
import com.example.ui.theme.CharuIndigo
import com.example.ui.theme.CharuTextPrimary
import com.example.ui.theme.CharuTextSecondary

@Composable
fun DeviceControlPanel(
    volumePercent: Int,
    onVolumeChanged: (Int) -> Unit,
    onVolumeUp: () -> Unit,
    onVolumeDown: () -> Unit,
    onFacebookLiteMessage: (String) -> Unit,
    onHomeClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    var customFbMessage by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- Volume Section ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("volume_control_card"),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = CharuDarkCard),
            border = androidx.compose.foundation.BorderStroke(1.dp, CharuBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "সাউন্ড নিয়ন্ত্রণ",
                            tint = CharuCyan,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "সাউন্ড ও ভলিউম নিয়ন্ত্রণ",
                            color = CharuTextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(CharuDarkSurface)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "$volumePercent%",
                            color = CharuCyan,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Slider with + and -
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onVolumeDown,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(CharuDarkSurface)
                            .testTag("vol_down_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeDown,
                            contentDescription = "সাউন্ড কমান",
                            tint = CharuTextPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Slider(
                        value = volumePercent.toFloat(),
                        onValueChange = { onVolumeChanged(it.toInt()) },
                        valueRange = 0f..100f,
                        colors = SliderDefaults.colors(
                            thumbColor = CharuCyan,
                            activeTrackColor = CharuCyan,
                            inactiveTrackColor = CharuBorder
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp)
                            .testTag("volume_slider")
                    )

                    IconButton(
                        onClick = onVolumeUp,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(CharuDarkSurface)
                            .testTag("vol_up_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "সাউন্ড বাড়ান",
                            tint = CharuCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Volume Preset Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    listOf(0 to "মিউট", 30 to "৩০%", 60 to "৬০%", 100 to "সর্বোচ্চ").forEach { (level, text) ->
                        SuggestionChip(
                            onClick = { onVolumeChanged(level) },
                            label = { Text(text, fontSize = 11.sp) },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = if (volumePercent == level) CharuIndigo else CharuDarkSurface,
                                labelColor = CharuTextPrimary
                            ),
                            border = SuggestionChipDefaults.suggestionChipBorder(
                                enabled = true,
                                borderColor = if (volumePercent == level) CharuCyan else CharuBorder
                            ),
                            modifier = Modifier.testTag("vol_preset_$level")
                        )
                    }
                }
            }
        }

        // --- Facebook Lite Messaging Deep Action Card ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("fb_lite_deep_action_card"),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = CharuDarkCard),
            border = androidx.compose.foundation.BorderStroke(1.dp, CharuBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Chat,
                        contentDescription = "ফেসবুক লাইট মেসেজ",
                        tint = CharuCyan,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ফেসবুক লাইট মেসেজিং অটোমেশন",
                        color = CharuTextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "বস মুকুল আহমেদ, আপনি নির্দেশ দিলে চারু স্বয়ংক্রিয়ভাবে ফেসবুক লাইট ওপেন করে চ্যাট/মেসেজ অপশনে প্রবেশ করবে।",
                    color = CharuTextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = customFbMessage,
                    onValueChange = { customFbMessage = it },
                    placeholder = { Text("ঐচ্ছিক মেসেজ (যদি কোনো বার্তা পাঠাতে চান)...", color = CharuTextSecondary, fontSize = 12.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = CharuDarkSurface,
                        unfocusedContainerColor = CharuDarkSurface,
                        focusedBorderColor = CharuCyan,
                        unfocusedBorderColor = CharuBorder,
                        focusedTextColor = CharuTextPrimary,
                        unfocusedTextColor = CharuTextPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("fb_message_input_field")
                )

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = { onFacebookLiteMessage(customFbMessage) },
                    colors = ButtonDefaults.buttonColors(containerColor = CharuCyan),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("fb_lite_launch_messages_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "মেসেজে প্রবেশ করুন",
                        tint = Color.Black,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ফেসবুক লাইট খুলে মেসেজে যান",
                        color = Color.Black,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // --- System Navigation Controls ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("navigation_controls_card"),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = CharuDarkCard),
            border = androidx.compose.foundation.BorderStroke(1.dp, CharuBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Layers,
                        contentDescription = "ডিভাইস নেভিগেশন",
                        tint = CharuCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ডিভাইসের নেভিগেশন অ্যাকশন",
                        color = CharuTextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onHomeClick,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CharuDarkSurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CharuBorder),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("nav_home_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "হোম",
                            tint = CharuCyan,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "হোম স্ক্রিন",
                            color = CharuTextPrimary,
                            fontSize = 12.sp
                        )
                    }

                    Button(
                        onClick = onBackClick,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CharuDarkSurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CharuBorder),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("nav_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "ব্যাক",
                            tint = CharuCyan,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "ব্যাক",
                            color = CharuTextPrimary,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}
