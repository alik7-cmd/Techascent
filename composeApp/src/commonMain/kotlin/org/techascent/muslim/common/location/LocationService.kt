package org.techascent.muslim.common.location

interface LocationService {
    suspend fun getCurrentLocation(): Location?
}

data class Location(val latitude: Double, val longitude: Double)