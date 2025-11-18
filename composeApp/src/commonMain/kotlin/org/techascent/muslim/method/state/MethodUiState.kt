package org.techascent.muslim.method.state

import apphub.composeapp.generated.resources.Res
import apphub.composeapp.generated.resources.title_nearby_mosque
import org.techascent.shared.data.enum.School

data class MethodUiState (
    val listOfMethods: List<School> = enumValues<School>().toList()
)

fun School.toStringRes() = when(this){
    School.SHAFI -> Res.string.title_nearby_mosque
    School.HANAFI -> TODO()
}