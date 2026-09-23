package com.example.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.VolumeDown
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CharuCyan
import com.example.ui.theme.CharuDarkCard
import com.example.ui.theme.CharuIndigo
import com.example.ui.theme.CharuTextPrimary

@Composable
fun QuickActionsRow(
    onVolumeUp: () -> Unit,
    onVolumeDown: () -> Unit,
    onFacebookLiteMessages: () -> Unit,
    onGoHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        QuickActionButton(
            label = "সাউন্ড বাড়াও",
            icon = Icons.AutoMirrored.Filled.VolumeUp,
            tag = "quick_action_vol_up",
            onClick = onVolumeUp
        )

        QuickActionButton(
            label = "সাউন্ড কমাও",
            icon = Icons.AutoMirrored.Filled.VolumeDown,
            tag = "quick_action_vol_down",
            onClick = onVolumeDown
        )

        QuickActionButton(
            label = "ফেসবুক লাইট মেসেজ",
            icon = Icons.AutoMirrored.Filled.Chat,
            tint = CharuCyan,
            tag = "quick_action_fb_lite",
            onClick = onFacebookLiteMessages
        )

        QuickActionButton(
            label = "হোম স্ক্রিন",
            icon = Icons.Default.Home,
            tag = "quick_action_home",
            onClick = onGoHome
        )
    }
}

@Composable
private fun QuickActionButton(
    label: String,
    icon: ImageVector,
    tag: String,
    tint: Color = CharuTextPrimary,
    onClick: () -> Unit
) {
    ElevatedButton(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.elevatedButtonColors(
            containerColor = CharuDarkCard,
            contentColor = CharuTextPrimary
        ),
        elevation = ButtonDefaults.elevatedButtonElevation(defaultElevation = 2.dp),
        modifier = Modifier.testTag(tag)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = tint,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
