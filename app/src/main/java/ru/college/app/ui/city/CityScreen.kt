package ru.college.app.ui.city

import androidx.annotation.StringRes
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
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.college.app.R
import ru.college.app.presentation.CityUiState
import ru.college.app.presentation.HeroClassUi
import ru.college.app.ui.components.GlyphArt
import ru.college.app.ui.components.PixelGlyph
import ru.college.app.ui.theme.AccentMutedBlue
import ru.college.app.ui.theme.AccentMutedRed
import ru.college.app.ui.theme.AccentOffWhite
import ru.college.app.ui.theme.BgBlack
import ru.college.app.ui.theme.PanelDarkGray
import ru.college.app.ui.theme.TextLightGray
import java.util.Locale

/**
 * Screen 3 — the city hub. Three branches from the Figma reference:
 * Shrine (skill tree), Armory (gear/inventory) and Portal (combat).
 */
@Composable
fun CityScreen(
    uiState: CityUiState,
    onShrine: () -> Unit,
    onToggleArmory: () -> Unit,
    onUpgradeWeapon: () -> Unit,
    onUpgradeArmor: () -> Unit,
    onBuyPotion: (String) -> Unit,
    onPortal: () -> Unit,
    onBack: () -> Unit,
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
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatBox(
                    labelRes = R.string.city_gold,
                    value = remember(uiState.gold) {
                        String.format(Locale.US, "%,d", uiState.gold)
                    },
                    modifier = Modifier.weight(1f)
                )
                StatBox(
                    labelRes = R.string.city_offerings,
                    value = remember(uiState.offerings) {
                        String.format(Locale.US, "%02d", uiState.offerings)
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))
            Text(
                text = stringResource(R.string.city_title),
                style = MaterialTheme.typography.headlineLarge,
                color = AccentOffWhite
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.city_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = TextLightGray
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(
                    R.string.city_hero_line,
                    stringResource(classNameRes(uiState.heroClass)),
                    uiState.hp,
                    uiState.maxHp,
                    uiState.attack,
                    uiState.defense
                ),
                style = MaterialTheme.typography.labelSmall,
                color = TextLightGray
            )

            Spacer(modifier = Modifier.height(28.dp))

            CityCard(
                accent = AccentMutedRed,
                index = stringResource(R.string.city_index_shrine),
                art = GlyphArt.SHRINE,
                titleRes = R.string.city_shrine_title,
                descriptionRes = R.string.city_shrine_desc,
                actionRes = R.string.city_shrine_action,
                onClick = onShrine
            )

            Spacer(modifier = Modifier.height(12.dp))

            CityCard(
                accent = AccentMutedBlue,
                index = stringResource(R.string.city_index_armory),
                art = GlyphArt.SWORD,
                titleRes = R.string.city_armory_title,
                descriptionRes = R.string.city_armory_desc,
                actionRes = R.string.city_armory_action,
                onClick = onToggleArmory
            )

            if (uiState.armoryOpen) {
                Spacer(modifier = Modifier.height(8.dp))
                ArmoryPanel(
                    uiState = uiState,
                    onUpgradeWeapon = onUpgradeWeapon,
                    onUpgradeArmor = onUpgradeArmor,
                    onBuyPotion = onBuyPotion
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            CityCard(
                accent = AccentMutedBlue,
                index = stringResource(R.string.city_index_portal),
                art = GlyphArt.PORTAL,
                titleRes = R.string.city_portal_title,
                descriptionRes = R.string.city_portal_desc,
                actionRes = R.string.city_portal_action,
                onClick = onPortal
            )

            if (uiState.messageRes != null || uiState.errorRes != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(uiState.errorRes ?: uiState.messageRes!!),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (uiState.errorRes != null) {
                        MaterialTheme.colorScheme.error
                    } else {
                        AccentOffWhite
                    }
                )
            }

            Spacer(modifier = Modifier.height(28.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(PanelDarkGray)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.city_profile),
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp),
                    color = AccentOffWhite,
                    modifier = Modifier
                        .clickable(onClick = onBack)
                        .padding(vertical = 8.dp)
                )
                Text(
                    text = stringResource(R.string.city_safe_ground),
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp),
                    color = TextLightGray
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun StatBox(
    @StringRes labelRes: Int,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .border(1.dp, PanelDarkGray, RoundedCornerShape(0.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Text(
            text = stringResource(labelRes),
            style = MaterialTheme.typography.labelSmall,
            color = TextLightGray
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            color = AccentOffWhite
        )
    }
}

@Composable
private fun CityCard(
    accent: Color,
    index: String,
    art: GlyphArt,
    @StringRes titleRes: Int,
    @StringRes descriptionRes: Int,
    @StringRes actionRes: Int,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, PanelDarkGray, RoundedCornerShape(0.dp))
            .drawBehind {
                drawRect(
                    color = accent,
                    size = Size(width = 4.dp.toPx(), height = size.height)
                )
            }
            .clickable(onClick = onClick)
            .padding(start = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .width(88.dp)
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PixelGlyph(
                art = art,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = index,
                style = MaterialTheme.typography.labelSmall,
                color = TextLightGray
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(titleRes),
                style = MaterialTheme.typography.titleLarge,
                color = AccentOffWhite
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(descriptionRes),
                style = MaterialTheme.typography.bodySmall,
                color = TextLightGray
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(actionRes),
                    style = MaterialTheme.typography.labelLarge,
                    color = AccentOffWhite
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.symbol_chevron_right),
                    style = MaterialTheme.typography.labelLarge,
                    color = AccentOffWhite
                )
            }
        }
    }
}

