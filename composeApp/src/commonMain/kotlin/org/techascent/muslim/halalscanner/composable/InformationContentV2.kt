package org.techascent.muslim.halalscanner.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import apphub.composeapp.generated.resources.Res
import apphub.composeapp.generated.resources.text_done
import apphub.composeapp.generated.resources.text_ingredients
import apphub.composeapp.generated.resources.text_labels
import apphub.composeapp.generated.resources.text_verdict
import org.jetbrains.compose.resources.stringResource
import org.techascent.composa.asyncimage.ComposeAsyncImage
import org.techascent.composa.button.primary.ComposaButton
import org.techascent.composa.common.ComposaSpacing
import org.techascent.composa.theming.ComposaTheme
import org.techascent.muslim.halalscanner.state.ProductUiState
import org.techascent.shared.data.mapper.FlaggedIngredient
import org.techascent.shared.data.mapper.FlagType
import org.techascent.shared.data.mapper.HalalStatus

@Composable
internal fun InformationContentV2(
    productUiState: ProductUiState,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()
    val statusColor = statusColor(productUiState.halalUiState.status)
    val statusBg = statusColor.copy(alpha = 0.08f)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
            .background(ComposaTheme.color.backgroundAppBackground)
            .padding(bottom = ComposaSpacing.Large),
    ) {
        // ── Hero Banner ─────────────────────────────────────────────────────────
        StatusBanner(
            productUiState = productUiState,
            statusColor = statusColor,
            statusBg = statusBg,
        )

        Spacer(modifier = Modifier.height(ComposaSpacing.Medium))

        // ── Verdict Explanation ─────────────────────────────────────────────────
        VerdictCard(
            productUiState = productUiState,
            statusColor = statusColor,
        )

        // ── Ingredients Section ─────────────────────────────────────────────────
        if (!productUiState.ingredientsText.isNullOrEmpty()) {
            Spacer(modifier = Modifier.height(ComposaSpacing.Medium))
            IngredientsSection(
                ingredients = productUiState.ingredientsText,
                flaggedIngredients = productUiState.flaggedIngredients,
            )
        }

        // ── Labels / Tags ───────────────────────────────────────────────────────
        if (!productUiState.labelsTags.isNullOrEmpty()) {
            Spacer(modifier = Modifier.height(ComposaSpacing.Medium))
            LabelsSection(labels = productUiState.labelsTags)
        }

        Spacer(modifier = Modifier.height(ComposaSpacing.Large))

        // ── Action Buttons ──────────────────────────────────────────────────────
        ComposaButton(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = ComposaSpacing.Medium),
            text = stringResource(Res.string.text_done),
            onClick = onNavigateBack,
            iconTint = Color.Unspecified,
        )
    }
}

// ─── Hero Banner ────────────────────────────────────────────────────────────────

@Composable
private fun StatusBanner(
    productUiState: ProductUiState,
    statusColor: Color,
    statusBg: Color,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(statusBg)
            .padding(vertical = ComposaSpacing.Large),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = spacedBy(ComposaSpacing.Small),
        ) {
            // Product image or fallback icon
            if (productUiState.imageUrl != null) {
                ComposeAsyncImage(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(ComposaTheme.color.backgroundAppBackground),
                    model = productUiState.imageUrl,
                    contentScale = ContentScale.Crop,
                    contentDescription = productUiState.labels,
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(statusColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = statusEmoji(productUiState.halalUiState.status),
                        fontSize = 44.sp,
                    )
                }
            }

            // Status badge
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(statusColor.copy(alpha = 0.15f))
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = spacedBy(8.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(statusColor),
                )
                Text(
                    text = stringResource(productUiState.halalUiState.halalStatusRes),
                    color = statusColor,
                    style = ComposaTheme.typography.bodyEmphasized,
                    fontWeight = FontWeight.Bold,
                )
            }

            // Brand name
            if (!productUiState.brands.isNullOrBlank()) {
                Text(
                    text = productUiState.brands,
                    style = ComposaTheme.typography.titleMediumEmphasized,
                    color = ComposaTheme.color.textNeutral,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = ComposaSpacing.Large),
                )
            }

            // Labels
            if (!productUiState.labels.isNullOrBlank()) {
                Text(
                    text = productUiState.labels,
                    style = ComposaTheme.typography.footnote,
                    color = ComposaTheme.color.textNeutralSubtle,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = ComposaSpacing.Large),
                )
            }
        }
    }
}

// ─── Verdict Card ───────────────────────────────────────────────────────────────

@Composable
private fun VerdictCard(
    productUiState: ProductUiState,
    statusColor: Color,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ComposaSpacing.Medium)
            .clip(RoundedCornerShape(16.dp))
            .background(ComposaTheme.color.strokeNeutralSubtle.copy(alpha = 0.15f))
            .padding(ComposaSpacing.Medium),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = spacedBy(ComposaSpacing.Small),
        ) {
            Box(
                modifier = Modifier
                    .size(4.dp, 20.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(statusColor),
            )
            Text(
                text = stringResource(Res.string.text_verdict),
                style = ComposaTheme.typography.bodyEmphasized,
                color = ComposaTheme.color.textNeutral,
            )
        }

        Spacer(modifier = Modifier.height(ComposaSpacing.Small))

        Text(
            text = stringResource(productUiState.halalUiState.reasonRes),
            style = ComposaTheme.typography.footnote.copy(lineHeight = 22.sp),
            color = ComposaTheme.color.textNeutral.copy(alpha = 0.8f),
        )
    }
}

