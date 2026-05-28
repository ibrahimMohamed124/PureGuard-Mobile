package com.pureguard.mobile.ui.features.settings.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pureguard.mobile.core.common.extensions.autoBringIntoView
import com.pureguard.mobile.ui.theme.PgAccentBlue
import com.pureguard.mobile.ui.theme.PgDanger
import com.pureguard.mobile.ui.theme.PgMuted
import com.pureguard.mobile.ui.theme.PgSuccess
import com.pureguard.mobile.ui.theme.PgText

@Composable
fun TamperProtectionCard(
    hasPassword: Boolean,
    locked: Boolean,
    oldPassword: String,
    newPassword: String,
    newPassword2: String,
    removePassword: String,
    onOldPasswordChange: (String) -> Unit,
    onNewPasswordChange: (String) -> Unit,
    onNewPassword2Change: (String) -> Unit,
    onRemovePasswordChange: (String) -> Unit,
    onSetPassword: () -> Unit,
    onRemovePassword: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(0.04f))
            .border(1.dp, Color.White.copy(0.08f), RoundedCornerShape(20.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(PgAccentBlue.copy(0.12f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Lock, null, tint = PgAccentBlue, modifier = Modifier.size(18.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("Tamper protection", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = PgText)
                Text("Password-protect your settings", fontSize = 12.sp, color = PgMuted)
            }
            if (hasPassword) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(PgSuccess.copy(0.12f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("Active", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PgSuccess)
                }
            }
        }

        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(0.06f)))

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val fieldColors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PgAccentBlue,
                unfocusedBorderColor = Color.White.copy(0.12f),
                focusedTextColor = PgText,
                unfocusedTextColor = PgText,
                disabledTextColor = PgMuted,
                disabledBorderColor = Color.White.copy(0.06f)
            )

            if (hasPassword) {
                OutlinedTextField(
                    value = oldPassword,
                    onValueChange = onOldPasswordChange,
                    label = { Text("Current password", color = PgMuted, fontSize = 13.sp) },
                    modifier = Modifier.fillMaxWidth().autoBringIntoView(),
                    shape = RoundedCornerShape(12.dp),
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    colors = fieldColors
                )
            }

            OutlinedTextField(
                value = newPassword,
                onValueChange = onNewPasswordChange,
                label = { Text("New password", color = PgMuted, fontSize = 13.sp) },
                modifier = Modifier.fillMaxWidth().autoBringIntoView(),
                shape = RoundedCornerShape(12.dp),
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                colors = fieldColors
            )

            OutlinedTextField(
                value = newPassword2,
                onValueChange = onNewPassword2Change,
                label = { Text("Confirm new password", color = PgMuted, fontSize = 13.sp) },
                modifier = Modifier.fillMaxWidth().autoBringIntoView(),
                shape = RoundedCornerShape(12.dp),
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                colors = fieldColors
            )

            val passwordsMatch = newPassword.isNotBlank() && newPassword == newPassword2
            Button(
                onClick = onSetPassword,
                enabled = passwordsMatch,
                modifier = Modifier.fillMaxWidth().height(46.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PgAccentBlue,
                    disabledContainerColor = Color.White.copy(0.07f)
                )
            ) {
                Text(
                    text = if (hasPassword) "Change password" else "Set password",
                    fontWeight = FontWeight.SemiBold,
                    color = if (passwordsMatch) Color(0xFF0A0F1E) else PgMuted
                )
            }

            if (hasPassword) {
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(0.06f)))
                OutlinedTextField(
                    value = removePassword,
                    onValueChange = onRemovePasswordChange,
                    label = { Text("Password to remove lock", color = PgMuted, fontSize = 13.sp) },
                    modifier = Modifier.fillMaxWidth().autoBringIntoView(),
                    shape = RoundedCornerShape(12.dp),
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    colors = fieldColors
                )
                TextButton(
                    onClick = onRemovePassword,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Remove password lock", color = PgDanger, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
