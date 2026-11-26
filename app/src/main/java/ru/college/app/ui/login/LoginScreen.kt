package ru.college.app.ui.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.college.app.R
import ru.college.app.presentation.AuthMode
import ru.college.app.presentation.LoginUiState
import ru.college.app.ui.components.PixelButton
import ru.college.app.ui.components.PixelOrnamentDivider
import ru.college.app.ui.components.PixelSegmentedSwitch
import ru.college.app.ui.components.PixelTextField
import ru.college.app.ui.theme.AccentOffWhite
import ru.college.app.ui.theme.BgBlack
import ru.college.app.ui.theme.FatigaTheme
import ru.college.app.ui.theme.TextLightGray

/**
 * Screen 1 — Login / Registration, laid out after the Figma reference:
 * top status line, display title, ornamental divider, segmented switch,
 * three fields and a filled ENTER button.
 */
@Composable
fun LoginScreen(
    uiState: LoginUiState,
    onModeSelected: (AuthMode) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onPlayerNameChange: (String) -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isRegister = uiState.mode == AuthMode.REGISTER

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
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            LoginTopBar()

            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = stringResource(R.string.login_title),
                style = MaterialTheme.typography.displayLarge,
                color = AccentOffWhite,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.login_subtitle),
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp),
                color = TextLightGray,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            PixelOrnamentDivider()

            Spacer(modifier = Modifier.height(16.dp))

            PixelSegmentedSwitch(
                options = listOf(
                    stringResource(R.string.auth_tab_login),
                    stringResource(R.string.auth_tab_register)
                ),
                selectedIndex = if (isRegister) 1 else 0,
                onSelect = { index ->
                    onModeSelected(if (index == 0) AuthMode.LOGIN else AuthMode.REGISTER)
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            PixelTextField(
                label = stringResource(R.string.label_email),
                value = uiState.email,
                onValueChange = onEmailChange,
                placeholder = stringResource(R.string.hint_email),
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next,
                errorMessage = uiState.emailErrorRes?.let { stringResource(it) }
            )

            Spacer(modifier = Modifier.height(20.dp))

            PixelTextField(
                label = stringResource(R.string.label_password),
                value = uiState.password,
                onValueChange = onPasswordChange,
                placeholder = stringResource(R.string.hint_password),
                isPassword = true,
                keyboardType = KeyboardType.Password,
                imeAction = if (isRegister) ImeAction.Next else ImeAction.Done,
                keyboardActions = KeyboardActions(
                    onDone = { onSubmit() }
                ),
                errorMessage = uiState.passwordErrorRes?.let { stringResource(it) }
            )

            if (isRegister) {
                Spacer(modifier = Modifier.height(20.dp))

                PixelTextField(
                    label = stringResource(R.string.label_player_name),
                    value = uiState.playerName,
                    onValueChange = onPlayerNameChange,
                    placeholder = stringResource(R.string.hint_player_name),
                    labelEndHint = stringResource(R.string.hint_new_souls),
                    imeAction = ImeAction.Done,
                    keyboardActions = KeyboardActions(
                        onDone = { onSubmit() }
                    ),
                    errorMessage = uiState.playerNameErrorRes?.let { stringResource(it) }
                )
            }

            uiState.formErrorRes?.let { errorRes ->
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(errorRes),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            PixelButton(
                text = if (uiState.isLoading) {
                    stringResource(R.string.auth_loading)
                } else {
                    stringResource(R.string.action_enter)
                },
                onClick = onSubmit,
                enabled = !uiState.isLoading,
                trailingSymbol = if (uiState.isLoading) null else stringResource(R.string.symbol_chevron_right)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.login_footer),
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp),
                color = TextLightGray,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun LoginTopBar() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(AccentOffWhite)
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text(
                text = stringResource(R.string.login_online_label),
                style = MaterialTheme.typography.labelSmall,
                color = TextLightGray
            )
        }
        Text(
            text = stringResource(R.string.login_screen_index),
            style = MaterialTheme.typography.labelSmall,
            color = TextLightGray
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D0D0D, heightDp = 900)
@Composable
private fun LoginScreenPreview() {
    FatigaTheme {
        LoginScreen(
            uiState = LoginUiState(mode = AuthMode.REGISTER),
            onModeSelected = {},
            onEmailChange = {},
            onPasswordChange = {},
            onPlayerNameChange = {},
            onSubmit = {}
        )
    }
}
