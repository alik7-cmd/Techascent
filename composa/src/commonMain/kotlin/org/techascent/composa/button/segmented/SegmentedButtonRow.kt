package org.techascent.composa.button.segmented

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MultiChoiceSegmentedButtonRow
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonColors
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import org.jetbrains.compose.resources.painterResource
import org.techascent.composa.common.DrawableData

// Data class to hold information for each segment
data class SegmentedButtonItem(
    val id: String,
    val label: String? = null,
    val icon: DrawableData? = null
)

// Enum to define selection type
enum class SelectionType {
    SINGLE, MULTI
}

/**
 * A Composable that displays a row of segmented buttons,
 * Allowing for either single or multiple selections.
 * Each button can contain an optional icon and/or an optional text label.
 *
 * This component utilizes Material 3's [SingleChoiceSegmentedButtonRow] and
 * [MultiChoiceSegmentedButtonRow] to provide appropriate container behavior and accessibility.
 * The shape of individual buttons is dynamically calculated using
 * [SegmentedButtonDefaults.itemShape] to create the typical
 * segmented button appearance (e.g., rounded outer edges, flat inner edges).
 *
 * @param modifier The [Modifier] to be applied to the entire segmented button row.
 * @param items A list of [SegmentedButtonItem]s, where each item defines the content
 *      (ID, label, icon) for a segment in the row.
 * @param selectionType The [SelectionType] determining the selection behavior:
 *       - [SelectionType.SINGLE]: Only one item can be selected at a time.
 *       - [SelectionType.MULTI]: Multiple items can be selected simultaneously.
 *       Defaults to [SelectionType.SINGLE].
 * @param selectedIds A [Set] of [String]s representing the IDs of the currently selected items.
 *       For [SelectionType.SINGLE], this set should ideally contain zero or one ID.
 *       For [SelectionType.MULTI], it can contain multiple IDs.
 * @param onItemSelected A callback lambda that is invoked when an item's selection state changes.
 *       It provides the `itemId` (String) of the affected item and `isSelected` (Boolean)
 *       indicating its new selection state.
 *       For single selection, `isSelected` will always be `true` as clicking an item selects it.
 * @param enabled A boolean indicating whether the segmented button row and its items are enabled.
 *       Disabled items are typically non-interactive and visually distinct. Defaults to `true`.
 * @param colors [SegmentedButtonColors] to be used for styling the segmented buttons
 *       (e.g., container color, content color, border color) in different
 *       states (selected, unselected, disabled). Defaults to [SegmentedButtonDefaults.colors].
 * @param baseShape The base [CornerBasedShape] used by [SegmentedButtonDefaults.itemShape]
 *       to determine the curvature of the outer corners of the segmented button group.
 *       Defaults to [SegmentedButtonDefaults.baseShape].
 *
 *
 * ### Example
 * ```
 * val items = listOf(
 *         SegmentedButtonItem(id = "home", label = "Home", icon = null),
 *         SegmentedButtonItem(id = "profile", label = "Profile", icon = null),
 *         SegmentedButtonItem(id = "settings", label = "Settings", icon = null)
 *     )
 * var selectedItemIds by remember { mutableStateOf(setOf(items.first().id)) }
 * SegmentedButtonRow(
 *             items = items,
 *             selectionType = SelectionType.SINGLE,
 *             selectedIds = selectedItemIds,
 *             onItemSelected = { itemId, _ -> // For single select, isSelected is always true
 *                 selectedItemIds = setOf(itemId)
 *             },
 *             modifier = Modifier.padding(16.dp)
 *         )
 * ```
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SegmentedButtonRow(
    modifier: Modifier = Modifier,
    items: List<SegmentedButtonItem>,
    selectionType: SelectionType = SelectionType.SINGLE,
    selectedIds: Set<String>,
    onItemSelected: (itemId: String, isSelected: Boolean) -> Unit,
    enabled: Boolean = true,
    colors: SegmentedButtonColors = SegmentedButtonDefaults.colors(),
    baseShape: CornerBasedShape = SegmentedButtonDefaults.baseShape
) {
    if (items.isEmpty()) return

    when (selectionType) {
        SelectionType.SINGLE -> {
            /*val selectedIndex = items.indexOfFirst { it.id == selectedIds.firstOrNull() }
                .coerceAtLeast(0)*/
            SingleChoiceSegmentedButtonRow(modifier = modifier) {
                items.forEachIndexed { index, item ->
                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = items.size,
                            baseShape = baseShape
                        ),
                        onClick = {
                            onItemSelected(
                                item.id,
                                true
                            ) // For single select, it's always a selection
                        },
                        selected = item.id == selectedIds.firstOrNull(),
                        enabled = enabled,
                        colors = colors,
                        icon = {
                            item.icon?.let {
                                SegmentedButtonIcon(
                                    drawableData = it
                                )
                            }
                        }
                    ) {
                        item.label?.let { SegmentedButtonText(text = it) }
                    }
                }
            }
        }

        SelectionType.MULTI -> {
            MultiChoiceSegmentedButtonRow(modifier = modifier) {
                items.forEachIndexed { index, item ->
                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = items.size,
                            baseShape = baseShape
                        ),
                        onCheckedChange = { isChecked ->
                            onItemSelected(item.id, isChecked)
                        },
                        checked = item.id in selectedIds,
                        enabled = enabled,
                        colors = colors,
                        icon = {
                            item.icon?.let {
                                SegmentedButtonIcon(
                                    drawableData = it
                                )
                            }
                        }
                    ) {
                        item.label?.let { SegmentedButtonText(text = it) }

                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SegmentedButtonIcon(
    drawableData: DrawableData
) {
    Icon(
        painter = painterResource(drawableData.imageRes),
        contentDescription = drawableData.contentDescription,
        modifier = Modifier.size(SegmentedButtonDefaults.IconSize)
    )
}

@Composable
private fun SegmentedButtonText(
    text: String
) {
    Text(text = text, overflow = TextOverflow.Clip, maxLines = 1)
}

