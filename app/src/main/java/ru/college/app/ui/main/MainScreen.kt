package ru.college.app.ui.main

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.college.app.R
import ru.college.app.presentation.MainClassOption
import ru.college.app.presentation.MainUiState
import ru.college.app.presentation.RunHistoryUi
import ru.college.app.presentation.RunResultUi
import ru.college.app.ui.components.PixelButton
import ru.college.app.ui.components.PixelSprite
import ru.college.app.ui.theme.AccentMutedBlue
import ru.college.app.ui.theme.AccentMutedGreen
import ru.college.app.ui.theme.AccentMutedRed
import ru.college.app.ui.theme.AccentOffWhite
import ru.college.app.ui.theme.BgBlack
import ru.college.app.ui.theme.FatigaTheme
import ru.college.app.ui.theme.PanelDarkGray
import ru.college.app.ui.theme.TextLightGray

/**
 * Screen 2 — profile, class selection and reactive run history (Room Flow).
 * Laid out after the Figma reference: header, profile card, two class cards,
 * PLAY, then the scrollable PAST RUNS list.
 */
@Composable
fun MainScreen(
    uiState: MainUiState,
    onClassSelected: (MainClassOption) -> Unit,
    onPlay: () -> Unit,
    onSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BgBlack)
    ) {
        LazyColumn(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .widthIn(max = 480.dp)
                .fillMaxHeight()
                .fillMaxWidth()
                .safeDrawingPadding(),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp)
        ) {
            item { MainTopBar(onSettings = onSettings) }

            item {
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    text = stringResource(R.string.main_title),
                    style = MaterialTheme.typography.displayMedium,
                    color = AccentOffWhite,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.main_subtitle),
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp),
                    color = TextLightGray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(32.dp))
                ProfileCard(uiState = uiState)
                Spacer(modifier = Modifier.height(32.dp))
            }

            item {
                ClassSelection(
                    selected = uiState.selectedClass,
                    onClassSelected = onClassSelected
                )
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
                PixelButton(
                    text = stringResource(R.string.main_play),
                    onClick = onPlay,
                    enabled = !uiState.isLoading,
                    trailingSymbol = stringResource(R.string.symbol_chevron_right)
                )
                uiState.errorRes?.let { errorRes ->
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = stringResource(errorRes),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                Spacer(modifier = Modifier.height(32.dp))
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.main_past_runs),
                        style = MaterialTheme.typography.labelLarge,
                        color = AccentOffWhite
                    )
                    Text(
                        text = stringResource(R.string.main_runs_total, uiState.totalRuns),
                        style = MaterialTheme.typography.labelSmall,
                        color = TextLightGray
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (uiState.runs.isEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.main_no_runs),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextLightGray,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }
            } else {
                itemsIndexed(
                    items = uiState.runs,
                    key = { _, run -> run.id }
                ) { index, run ->
                    RunRow(
                        displayIndex = uiState.totalRuns - index,
                        run = run
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = stringResource(R.string.main_footer),
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp),
                    color = TextLightGray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun MainTopBar(onSettings: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.main_profile_label),
            style = MaterialTheme.typography.labelSmall,
            color = TextLightGray
        )
        Text(
            text = stringResource(R.string.main_settings),
            style = MaterialTheme.typography.labelSmall,
            color = AccentOffWhite,
            modifier = Modifier
                .border(1.dp, PanelDarkGray, RoundedCornerShape(0.dp))
                .clickable(onClick = onSettings)
                .padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun ProfileCard(uiState: MainUiState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, PanelDarkGray, RoundedCornerShape(0.dp))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(AccentMutedGreen)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = uiState.playerName.ifBlank { stringResource(R.string.main_unknown_soul) },
                style = MaterialTheme.typography.titleLarge,
                color = AccentOffWhite,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(
                R.string.main_soul_line,
                uiState.soulLevel,
                uiState.totalRuns
            ),
            style = MaterialTheme.typography.labelSmall,
            color = TextLightGray
        )
    }
}

