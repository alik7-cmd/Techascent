package org.techascent.muslim.common

import org.techascent.muslim.city.state.City
import org.techascent.muslim.city.state.Country

fun parseCountriesFromLines(lines: List<String>): List<Country> {
    val countriesMap = mutableMapOf<String, MutableList<City>>()

    lines.drop(1).forEach { line ->
        val parts = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)".toRegex())

        if (parts.size >= 11) {
            val cityName = parts[0].trim('"')
            val lat = parts[2].trim('"').toDoubleOrNull() ?: return@forEach
            val lng = parts[3].trim('"').toDoubleOrNull() ?: return@forEach
            val countryName = parts[4].trim('"')

            val city = City(cityName, lat, lng)
            countriesMap.getOrPut(countryName) { mutableListOf() }.add(city)
        }
    }

    return countriesMap.map { (countryName, cities) ->
        Country(countryName, cities)
    }
}