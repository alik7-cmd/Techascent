package org.techascent.aifinder.data.location

import org.techascent.aifinder.domain.location.LocationError

// iOS needs GoogleService-Info.plist and the Firebase iOS SDK before Realtime Database can be used.
class UnavailableLocationDataSource : LocationRemoteDataSource {
    override suspend fun fetchAll(userId: String): List<Pair<String, LocationDto>> = throw LocationError.Unavailable

    override suspend fun create(userId: String, location: LocationDto): String = throw LocationError.Unavailable
}
