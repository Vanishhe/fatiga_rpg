package ru.college.app.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.college.app.R
import ru.college.app.presentation.SettingsUiState
import ru.college.app.ui.components.PixelButton
import ru.college.app.ui.components.PixelSegmentedSwitch
import ru.college.app.ui.components.PixelTextField
import ru.college.app.ui.theme.AccentMutedGreen
import ru.college.app.ui.theme.AccentMutedRed
import ru.college.app.ui.theme.AccentOffWhite
import ru.college.app.ui.theme.BgBlack
import ru.college.app.ui.theme.PanelDarkGray
import ru.college.app.ui.theme.TextLightGray
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    onNameChange: (String) -> Unit,
    onSave: () -> Unit,
    onToggleSound: (Boolean) -> Unit,
    onToggleVibration: (Boolean) -> Unit,
    onLanguageSelected: (Int) -> Unit,
    onLogout: () -> Unit,
    onDeleteRequested: () -> Unit,
    onDeleteDismissed: () -> Unit,
    onDeleteConfirmed: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BgBlack)
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .widthIn(max = 480.dp)
                .fillMaxWidth()
                .safeDrawingPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.settings_online_label),
                    style = MaterialTheme.typography.labelSmall,
                    color = TextLightGray
                )
                Text(
                    text = stringResource(R.string.settings_close),
                    style = MaterialTheme.typography.labelSmall,
                    color = AccentOffWhite,
                    modifier = Modifier
                        .border(1.dp, PanelDarkGray, RoundedCornerShape(0.dp))
                        .clickable(onClick = onClose)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = stringResource(R.string.settings_title),
                style = MaterialTheme.typography.displayMedium,
                color = AccentOffWhite
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.settings_subtitle),
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp),
                color = TextLightGray
            )

            Spacer(modifier = Modifier.height(32.dp))
            SectionLabel(R.string.settings_section_profile)
            Spacer(modifier = Modifier.height(12.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, PanelDarkGray, RoundedCornerShape(0.dp))
                    .padding(16.dp)
            ) {
                PixelTextField(
                    label = stringResource(R.string.label_player_name),
                    value = uiState.name,
                    onValueChange = onNameChange,
                    placeholder = stringResource(R.string.hint_player_name)
                )
                Spacer(modifier = Modifier.height(16.dp))
                InfoRow(
                    labelRes = R.string.label_email,
                    value = uiState.email
                )
                Spacer(modifier = Modifier.height(12.dp))
                InfoRow(
                    labelRes = R.string.settings_soul_level,
                    value = stringResource(R.string.settings_soul_value, uiState.soulLevel)
                )
                Spacer(modifier = Modifier.height(12.dp))
                InfoRow(
                    labelRes = R.string.settings_bound,
                    value = rememberBoundDate(uiState.createdAt)
                )

                Spacer(modifier = Modifier.height(20.dp))
                PixelButton(
                    text = stringResource(R.string.settings_save),
                    onClick = onSave,
                    enabled = uiState.hasChanges && !uiState.isSaving
                )
            }

            if (uiState.messageRes != null || uiState.errorRes != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(uiState.errorRes ?: uiState.messageRes!!),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (uiState.errorRes != null) {
                        MaterialTheme.colorScheme.error
                    } else {
                        AccentMutedGreen
                    }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
            SectionLabel(R.string.settings_section_preferences)
            Spacer(modifier = Modifier.height(12.dp))

            SettingsToggleRow(
                labelRes = R.string.settings_sound,
                enabled = uiState.soundEnabled,
                onToggle = onToggleSound
            )
            Spacer(modifier = Modifier.height(8.dp))
            SettingsToggleRow(
                labelRes = R.string.settings_vibration,
                enabled = uiState.vibrationEnabled,
                onToggle = onToggleVibration
            )

            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = stringResource(R.string.settings_language),
                style = MaterialTheme.typography.labelLarge,
                color = AccentOffWhite
            )
            Spacer(modifier = Modifier.height(8.dp))
            PixelSegmentedSwitch(
                options = listOf(
                    stringResource(R.string.language_en),
                    stringResource(R.string.language_ru)
                ),
                selectedIndex = uiState.languageIndex,
                onSelect = onLanguageSelected
            )

            Spacer(modifier = Modifier.height(32.dp))
            SectionLabel(R.string.settings_section_account)
            Spacer(modifier = Modifier.height(12.dp))

            PixelButton(
                text = stringResource(R.string.settings_delete),
                onClick = onDeleteRequested,
                containerColor = AccentMutedRed,
                contentColor = AccentOffWhite
            )
            Spacer(modifier = Modifier.height(12.dp))
            PixelButton(
                text = stringResource(R.string.settings_logout),
                onClick = onLogout,
                containerColor = AccentMutedRed,
                contentColor = AccentOffWhite
            )

            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = stringResource(R.string.settings_footer),
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp),
                color = TextLightGray,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (uiState.showDeleteConfirm) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BgBlack.copy(alpha = 0.94f))
                    .safeDrawingPadding()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, AccentMutedRed, RoundedCornerShape(0.dp))
                        .padding(24.dp)
                ) {
                    Text(
                        text = stringResource(R.string.settings_delete_title),
                        style = MaterialTheme.typography.headlineMedium,
                        color = AccentOffWhite
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = stringResource(R.string.settings_delete_body),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextLightGray
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    PixelButton(
                        text = stringResource(R.string.settings_cancel),
                        onClick = onDeleteDismissed,
                        containerColor = PanelDarkGray,
                        contentColor = AccentOffWhite
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    PixelButton(
                        text = stringResource(R.string.settings_delete_confirm),
                        onClick = onDeleteConfirmed,
                        containerColor = AccentMutedRed,
                        contentColor = AccentOffWhite
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionLabel(labelRes: Int) {
    Text(
        text = stringResource(labelRes),
        style = MaterialTheme.typography.labelLarge,
        color = AccentOffWhite
    )
}

@Composable
private fun InfoRow(labelRes: Int, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(labelRes),
            style = MaterialTheme.typography.labelSmall,
            color = TextLightGray
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = AccentOffWhite,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun SettingsToggleRow(
    labelRes: Int,
    enabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, PanelDarkGray, RoundedCornerShape(0.dp))
            .clickable { onToggle(!enabled) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(labelRes),
            style = MaterialTheme.typography.labelLarge,
            color = AccentOffWhite
        )
        Box(
            modifier = Modifier
                .width(64.dp)
                .height(32.dp)
                .background(if (enabled) AccentMutedGreen else PanelDarkGray),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(
                    if (enabled) R.string.settings_on else R.string.settings_off
                ),
                style = MaterialTheme.typography.labelSmall,
                color = AccentOffWhite
            )
        }
    }
}

@Composable
private fun rememberBoundDate(createdAt: Long): String {
    val noDate = stringResource(R.string.settings_no_date)
    return remember(createdAt, noDate) {
        if (createdAt <= 0L) {
            noDate
        } else {
            SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(createdAt))
        }
    }
}
