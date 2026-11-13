package org.techascent.aifinder.presentation.legal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import apphub.aifinder.generated.resources.*
import org.jetbrains.compose.resources.stringArrayResource
import org.jetbrains.compose.resources.stringResource
import org.techascent.aifinder.presentation.components.ScreenLayout
import org.techascent.aifinder.presentation.components.ScreenTopBar
import org.techascent.aifinder.presentation.theme.AiFinderPalette

enum class LegalDocument { Terms, PrivacyPolicy }

@Composable
fun LegalScreen(document: LegalDocument, onBack: () -> Unit) {
    val title = stringResource(
        if (document == LegalDocument.Terms) Res.string.legal_terms_title else Res.string.legal_privacy_title
    )
    val sections = stringArrayResource(
        if (document == LegalDocument.Terms) Res.array.legal_terms_sections else Res.array.legal_privacy_sections
    )
    ScreenLayout(topBar = { ScreenTopBar(title, onBack) }) {
        item {
            Text(
                stringResource(Res.string.legal_updated),
                style = MaterialTheme.typography.labelLarge,
                color = AiFinderPalette.Muted,
            )
        }
        items(sections.size) { index ->
            val section = sections[index]
            SelectionContainer {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        section.substringBefore('\n'),
                        modifier = Modifier.semantics { heading() },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = AiFinderPalette.Ink,
                    )
                    Text(
                        section.substringAfter('\n'),
                        style = MaterialTheme.typography.bodyLarge,
                        color = AiFinderPalette.Ink,
                    )
                }
            }
        }
    }
}
