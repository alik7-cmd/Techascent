package org.techascent.muslim.method.state

import apphub.composeapp.generated.resources.Res
import apphub.composeapp.generated.resources.text_hanafi
import apphub.composeapp.generated.resources.text_shafi
import org.techascent.shared.data.enum.School

data class MethodUiState (
    val listOfMethods: List<School> = enumValues<School>().toList()
)

fun School.toStringRes() = when(this){
    School.SHAFI -> Res.string.text_shafi
    School.HANAFI -> Res.string.text_hanafi
}