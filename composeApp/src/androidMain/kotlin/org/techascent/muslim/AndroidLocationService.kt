package org.techascent.muslim

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

/*
package org.techascent.muslim

import android.annotation.SuppressLint
import android.content.Context
import android.os.Looper
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.suspendCancellableCoroutine
import org.techascent.muslim.common.location.Location
import org.techascent.muslim.common.location.LocationService
import kotlin.coroutines.resume
import com.google.android.gms.location.*

class AndroidLocationService(private val context: Context) : LocationService {
    @SuppressLint("MissingPermission")
    override suspend fun getCurrentLocation(): Location? {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY, 5000L // interval: 5 seconds
        ).setMaxUpdates(1).build()

        return suspendCancellableCoroutine { cont ->
            val callback = object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    val loc = result.lastLocation
                    if (loc != null) {
                        cont.resume(Location(loc.latitude, loc.longitude))
                    } else {
                        cont.resume(null)
                    }
                    fusedLocationClient.removeLocationUpdates(this)
                }

                override fun onLocationAvailability(availability: LocationAvailability) {
                    if (!availability.isLocationAvailable) {
                        cont.resume(null)
                        fusedLocationClient.removeLocationUpdates(this)
                    }
                }
            }

            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                callback,
                Looper.getMainLooper()
            )

            cont.invokeOnCancellation {
                fusedLocationClient.removeLocationUpdates(callback)
            }
        }
    }
}*/
