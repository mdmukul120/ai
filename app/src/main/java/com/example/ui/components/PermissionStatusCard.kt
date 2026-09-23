package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CharuAccentAmber
import com.example.ui.theme.CharuAccentGreen
import com.example.ui.theme.CharuBorder
import com.example.ui.theme.CharuCyan
import com.example.ui.theme.CharuDarkCard
import com.example.ui.theme.CharuDarkSurface
import com.example.ui.theme.CharuIndigo
import com.example.ui.theme.CharuTextPrimary
import com.example.ui.theme.CharuTextSecondary

@Composable
fun PermissionStatusCard(
    isAccessibilityEnabled: Boolean,
    isMicGranted: Boolean,
    onEnableAccessibilityClick: () -> Unit,
    onRequestMicClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("permission_status_card"),
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
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "ডিভাইস নিয়ন্ত্রণ পারমিশন স্ট্যাটাস",
                    color = CharuTextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )

                val allGranted = isAccessibilityEnabled && isMicGranted
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (allGranted) CharuAccentGreen.copy(alpha = 0.15f) else CharuAccentAmber.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    BoxDot(color = if (allGranted) CharuAccentGreen else CharuAccentAmber)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (allGranted) "সম্পূর্ণ সক্রিয়" else "অনুমতি বাকি",
                        color = if (allGranted) CharuAccentGreen else CharuAccentAmber,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Permission items row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Accessibility Service
                PermissionPill(
                    icon = Icons.Default.AccessibilityNew,
                    title = "অ্যাক্সেসিবিলিটি",
                    subtitle = "অ্যাপ ও স্ক্রিন নিয়ন্ত্রণ",
                    isGranted = isAccessibilityEnabled,
                    onClick = onEnableAccessibilityClick,
                    modifier = Modifier.weight(1f)
                )

                // Mic
                PermissionPill(
                    icon = Icons.Default.Mic,
                    title = "মাইক্রোফোন",
                    subtitle = "ভয়েস কমান্ড",
                    isGranted = isMicGranted,
                    onClick = onRequestMicClick,
                    modifier = Modifier.weight(1f)
                )
            }

            AnimatedVisibility(visible = !isAccessibilityEnabled) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(CharuDarkSurface)
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "সতর্কতা",
                            tint = CharuAccentAmber,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "চারু যাতে স্বয়ংক্রিয়ভাবে যেকোনো অ্যাপ ও ফেসবুক লাইট মেসেজ পরিচালনা করতে পারে, সেজন্য অ্যাক্সেসিবিলিটি পারমিশন অন করুন।",
                            color = CharuTextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = onEnableAccessibilityClick,
                        colors = ButtonDefaults.buttonColors(containerColor = CharuCyan),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("enable_accessibility_button")
                    ) {
                        Text(
                            text = "সেটিংসে গিয়ে চারু পারমিশন অন করুন",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PermissionPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    isGranted: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CharuDarkSurface),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isGranted) CharuAccentGreen.copy(alpha = 0.4f) else CharuAccentAmber.copy(alpha = 0.4f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = if (isGranted) CharuCyan else CharuTextSecondary,
                    modifier = Modifier.size(20.dp)
                )

                Icon(
                    imageVector = if (isGranted) Icons.Default.CheckCircle else Icons.Default.Warning,
                    contentDescription = null,
                    tint = if (isGranted) CharuAccentGreen else CharuAccentAmber,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = title,
                color = CharuTextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                color = CharuTextSecondary,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun BoxDot(color: Color) {
    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(color)
    )
}
