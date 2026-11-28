package ru.college.app.ui.battle

import androidx.activity.compose.BackHandler
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ru.college.app.R
import ru.college.app.presentation.BattleItemUi
import ru.college.app.presentation.BattleOutcome
import ru.college.app.presentation.BattlePhase
import ru.college.app.presentation.BattleUiState
import ru.college.app.presentation.HeroClassUi
import ru.college.app.ui.components.GlyphArt
import ru.college.app.ui.components.PixelButton
import ru.college.app.ui.components.PixelGlyph
import ru.college.app.ui.components.PixelHpBar
import ru.college.app.ui.components.PixelSprite
import ru.college.app.ui.theme.AccentMutedBlue
import ru.college.app.ui.theme.AccentMutedGreen
import ru.college.app.ui.theme.AccentMutedRed
import ru.college.app.ui.theme.AccentOffWhite
import ru.college.app.ui.theme.BgBlack
import ru.college.app.ui.theme.PanelDarkGray
import ru.college.app.ui.theme.TextLightGray
import kotlin.math.min

/**
 * Screen 4 — turn-based arena. Animations are driven by sequence counters in
 * BattleUiState so the ViewModel owns all combat timing.
 */
@Composable
fun BattleScreen(
    uiState: BattleUiState,
    onAttack: () -> Unit,
    onSpecial: () -> Unit,
    onFlee: () -> Unit,
    onUseItem: (String) -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onAbandon: () -> Unit,
    onOutcomeConfirmed: () -> Unit,
    modifier: Modifier = Modifier
) {
    // BACK pauses between turns; once the outcome is shown it confirms it,
    // so a death can never strand the player in the city without a hero.
    BackHandler {
        if (uiState.outcome == null) onPause() else onOutcomeConfirmed()
    }

    val heroNudge = remember { Animatable(0f) }
    val enemyNudge = remember { Animatable(0f) }
    val heroShake = remember { Animatable(0f) }
    val enemyShake = remember { Animatable(0f) }
    val heroFlash = remember { Animatable(0f) }
    val enemyFlash = remember { Animatable(0f) }
    val fleeOffset = remember { Animatable(0f) }
    // Death = white flash + fade to zero. Spawn = slide in from off-screen
    // right. Both are driven by sequence counters from BattleUiState.
    val enemyDeathFlash = remember { Animatable(0f) }
    // Start visible at rest so a restored battle never renders an invisible
    // enemy; the spawn effect overrides these values when it fires.
    val enemyAlpha = remember { Animatable(1f) }
    val enemySpawnOffset = remember { Animatable(0f) }

    LaunchedEffect(uiState.heroAttackSeq) {
        if (uiState.heroAttackSeq > 0) {
            heroNudge.snapTo(0f)
            heroNudge.animateTo(28f, tween(90))
            heroNudge.animateTo(0f, tween(160))
        }
    }
    LaunchedEffect(uiState.enemyAttackSeq) {
        if (uiState.enemyAttackSeq > 0) {
            enemyNudge.snapTo(0f)
            enemyNudge.animateTo(-28f, tween(90))
            enemyNudge.animateTo(0f, tween(160))
        }
    }
    LaunchedEffect(uiState.heroHitSeq) {
        if (uiState.heroHitSeq > 0) {
            heroFlash.snapTo(1f)
            heroFlash.animateTo(0f, tween(220))
            repeat(3) {
                heroShake.animateTo(6f, tween(40))
                heroShake.animateTo(-6f, tween(40))
            }
            heroShake.animateTo(0f, tween(40))
        }
    }
    LaunchedEffect(uiState.enemyHitSeq) {
        if (uiState.enemyHitSeq > 0) {
            enemyFlash.snapTo(1f)
            enemyFlash.animateTo(0f, tween(220))
            repeat(3) {
                enemyShake.animateTo(6f, tween(40))
                enemyShake.animateTo(-6f, tween(40))
            }
            enemyShake.animateTo(0f, tween(40))
        }
    }
    LaunchedEffect(uiState.enemyDeathSeq) {
        if (uiState.enemyDeathSeq > 0) {
            // Bright white silhouette, then dissolve before the next spawn.
            enemyDeathFlash.snapTo(1f)
            enemyDeathFlash.animateTo(0f, tween(300))
            enemyAlpha.animateTo(0f, tween(450))
        }
    }
    LaunchedEffect(uiState.enemySpawnSeq) {
        if (uiState.enemySpawnSeq > 0) {
            // Reset the corpse state, then slide the new enemy in from the right.
            enemyDeathFlash.snapTo(0f)
            enemyAlpha.snapTo(1f)
            enemySpawnOffset.snapTo(SPAWN_OFFSET_DP)
            enemySpawnOffset.animateTo(0f, tween(420))
        }
    }
    LaunchedEffect(uiState.fleeing) {
        if (uiState.fleeing) {
            fleeOffset.animateTo(-420f, tween(700))
        }
    }

    val heroTint = if (heroFlash.value > 0.45f) AccentOffWhite else null
    val enemyTint = when {
        // Keep the corpse white through the whole dissolve, not just the flash.
        enemyDeathFlash.value > 0.05f || enemyAlpha.value < 0.99f -> Color.White
        enemyFlash.value > 0.45f -> AccentOffWhite
        else -> null
    }
    val actionsEnabled = uiState.phase == BattlePhase.PLAYER_TURN &&
        !uiState.paused &&
        uiState.outcome == null &&
        !uiState.isLoading

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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.battle_dungeon),
                        style = MaterialTheme.typography.titleLarge,
                        color = AccentOffWhite
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(
                            R.string.battle_floor_turn,
                            uiState.floor,
                            uiState.turn
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        color = TextLightGray
                    )
                }
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .border(1.dp, PanelDarkGray, RoundedCornerShape(0.dp))
                        .clickable(onClick = onPause),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.battle_pause),
                        style = MaterialTheme.typography.titleMedium,
                        color = AccentOffWhite
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(PanelDarkGray)
            )
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(uiState.enemyNameRes),
                style = MaterialTheme.typography.headlineLarge,
                color = AccentOffWhite
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(uiState.enemyDescriptionRes),
                style = MaterialTheme.typography.bodySmall,
                color = TextLightGray
            )

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clipToBounds()
            ) {
                Image(
                    bitmap = ImageBitmap.imageResource(R.drawable.bg_portal_1),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    filterQuality = FilterQuality.None,
                    alpha = 0.35f
                )
                PixelSprite(
                    drawableRes = heroDrawable(uiState.heroClass),
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 24.dp, bottom = 16.dp)
                        .offset(
                            x = (
                                heroNudge.value +
                                    heroShake.value +
                                    fleeOffset.value
                                ).dp
                        )
                        .size(112.dp),
                    flipHorizontal = uiState.fleeing,
                    tint = heroTint
                )
                PixelSprite(
                    drawableRes = R.drawable.enemy_goblin,
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 24.dp, bottom = 16.dp)
                        .offset(
                            x = (
                                enemyNudge.value +
                                    enemyShake.value +
                                    enemySpawnOffset.value
                                ).dp
                        )
                        .size(112.dp),
                    flipHorizontal = true,
                    tint = enemyTint,
                    alpha = enemyAlpha.value
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            DashedDivider()
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                HpColumn(
                    name = uiState.heroName.ifBlank {
                        stringResource(R.string.main_unknown_soul)
                    },
                    hp = uiState.heroHp,
                    maxHp = uiState.heroMaxHp,
                    modifier = Modifier.weight(1f)
                )
                HpColumn(
                    name = stringResource(uiState.enemyNameRes),
                    hp = uiState.enemyHp,
                    maxHp = uiState.enemyMaxHp,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(PanelDarkGray)
            )
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = stringResource(uiState.logRes, uiState.logAmount),
                style = MaterialTheme.typography.bodyMedium,
                color = TextLightGray,
                modifier = Modifier.heightIn(min = 40.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(
                        if (uiState.phase == BattlePhase.ENEMY_TURN) {
                            R.string.battle_enemy_turn
                        } else {
                            R.string.battle_your_turn
                        }
                    ),
                    style = MaterialTheme.typography.labelLarge,
                    color = AccentOffWhite
                )
                Text(
                    text = stringResource(R.string.battle_turn, uiState.turn),
                    style = MaterialTheme.typography.labelSmall,
                    color = TextLightGray
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BattleActionButton(
                    textRes = R.string.battle_attack,
                    containerColor = AccentOffWhite,
                    contentColor = BgBlack,
                    enabled = actionsEnabled,
                    onClick = onAttack,
                    modifier = Modifier.weight(1f)
                )
                BattleActionButton(
                    textRes = R.string.battle_special,
                    containerColor = AccentMutedBlue,
                    contentColor = AccentOffWhite,
                    enabled = actionsEnabled,
                    onClick = onSpecial,
                    modifier = Modifier.weight(1f)
                )
                BattleActionButton(
                    textRes = R.string.battle_flee,
                    containerColor = AccentMutedRed,
                    contentColor = AccentOffWhite,
                    enabled = actionsEnabled,
                    onClick = onFlee,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.battle_inventory),
                    style = MaterialTheme.typography.labelLarge,
                    color = AccentOffWhite
                )
                Text(
                    text = stringResource(
                        R.string.battle_inventory_count,
                        uiState.inventory.size
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    color = TextLightGray
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            val slots = uiState.inventory.take(MAX_SLOTS)
            repeat(2) { rowIndex ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    repeat(4) { columnIndex ->
                        val item = slots.getOrNull(rowIndex * 4 + columnIndex)
                        InventorySlot(
                            item = item,
                            enabled = actionsEnabled && item != null,
                            onClick = { item?.let { onUseItem(it.id) } },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            Text(
                text = stringResource(R.string.battle_hint),
                style = MaterialTheme.typography.labelSmall,
                color = TextLightGray,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (uiState.paused && uiState.outcome == null) {
            PauseOverlay(onResume = onResume, onAbandon = onAbandon)
        }

        uiState.outcome?.let { outcome ->
            OutcomeOverlay(
                outcome = outcome,
                uiState = uiState,
                onOutcomeConfirmed = onOutcomeConfirmed
            )
        }
    }
}

@Composable
private fun HpColumn(
    name: String,
    hp: Int,
    maxHp: Int,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = name,
            style = MaterialTheme.typography.titleMedium,
            color = AccentOffWhite,
            maxLines = 1
        )
        Spacer(modifier = Modifier.height(8.dp))
        PixelHpBar(hp = hp, maxHp = maxHp)
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = stringResource(R.string.battle_hp_value, hp, maxHp),
            style = MaterialTheme.typography.labelSmall,
            color = TextLightGray
        )
    }
}

@Composable
private fun DashedDivider(modifier: Modifier = Modifier) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(8.dp)
    ) {
        val dash = 8.dp.toPx()
        val gap = 5.dp.toPx()
        val thickness = 2.dp.toPx()
        var x = 0f
        while (x < size.width) {
            drawRect(
                color = PanelDarkGray,
                topLeft = Offset(x, size.height / 2f - thickness / 2f),
                size = Size(min(dash, size.width - x), thickness)
            )
            x += dash + gap
        }
    }
}