// ─── Ingredients Section ────────────────────────────────────────────────────────

private fun ingredientFlagType(
    ingredient: String,
    flaggedIngredients: List<FlaggedIngredient>,
): FlagType? {
    val lower = ingredient.lowercase()
    return flaggedIngredients.firstOrNull { lower.contains(it.name.lowercase()) }?.type
}

@Composable
private fun IngredientsSection(
    ingredients: List<String>,
    flaggedIngredients: List<FlaggedIngredient> = emptyList(),
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ComposaSpacing.Medium),
    ) {
        Text(
            text = stringResource(Res.string.text_ingredients),
            style = ComposaTheme.typography.bodyEmphasized,
            color = ComposaTheme.color.textNeutral,
        )

        Spacer(modifier = Modifier.height(ComposaSpacing.Small))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(ComposaTheme.color.strokeNeutralSubtle.copy(alpha = 0.15f)),
        ) {
            ingredients.forEachIndexed { index, ingredient ->
                val flagType = ingredientFlagType(ingredient, flaggedIngredients)
                val rowBg = when (flagType) {
                    FlagType.NON_HALAL -> Color(0xFFC62828).copy(alpha = 0.08f)
                    FlagType.DOUBTFUL  -> Color(0xFFEF6C00).copy(alpha = 0.08f)
                    null               -> Color.Transparent
                }
                val bulletColor = when (flagType) {
                    FlagType.NON_HALAL -> Color(0xFFC62828)
                    FlagType.DOUBTFUL  -> Color(0xFFEF6C00)
                    null               -> ComposaTheme.color.textNeutralSubtle
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(rowBg)
                        .padding(
                            horizontal = ComposaSpacing.Medium,
                            vertical = 12.dp,
                        ),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = spacedBy(ComposaSpacing.Small),
                ) {
                    Text(
                        text = "•",
                        style = ComposaTheme.typography.body,
                        color = bulletColor,
                    )
                    Text(
                        text = ingredient.trim(),
                        style = ComposaTheme.typography.footnote,
                        color = ComposaTheme.color.textNeutral.copy(alpha = 0.85f),
                        modifier = Modifier.weight(1f),
                    )
                    if (flagType != null) {
                        val badgeColor = bulletColor
                        val badgeLabel = when (flagType) {
                            FlagType.NON_HALAL -> "❌ Haram"
                            FlagType.DOUBTFUL  -> "⚠️ Doubtful"
                        }
                        Text(
                            text = badgeLabel,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(badgeColor.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 3.dp),
                            style = ComposaTheme.typography.caption,
                            color = badgeColor,
                        )
                    }
                }
                if (index < ingredients.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = ComposaSpacing.Medium),
                        color = ComposaTheme.color.strokeNeutralSubtle.copy(alpha = 0.3f),
                    )
                }
            }
        }
    }
}

// ─── Labels / Tags Section ──────────────────────────────────────────────────────

@Composable
private fun LabelsSection(labels: List<String>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ComposaSpacing.Medium),
    ) {
        Text(
            text = stringResource(Res.string.text_labels),
            style = ComposaTheme.typography.bodyEmphasized,
            color = ComposaTheme.color.textNeutral,
        )

        Spacer(modifier = Modifier.height(ComposaSpacing.Small))

        // Wrap flow of tag chips
        FlowRow(labels)
    }
}

@Composable
private fun FlowRow(tags: List<String>) {
    // Simple horizontal wrapping via multiple rows
    Column(verticalArrangement = spacedBy(8.dp)) {
        var currentRow = mutableListOf<String>()
        val rows = mutableListOf<List<String>>()

        tags.forEach { tag ->
            currentRow.add(tag)
            if (currentRow.size >= 3) {
                rows.add(currentRow.toList())
                currentRow = mutableListOf()
            }
        }
        if (currentRow.isNotEmpty()) rows.add(currentRow)

        rows.forEach { row ->
            Row(horizontalArrangement = spacedBy(8.dp)) {
                row.forEach { tag ->
                    TagChip(tag = tag)
                }
            }
        }
    }
}

@Composable
private fun TagChip(tag: String) {
    Text(
        text = tag.replace("en:", "").replace("-", " ").replaceFirstChar { it.uppercase() },
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(ComposaTheme.color.strokeNeutralSubtle.copy(alpha = 0.2f))
            .padding(horizontal = 14.dp, vertical = 6.dp),
        style = ComposaTheme.typography.caption,
        color = ComposaTheme.color.textNeutral.copy(alpha = 0.75f),
    )
}

// ─── Status Color Helper ────────────────────────────────────────────────────────

@Composable
private fun statusColor(status: HalalStatus): Color {
    return when (status) {
        HalalStatus.HALAL_CERTIFIED -> Color(0xFF2E7D32)   // deep green
        HalalStatus.HALAL_POSSIBLE -> Color(0xFF558B2F)    // olive green
        HalalStatus.HALAL_DOUBTFUL -> Color(0xFFEF6C00)    // amber
        HalalStatus.NOT_HALAL -> Color(0xFFC62828)         // red
        HalalStatus.UNKNOWN -> Color(0xFF757575)           // grey
    }
}

private fun statusEmoji(status: HalalStatus): String {
    return when (status) {
        HalalStatus.HALAL_CERTIFIED -> "✅"
        HalalStatus.HALAL_POSSIBLE -> "🟢"
        HalalStatus.HALAL_DOUBTFUL -> "⚠️"
        HalalStatus.NOT_HALAL -> "❌"
        HalalStatus.UNKNOWN -> "❓"
    }
}

