package org.techascent.muslim.servive

import android.annotation.SuppressLint
import android.content.Context
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import org.techascent.muslim.common.location.Location
import org.techascent.muslim.common.location.LocationService
import kotlin.coroutines.resume

class AndroidLocationService(private val context: Context) : LocationService {
    @SuppressLint("MissingPermission")
    override suspend fun getCurrentLocation(): Location? {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        // Use BALANCED_POWER_ACCURACY (network/cell location) — fast and accurate enough
        // for city-level prayer time calculation. Cap at 3 s to avoid blocking the UI.
        return withTimeoutOrNull(3_000L) {
            suspendCancellableCoroutine { cont ->
                fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_BALANCED_POWER_ACCURACY, null
                )
                    .addOnSuccessListener { location ->
                        cont.resume(location?.let { Location(it.latitude, it.longitude) })
                    }
                    .addOnFailureListener {
                        cont.resume(null)
                    }
            }
        }
    }
}