@Composable
private fun ArmoryPanel(
    uiState: CityUiState,
    onUpgradeWeapon: () -> Unit,
    onUpgradeArmor: () -> Unit,
    onBuyPotion: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, PanelDarkGray, RoundedCornerShape(0.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(R.string.city_armory_panel_title),
                style = MaterialTheme.typography.labelLarge,
                color = AccentOffWhite
            )
            Text(
                text = stringResource(
                    R.string.city_gold_short,
                    String.format(Locale.US, "%,d", uiState.gold)
                ),
                style = MaterialTheme.typography.labelSmall,
                color = TextLightGray
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        ArmoryRow(
            titleRes = R.string.city_armory_weapon,
            subtitle = stringResource(R.string.city_level, uiState.weaponLevel),
            cost = uiState.weaponCost,
            enabled = uiState.gold >= uiState.weaponCost,
            onBuy = onUpgradeWeapon
        )
        Spacer(modifier = Modifier.height(12.dp))
        ArmoryRow(
            titleRes = R.string.city_armory_armor,
            subtitle = stringResource(R.string.city_level, uiState.armorLevel),
            cost = uiState.armorCost,
            enabled = uiState.gold >= uiState.armorCost,
            onBuy = onUpgradeArmor
        )
        Spacer(modifier = Modifier.height(12.dp))
        ArmoryRow(
            titleRes = R.string.city_armory_potion_red,
            subtitle = stringResource(R.string.city_owned, uiState.redPotions),
            cost = uiState.redPotionPrice,
            enabled = uiState.gold >= uiState.redPotionPrice,
            onBuy = { onBuyPotion("POTION_RED") }
        )
        Spacer(modifier = Modifier.height(12.dp))
        ArmoryRow(
            titleRes = R.string.city_armory_potion_blue,
            subtitle = stringResource(R.string.city_owned, uiState.bluePotions),
            cost = uiState.bluePotionPrice,
            enabled = uiState.gold >= uiState.bluePotionPrice,
            onBuy = { onBuyPotion("POTION_BLUE") }
        )
    }
}

@Composable
private fun ArmoryRow(
    @StringRes titleRes: Int,
    subtitle: String,
    cost: Int,
    enabled: Boolean,
    onBuy: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(titleRes),
                style = MaterialTheme.typography.labelLarge,
                color = AccentOffWhite
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = TextLightGray
            )
        }
        Text(
            text = stringResource(R.string.city_cost, cost),
            style = MaterialTheme.typography.labelSmall,
            color = if (enabled) AccentOffWhite else TextLightGray
        )
        Spacer(modifier = Modifier.width(12.dp))
        Box(
            modifier = Modifier
                .width(72.dp)
                .height(40.dp)
                .background(if (enabled) AccentOffWhite else PanelDarkGray)
                .clickable(enabled = enabled, onClick = onBuy),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.city_buy),
                style = MaterialTheme.typography.labelSmall,
                color = if (enabled) BgBlack else TextLightGray
            )
        }
    }
}

private fun classNameRes(heroClass: HeroClassUi): Int = when (heroClass) {
    HeroClassUi.WARRIOR -> R.string.main_warrior
    HeroClassUi.ARCHER -> R.string.main_archer
}
