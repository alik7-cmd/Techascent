package org.techascent.muslim.city.state

sealed interface CityPickerUiState{

    data object Loading: CityPickerUiState
    data class Success(val data: List<Country>): CityPickerUiState
}

data class City(
    val name: String,
    val lat: Double,
    val lng: Double
)

data class Country(
    val name: String,
    val cities: List<City>
)