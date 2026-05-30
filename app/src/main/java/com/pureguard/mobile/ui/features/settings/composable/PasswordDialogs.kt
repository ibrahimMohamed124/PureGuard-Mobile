package com.pureguard.mobile.ui.features.settings.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.res.stringResource
import com.pureguard.mobile.R
import com.pureguard.mobile.ui.GradientBackground
import com.pureguard.mobile.ui.theme.PgAccentBlue
import com.pureguard.mobile.ui.theme.PgDanger
import com.pureguard.mobile.ui.theme.PgMuted
import com.pureguard.mobile.ui.theme.PgText
import com.pureguard.mobile.ui.theme.TbColor

@Composable
fun PasswordGateDialog(
    title: String,
    message: String,
    confirmText: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var password by rememberSaveable { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        PasswordDialogSurface {
            DialogHeader(title = title, message = message)
            PasswordField(
                value = password,
                onValueChange = { password = it },
                label = stringResource(R.string.common_password)
            )
            Button(
                onClick = { onConfirm(password) },
                enabled = password.isNotBlank(),
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PgAccentBlue,
                    disabledContainerColor = Color.White.copy(0.07f)
                )
            ) {
                Text(confirmText, color = Color(0xFF0A0F1E), fontWeight = FontWeight.SemiBold)
            }
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.common_cancel), color = PgMuted, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun ChangeLockPasswordDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var oldPassword by rememberSaveable { mutableStateOf("") }
    var newPassword by rememberSaveable { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        PasswordDialogSurface {
            DialogHeader(
                title = stringResource(R.string.password_dialog_change_title),
                message = stringResource(R.string.password_dialog_change_message)
            )
            PasswordField(
                value = oldPassword,
                onValueChange = { oldPassword = it },
                label = stringResource(R.string.password_old)
            )
            PasswordField(
                value = newPassword,
                onValueChange = { newPassword = it },
                label = stringResource(R.string.password_new)
            )
            Button(
                onClick = { onConfirm(oldPassword, newPassword) },
                enabled = oldPassword.isNotBlank() && newPassword.length >= 4,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PgAccentBlue,
                    disabledContainerColor = Color.White.copy(0.07f)
                )
            ) {
                Text(stringResource(R.string.password_change_button), color = Color(0xFF0A0F1E), fontWeight = FontWeight.SemiBold)
            }
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.common_cancel), color = PgMuted, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun MandatoryLockPasswordScreen(
    onCreatePassword: (String, (Boolean) -> Unit) -> Unit
) {
    var password by rememberSaveable { mutableStateOf("") }
    var confirm by rememberSaveable { mutableStateOf("") }
    var localError by rememberSaveable { mutableStateOf("") }
    val minCharsError = stringResource(R.string.password_error_min_chars)
    val mismatchError = stringResource(R.string.password_error_no_match)
    val createFailedError = stringResource(R.string.password_error_create_failed)

    GradientBackground {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White.copy(0.04f))
                    .border(1.dp, Color.White.copy(0.08f), RoundedCornerShape(24.dp))
                    .padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(PgAccentBlue.copy(0.12f), RoundedCornerShape(18.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Lock, null, tint = PgAccentBlue, modifier = Modifier.size(28.dp))
                }
                Text(
                    text = stringResource(R.string.password_create_title),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = PgText,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = stringResource(R.string.password_create_message),
                    fontSize = 13.sp,
                    color = PgMuted,
                    textAlign = TextAlign.Center,
                    lineHeight = 19.sp
                )
                PasswordField(password, { password = it }, stringResource(R.string.password_new))
                PasswordField(confirm, { confirm = it }, stringResource(R.string.password_confirm))
                if (localError.isNotBlank()) {
                    Text(localError, color = PgDanger, fontSize = 12.sp)
                }
                Button(
                    onClick = {
                        localError = when {
                            password.length < 4 -> minCharsError
                            password != confirm -> mismatchError
                            else -> ""
                        }
                        if (localError.isBlank()) {
                            onCreatePassword(password) { ok ->
                                if (!ok) localError = createFailedError
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PgAccentBlue)
                ) {
                    Text(stringResource(R.string.common_continue), color = Color(0xFF0A0F1E), fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun PasswordDialogSurface(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(TbColor.copy(alpha = 0.98f))
            .border(1.dp, Color.White.copy(0.08f), RoundedCornerShape(24.dp))
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        content = content
    )
}

@Composable
private fun DialogHeader(title: String, message: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .background(PgAccentBlue.copy(0.12f), RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Lock, null, tint = PgAccentBlue, modifier = Modifier.size(22.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = PgText)
            Text(message, fontSize = 12.sp, color = PgMuted, lineHeight = 17.sp)
        }
    }
    Spacer(Modifier.height(2.dp))
}

@Composable
private fun PasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = PgMuted, fontSize = 13.sp) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        visualTransformation = PasswordVisualTransformation(),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = PgAccentBlue,
            unfocusedBorderColor = Color.White.copy(0.12f),
            focusedTextColor = PgText,
            unfocusedTextColor = PgText,
            disabledTextColor = PgMuted,
            disabledBorderColor = Color.White.copy(0.06f)
        )
    )
}