@Composable
private fun BattleActionButton(
    @StringRes textRes: Int,
    containerColor: Color,
    contentColor: Color,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(56.dp)
            .background(if (enabled) containerColor else PanelDarkGray)
            .border(
                width = 1.dp,
                color = if (enabled) containerColor else PanelDarkGray,
                shape = RoundedCornerShape(0.dp)
            )
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(textRes),
            style = MaterialTheme.typography.titleMedium,
            color = if (enabled) contentColor else TextLightGray
        )
    }
}

@Composable
private fun InventorySlot(
    item: BattleItemUi?,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .border(1.dp, PanelDarkGray, RoundedCornerShape(0.dp))
            .clickable(enabled = enabled, onClick = onClick)
    ) {
        if (item == null) {
            Text(
                text = stringResource(R.string.battle_empty_slot),
                style = MaterialTheme.typography.labelSmall,
                color = TextLightGray,
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            PixelGlyph(
                art = if (item.blue) GlyphArt.POTION_BLUE else GlyphArt.POTION_RED,
                modifier = Modifier
                    .size(40.dp)
                    .align(Alignment.Center)
            )
            Text(
                text = stringResource(R.string.battle_item_count, item.count),
                style = MaterialTheme.typography.labelSmall,
                color = TextLightGray,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(4.dp)
            )
        }
    }
}

