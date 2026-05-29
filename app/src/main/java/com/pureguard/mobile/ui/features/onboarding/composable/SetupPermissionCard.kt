package com.pureguard.mobile.ui.features.onboarding.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pureguard.mobile.ui.theme.PgMuted
import com.pureguard.mobile.ui.theme.PgSuccess
import com.pureguard.mobile.ui.theme.PgText

@Composable
fun PermissionCard(
    icon: ImageVector,
    iconTint: Color,
    iconBg: Color,
    title: String,
    description: String,
    isGranted: Boolean,
    onClick: (() -> Unit)?
) {
    val borderColor = if (isGranted) PgSuccess.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.1f)
    val bgColor = if (isGranted) Color(0xFF0A2A18) else Color.White.copy(alpha = 0.04f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(20.dp))
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(18.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(if (isGranted) Color(0xFF0F3B2A) else iconBg, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isGranted) Icons.Default.Check else icon,
                    contentDescription = null,
                    tint = if (isGranted) PgSuccess else iconTint,
                    modifier = Modifier.size(26.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PgText
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isGranted) PgSuccess.copy(0.15f) else PgMuted.copy(0.15f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (isGranted) "Active" else "Required",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isGranted) PgSuccess else PgMuted
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = description,
                    fontSize = 13.sp,
                    color = PgMuted,
                    lineHeight = 19.sp
                )
                if (!isGranted && onClick != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Tap to enable →",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = iconTint
                    )
                }
            }
        }
    }
}
