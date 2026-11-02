package org.techascent.aifinder.data.location

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.techascent.aifinder.domain.location.LocationError

// iOS needs GoogleService-Info.plist and the Firebase iOS SDK before Realtime Database can be used.
class UnavailableLocationDataSource : LocationRemoteDataSource {
    override fun observe(userId: String): Flow<List<Pair<String, LocationDto>>> =
        flow { throw LocationError.Unavailable }

    override suspend fun create(userId: String, location: LocationDto): String = throw LocationError.Unavailable
}