@Composable
private fun ClassSelection(
    selected: MainClassOption,
    onClassSelected: (MainClassOption) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.main_choose_class),
            style = MaterialTheme.typography.labelLarge,
            color = AccentOffWhite
        )
        Text(
            text = stringResource(R.string.main_class_counter),
            style = MaterialTheme.typography.labelSmall,
            color = TextLightGray
        )
    }

    Spacer(modifier = Modifier.height(12.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ClassCard(
            drawableRes = R.drawable.hero_warrior,
            nameRes = R.string.main_warrior,
            statsRes = R.string.main_warrior_stats,
            selected = selected == MainClassOption.WARRIOR,
            onClick = { onClassSelected(MainClassOption.WARRIOR) },
            modifier = Modifier.weight(1f)
        )
        ClassCard(
            drawableRes = R.drawable.hero_archer,
            nameRes = R.string.main_archer,
            statsRes = R.string.main_archer_stats,
            selected = selected == MainClassOption.ARCHER,
            onClick = { onClassSelected(MainClassOption.ARCHER) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ClassCard(
    @DrawableRes drawableRes: Int,
    @StringRes nameRes: Int,
    @StringRes statsRes: Int,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .height(232.dp)
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) AccentOffWhite else PanelDarkGray,
                shape = RoundedCornerShape(0.dp)
            )
            .clickable(onClick = onClick)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(
                if (selected) R.string.main_marker_selected else R.string.main_marker_unselected
            ),
            style = MaterialTheme.typography.labelSmall,
            color = TextLightGray,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        PixelSprite(
            drawableRes = drawableRes,
            contentDescription = stringResource(nameRes),
            modifier = Modifier.size(80.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = stringResource(nameRes),
            style = MaterialTheme.typography.titleMedium,
            color = AccentOffWhite,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = stringResource(statsRes),
            style = MaterialTheme.typography.labelSmall,
            color = TextLightGray,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.weight(1f))

        if (selected) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(AccentMutedBlue)
            )
        }
    }
}

@Composable
private fun RunRow(displayIndex: Int, run: RunHistoryUi) {
    val resultColor = when (run.result) {
        RunResultUi.CLEAR -> AccentMutedGreen
        RunResultUi.FELL -> AccentMutedRed
        RunResultUi.FLED -> AccentMutedBlue
    }
    val resultLabel = when (run.result) {
        RunResultUi.CLEAR -> stringResource(R.string.run_result_clear)
        RunResultUi.FELL -> stringResource(R.string.run_result_fell)
        RunResultUi.FLED -> stringResource(R.string.run_result_fled)
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        ) {
            Text(
                text = stringResource(R.string.run_index, displayIndex),
                style = MaterialTheme.typography.labelSmall,
                color = TextLightGray,
                modifier = Modifier.width(56.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(run.locationRes),
                    style = MaterialTheme.typography.titleMedium,
                    color = AccentOffWhite,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(resultColor)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = resultLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = resultColor
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = stringResource(R.string.run_floor, run.floorReached),
                        style = MaterialTheme.typography.labelSmall,
                        color = TextLightGray
                    )
                }
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(PanelDarkGray)
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D0D0D, heightDp = 900)
@Composable
private fun MainScreenPreview() {
    FatigaTheme {
        MainScreen(
            uiState = MainUiState(
                playerName = "The Nameless",
                soulLevel = 4,
                totalRuns = 2,
                runs = listOf(
                    RunHistoryUi(2, R.string.location_sunken_keep, RunResultUi.FELL, 6),
                    RunHistoryUi(1, R.string.location_hollow_road, RunResultUi.CLEAR, 8)
                ),
                isLoading = false
            ),
            onClassSelected = {},
            onPlay = {},
            onSettings = {}
        )
    }
}
