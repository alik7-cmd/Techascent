package org.techascent.muslim

import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import org.techascent.muslim.common.location.Location
import org.techascent.muslim.common.location.LocationService
import platform.CoreLocation.*
import platform.Foundation.NSError
import platform.darwin.NSObject
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume

class IOSLocationService : LocationService {
    private val locationManager = CLLocationManager()
    private var continuation: Continuation<Location?>? = null

    override suspend fun getCurrentLocation(): Location? {
        return suspendCancellableCoroutine { cont ->
            continuation = cont

            // Set up delegate
            val delegate = object : NSObject(), CLLocationManagerDelegateProtocol {
                @OptIn(ExperimentalForeignApi::class)
                override fun locationManager(manager: CLLocationManager, didUpdateLocations: List<*>) {
                    val loc = (didUpdateLocations.firstOrNull() as? CLLocation)
                    val coordinate = loc?.coordinate
                    if (coordinate != null) {
                        coordinate.useContents {
                            cont.resume(Location(this.latitude, this.longitude))
                        }
                    } else {
                        cont.resume(null)
                    }
                    locationManager.delegate = null // remove delegate to avoid leaks
                }

                override fun locationManager(manager: CLLocationManager, didFailWithError: NSError) {
                    cont.resume(null)
                    locationManager.delegate = null
                }
            }

            locationManager.delegate = delegate
            locationManager.requestWhenInUseAuthorization()
            locationManager.requestLocation()
        }
    }
}
