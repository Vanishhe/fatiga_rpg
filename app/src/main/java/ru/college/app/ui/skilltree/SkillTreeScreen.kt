package ru.college.app.ui.skilltree

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.college.app.R
import ru.college.app.presentation.HeroClassUi
import ru.college.app.presentation.SkillNodeUi
import ru.college.app.presentation.SkillTreeUiState
import ru.college.app.ui.components.PixelSprite
import ru.college.app.ui.theme.AccentMutedGreen
import ru.college.app.ui.theme.AccentMutedRed
import ru.college.app.ui.theme.AccentOffWhite
import ru.college.app.ui.theme.BgBlack
import ru.college.app.ui.theme.PanelDarkGray
import ru.college.app.ui.theme.TextLightGray

/**
 * Shrine branch: two vertical paths. Only the hero's own class is interactive;
 * the other branch stays visible but greyed out.
 */
@Composable
fun SkillTreeScreen(
    uiState: SkillTreeUiState,
    onUnlock: (String) -> Unit,
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
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.skilltree_label),
                    style = MaterialTheme.typography.labelSmall,
                    color = TextLightGray
                )
                Text(
                    text = stringResource(R.string.skilltree_offerings, uiState.offerings),
                    style = MaterialTheme.typography.labelSmall,
                    color = AccentOffWhite
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = stringResource(R.string.skilltree_title),
                style = MaterialTheme.typography.displayMedium,
                color = AccentOffWhite
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.skilltree_subtitle),
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp),
                color = TextLightGray
            )

            Spacer(modifier = Modifier.height(28.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                SkillBranch(
                    titleRes = R.string.main_warrior,
                    drawableRes = R.drawable.hero_warrior,
                    nodes = uiState.warriorNodes,
                    active = uiState.heroClass == HeroClassUi.WARRIOR,
                    onUnlock = onUnlock,
                    modifier = Modifier.weight(1f)
                )
                SkillBranch(
                    titleRes = R.string.main_archer,
                    drawableRes = R.drawable.hero_archer,
                    nodes = uiState.archerNodes,
                    active = uiState.heroClass == HeroClassUi.ARCHER,
                    onUnlock = onUnlock,
                    modifier = Modifier.weight(1f)
                )
            }

            if (uiState.messageRes != null || uiState.errorRes != null) {
                Spacer(modifier = Modifier.height(16.dp))
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

            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = stringResource(R.string.skilltree_footer),
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp),
                color = TextLightGray
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.skilltree_back),
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp),
                color = AccentOffWhite,
                modifier = Modifier
                    .clickable(onClick = onBack)
                    .padding(vertical = 8.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SkillBranch(
    titleRes: Int,
    drawableRes: Int,
    nodes: List<SkillNodeUi>,
    active: Boolean,
    onUnlock: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .alpha(if (active) 1f else 0.35f)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            PixelSprite(
                drawableRes = drawableRes,
                contentDescription = null,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(titleRes),
                style = MaterialTheme.typography.labelLarge,
                color = AccentOffWhite
            )
        }
        if (!active) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.skilltree_inactive),
                style = MaterialTheme.typography.labelSmall,
                color = TextLightGray
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        nodes.forEachIndexed { index, node ->
            SkillNode(
                node = node,
                active = active,
                onUnlock = onUnlock
            )
            if (index != nodes.lastIndex) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .width(1.dp)
                        .height(12.dp)
                        .background(PanelDarkGray)
                )
            }
        }
    }
}

@Composable
private fun SkillNode(
    node: SkillNodeUi,
    active: Boolean,
    onUnlock: (String) -> Unit
) {
    val borderColor = when {
        node.unlocked -> AccentMutedGreen
        node.available && node.affordable -> AccentOffWhite
        else -> PanelDarkGray
    }
    val interactive = active && node.available

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (node.unlocked) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(0.dp)
            )
            .clickable(enabled = interactive) { onUnlock(node.id) }
            .padding(12.dp)
    ) {
        Text(
            text = stringResource(node.nameRes),
            style = MaterialTheme.typography.titleMedium,
            color = AccentOffWhite
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(node.descriptionRes),
            style = MaterialTheme.typography.bodySmall,
            color = TextLightGray
        )
        Spacer(modifier = Modifier.height(10.dp))

        when {
            node.unlocked -> Text(
                text = stringResource(R.string.skilltree_unlocked),
                style = MaterialTheme.typography.labelSmall,
                color = AccentMutedGreen
            )

            !node.available -> Text(
                text = stringResource(R.string.skilltree_locked),
                style = MaterialTheme.typography.labelSmall,
                color = TextLightGray
            )

            else -> Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.skilltree_cost, node.cost),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (node.affordable) AccentOffWhite else AccentMutedRed
                )
                Text(
                    text = stringResource(R.string.skilltree_unlock),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (node.affordable) AccentOffWhite else AccentMutedRed
                )
            }
        }
    }
}