@Composable
private fun PauseOverlay(
    onResume: () -> Unit,
    onAbandon: () -> Unit
) {
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
                .border(1.dp, PanelDarkGray, RoundedCornerShape(0.dp))
                .padding(24.dp)
        ) {
            Text(
                text = stringResource(R.string.battle_pause_title),
                style = MaterialTheme.typography.headlineMedium,
                color = AccentOffWhite
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.battle_pause_body),
                style = MaterialTheme.typography.bodySmall,
                color = TextLightGray
            )
            Spacer(modifier = Modifier.height(24.dp))
            PixelButton(
                text = stringResource(R.string.battle_resume),
                onClick = onResume
            )
            Spacer(modifier = Modifier.height(12.dp))
            PixelButton(
                text = stringResource(R.string.battle_abandon),
                onClick = onAbandon,
                containerColor = AccentMutedRed,
                contentColor = AccentOffWhite
            )
        }
    }
}

@Composable
private fun OutcomeOverlay(
    outcome: BattleOutcome,
    uiState: BattleUiState,
    onOutcomeConfirmed: () -> Unit
) {
    val titleRes = when (outcome) {
        BattleOutcome.CLEAR -> R.string.battle_outcome_clear
        BattleOutcome.FLED -> R.string.battle_outcome_fled
        BattleOutcome.FELL -> R.string.battle_outcome_fell
    }
    val bodyRes = when (outcome) {
        BattleOutcome.CLEAR -> R.string.battle_outcome_clear_body
        BattleOutcome.FLED -> R.string.battle_outcome_fled_body
        BattleOutcome.FELL -> R.string.battle_outcome_fell_body
    }
    val buttonRes = when (outcome) {
        BattleOutcome.CLEAR, BattleOutcome.FLED -> R.string.battle_return_sanctuary
        BattleOutcome.FELL -> R.string.battle_return_profile
    }
    val accent = when (outcome) {
        BattleOutcome.CLEAR -> AccentMutedGreen
        BattleOutcome.FLED -> AccentMutedBlue
        BattleOutcome.FELL -> AccentMutedRed
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgBlack.copy(alpha = 0.96f))
            .safeDrawingPadding()
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, accent, RoundedCornerShape(0.dp))
                    .padding(24.dp)
            ) {
                Text(
                    text = stringResource(titleRes),
                    style = MaterialTheme.typography.headlineLarge,
                    color = accent
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(bodyRes),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextLightGray
                )
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = stringResource(
                        R.string.battle_outcome_floor,
                        uiState.outcomeFloor,
                        uiState.totalFloors
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    color = TextLightGray
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = stringResource(R.string.battle_outcome_gold, uiState.outcomeGold),
                    style = MaterialTheme.typography.labelSmall,
                    color = AccentOffWhite
                )
                if (uiState.outcomeOfferings > 0) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = stringResource(
                            R.string.battle_outcome_offerings,
                            uiState.outcomeOfferings
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        color = AccentOffWhite
                    )
                }
                if (uiState.errorRes != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = stringResource(uiState.errorRes),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            // Bottom-anchored and styled exactly like the Login ENTER button:
            // full width, high contrast, square border, chevron on the right.
            PixelButton(
                text = stringResource(buttonRes),
                onClick = onOutcomeConfirmed,
                trailingSymbol = stringResource(R.string.symbol_chevron_right)
            )
        }
    }
}

@DrawableRes
private fun heroDrawable(heroClass: HeroClassUi): Int = when (heroClass) {
    HeroClassUi.WARRIOR -> R.drawable.hero_warrior
    HeroClassUi.ARCHER -> R.drawable.hero_archer
}

private const val MAX_SLOTS = 8
private const val SPAWN_OFFSET_DP = 520f
