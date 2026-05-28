package com.pureguard.mobile.ui.features.settings.composable

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.pureguard.mobile.ui.theme.PgDanger
import com.pureguard.mobile.ui.theme.PgSuccess

@Composable
fun DomainManagerScreen(
    destination: SettingsDestination,
    domains: List<String>,
    locked: Boolean,
    domainInput: String,
    onDomainInputChange: (String) -> Unit,
    onDeleteDomain: (String) -> Unit,
    innerPadding: PaddingValues
) {
    val isWhitelist = destination == SettingsDestination.WHITELIST
    val accentColor = if (isWhitelist) PgSuccess else PgDanger
    val title = if (isWhitelist) "Allowed domains" else "Blocked domains"
    val helper = if (isWhitelist) "These domains are always allowed through." else "These domains are always blocked."

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = innerPadding.calculateTopPadding() + 4.dp,
            bottom = innerPadding.calculateBottomPadding() + 88.dp
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(accentColor.copy(0.06f))
                    .border(1.dp, accentColor.copy(0.2f), RoundedCornerShape(20.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = PgText)
                Text(helper, fontSize = 13.sp, color = PgMuted)
                OutlinedTextField(
                    value = domainInput,
                    onValueChange = onDomainInputChange,
                    label = { Text("Domain to add", color = PgMuted, fontSize = 13.sp) },
                    placeholder = { Text("example.com", color = PgMuted.copy(0.5f)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !locked,
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = accentColor,
                        unfocusedBorderColor = Color.White.copy(0.12f),
                        focusedTextColor = PgText,
                        unfocusedTextColor = PgText
                    )
                )
            }
        }

        if (domains.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White.copy(0.03f))
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(accentColor.copy(0.12f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                if (isWhitelist) Icons.Default.CheckCircle else Icons.Default.Block,
                                null, tint = accentColor, modifier = Modifier.size(24.dp)
                            )
                        }
                        Text("No domains added yet", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = PgText)
                        Text("Use the + button to add your first domain", fontSize = 12.sp, color = PgMuted)
                    }
                }
            }
        } else {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White.copy(0.04f))
                        .border(1.dp, Color.White.copy(0.08f), RoundedCornerShape(16.dp))
                ) {
                    domains.forEachIndexed { index, domain ->
                        DomainRow(
                            domain = domain,
                            accentColor = accentColor,
                            enabled = !locked,
                            showDivider = index < domains.lastIndex,
                            onDelete = { onDeleteDomain(domain) }
                        )
                    }
                }
            }
        }
    }
}

