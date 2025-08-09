package org.techascent.composa.appbar

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.techascent.composa.common.DrawableData
import org.techascent.composa.theming.ComposaTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.ui.Alignment
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.techascent.composa.button.text.ComposaButtonText
import org.techascent.composa.progressbar.LoadingSpinner

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    navigationIcon: DrawableResource? = null,
    navigationIconContentDescription: String? = null,
    onNavigationIconClicked: (() -> Unit)? = null,
    scrollBehavior: TopAppBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(),
    action: TrailingAction? = null
) {

    val actionTint = if (isSystemInDarkTheme()) {
        ComposaTheme.color.iconAction
    } else {
        ComposaTheme.color.iconActionOnDark
    }


    Column {
        TopAppBar(
            title = {
                Text(
                    text = title,
                    style = ComposaTheme.typography.titleEmphasized,
                    color = ComposaTheme.color.textNeutral,
                )
            },
            modifier = modifier,
            navigationIcon = {
                if (navigationIcon != null) {
                    IconButton(
                        onClick = {
                            if (onNavigationIconClicked != null) {
                                onNavigationIconClicked()
                            }
                        }
                    ) {
                        Icon(
                            painter = painterResource(resource = navigationIcon),
                            contentDescription = navigationIconContentDescription,
                            tint = actionTint
                        )
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = ComposaTheme.color.backgroundAppBackground,
                titleContentColor = ComposaTheme.color.textNeutral,
                scrolledContainerColor = ComposaTheme.color.backgroundAppBackground,
            ),
            scrollBehavior = scrollBehavior,
            actions = {
                if (action != null) {
                    when (action) {
                        is TrailingAction.IconButton -> {
                            IconButton(onClick = action.onClick) {
                                Icon(
                                    modifier = Modifier.align(Alignment.CenterVertically),
                                    painter = painterResource(resource = action.icon.imageRes),
                                    contentDescription = action.icon.contentDescription,
                                    tint = actionTint
                                )
                            }
                        }

                        TrailingAction.LoadingIndicator -> {
                            LoadingSpinner(
                                color = actionTint,
                                modifier = Modifier
                                    .align(Alignment.CenterVertically)
                                    .minimumInteractiveComponentSize()
                            )
                        }

                        is TrailingAction.TextButton -> {
                            ComposaButtonText(
                                text = action.text,
                                onClick = action.onClick,
                                isEnabled = action.enabled,
                                modifier = Modifier
                                    .align(Alignment.CenterVertically)
                            )
                        }
                    }
                }
            }
        )
    }
}

sealed interface TrailingAction {
    data class IconButton(val icon: DrawableData, val onClick: () -> Unit) : TrailingAction
    data class TextButton(val text: String, val onClick: () -> Unit, val enabled: Boolean = true) :
        TrailingAction

    data object LoadingIndicator : TrailingAction
}
