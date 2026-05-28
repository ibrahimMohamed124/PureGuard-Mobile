package com.pureguard.mobile.ui.features.settings.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pureguard.mobile.ui.theme.PgAccentBlue

@Composable
public fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        // عنوان القسم بره الـ Container عشان يفصل المجموعات بشكل نظيف جداً كـ Headers
        Text(
            text = title.uppercase(), // أو سيبها عادي حسب رغبتك
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = PgAccentBlue, // أو أي لون براند أساسي عندك زي PgAccentViolet
            modifier = Modifier
                .padding(start = 4.dp, bottom = 8.dp, top = 8.dp)
                .alpha(0.8f)
        )

        // الـ Container اللي جواه الـ Rows
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White.copy(0.04f))
                .border(1.dp, Color.White.copy(0.08f), RoundedCornerShape(20.dp))
                .padding(horizontal = 12.dp, vertical = 4.dp)
        ) {
            content()
        }
    }
}
