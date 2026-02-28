package org.techascent.muslim.servive

import android.annotation.SuppressLint
import android.content.Context
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.suspendCancellableCoroutine
import org.techascent.muslim.common.location.Location
import org.techascent.muslim.common.location.LocationService
import kotlin.coroutines.resume

class AndroidLocationService(private val context: Context) : LocationService {
    @SuppressLint("MissingPermission")
    override suspend fun getCurrentLocation(): Location? {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        return suspendCancellableCoroutine { cont ->
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener { location ->
                    cont.resume(location?.let {
                        Location(it.latitude, it.longitude)
                    })
                }
                .addOnFailureListener {
                    cont.resume(null)
                }
        }
    }
}