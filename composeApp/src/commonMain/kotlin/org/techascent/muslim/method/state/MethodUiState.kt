package org.techascent.muslim.method.state

import org.techascent.shared.data.enum.PrayerCalculationMethod

data class MethodUiState (
    val listOfMethods: List<PrayerCalculationMethod> = enumValues<PrayerCalculationMethod>().toList()
)