package com.pureguard.mobile.ui.features.settings.composable

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pureguard.mobile.ui.theme.PgAccentViolet
import com.pureguard.mobile.ui.theme.PgMuted
import com.pureguard.mobile.ui.theme.PgText

@Composable
fun LockCard(
    password: String,
    onPasswordChange: (String) -> Unit,
    onUnlock: (Boolean) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF1A1030))
            .border(1.dp, PgAccentViolet.copy(0.3f), RoundedCornerShape(20.dp))
            .padding(20.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(PgAccentViolet.copy(0.15f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Lock, null, tint = PgAccentViolet, modifier = Modifier.size(22.dp))
                }
                Column {
                    Text("Settings locked", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = PgText)
                    Text("Enter your password to edit settings", fontSize = 13.sp, color = PgMuted)
                }
            }

            OutlinedTextField(
                value = password,
                onValueChange = onPasswordChange,
                label = { Text("Password", color = PgMuted, fontSize = 13.sp) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PgAccentViolet,
                    unfocusedBorderColor = Color.White.copy(0.12f),
                    focusedTextColor = PgText,
                    unfocusedTextColor = PgText
                )
            )

            Button(
                onClick = { onUnlock(true) },
                modifier = Modifier.fillMaxWidth().height(46.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PgAccentViolet)
            ) {
                Icon(Icons.Default.Lock, null, modifier = Modifier.size(16.dp), tint = Color(0xFF0A0F1E))
                Spacer(Modifier.width(8.dp))
                Text("Unlock", fontWeight = FontWeight.SemiBold, color = Color(0xFF0A0F1E))
            }
        }
    }
}
