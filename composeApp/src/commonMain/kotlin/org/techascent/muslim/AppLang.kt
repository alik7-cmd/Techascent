package org.techascent.muslim

import apphub.composeapp.generated.resources.Res
import apphub.composeapp.generated.resources.bn
import apphub.composeapp.generated.resources.en
import apphub.composeapp.generated.resources.tr
import org.jetbrains.compose.resources.StringResource

enum class AppLang(
    val code: String,
    val stringRes: StringResource
) {
    English("en", Res.string.en),
    Bengali("bn", Res.string.bn),
    Turkish("tr", Res.string.tr)
}