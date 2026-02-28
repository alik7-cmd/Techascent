package org.techascent.muslim

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.datetime.Instant
import okio.Path.Companion.toPath
import org.techascent.muslim.common.location.LocationService
import org.techascent.muslim.preference.DATA_STORE_FILE_NAME
import org.techascent.muslim.preference.createDataStore
import platform.AudioToolbox.AudioServicesPlaySystemSound
import platform.CoreLocation.CLHeading
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSString
import platform.Foundation.NSUserDomainMask
import platform.Foundation.NSURL
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.NSBundle
import platform.UIKit.UIAlertAction
import platform.UIKit.UIAlertActionStyleCancel
import platform.UIKit.UIAlertActionStyleDefault
import platform.UIKit.UIAlertController
import platform.UIKit.UIAlertControllerStyleAlert
import platform.UIKit.UIApplication
import platform.UIKit.UIImpactFeedbackGenerator
import platform.UIKit.UIImpactFeedbackStyle
import platform.UIKit.UIScreen
import platform.darwin.NSObject
import kotlin.coroutines.resume
import kotlin.math.*
import platform.CoreLocation.CLGeocoder
import platform.CoreLocation.CLLocation
import platform.CoreLocation.CLPlacemark
import org.techascent.muslim.prayer.uimodel.AddressInfo
import org.techascent.muslim.service.IOSLocationService
import org.techascent.muslim.service.IOSPrayerNotificationService
import platform.Foundation.stringWithContentsOfFile



actual fun playBeep() {
    AudioServicesPlaySystemSound(1057u)
}

actual fun performHapticFeedback() {
    val generator =
        UIImpactFeedbackGenerator(style = UIImpactFeedbackStyle.UIImpactFeedbackStyleMedium)
    generator.prepare()
    generator.impactOccurred()
}

@OptIn(ExperimentalForeignApi::class)
actual fun provideDataStore(): DataStore<Preferences> {
    return createDataStore {
        val directory = NSFileManager.defaultManager.URLForDirectory(
            directory = NSDocumentDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = false,
            error = null
        )
        requireNotNull(directory).path + "/$DATA_STORE_FILE_NAME"
    }
}

actual fun showNativeResetDialog(
    title: String,
    message: String,
    confirmText: String,
    cancelText: String,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    val alert = UIAlertController.alertControllerWithTitle(
        title = title,
        message = message,
        preferredStyle = UIAlertControllerStyleAlert
    )

    alert.addAction(UIAlertAction.actionWithTitle(confirmText, UIAlertActionStyleDefault) {
        onConfirm()
    })

    alert.addAction(UIAlertAction.actionWithTitle(cancelText, UIAlertActionStyleCancel) {
        onCancel()
    })

    val rootVC = UIApplication.sharedApplication.keyWindow?.rootViewController
    rootVC?.presentViewController(alert, animated = true, completion = null)
}

private const val qiblaLat = 21.4225
private const val qiblaLng = 39.8262

actual fun getQiblaDirection(currentLat: Double, currentLng: Double): Flow<Float> = callbackFlow {
    val locationManager = CLLocationManager().apply {
        requestWhenInUseAuthorization()
        headingFilter = 1.0
    }

    val delegate = object : NSObject(), CLLocationManagerDelegateProtocol {
        override fun locationManager(manager: CLLocationManager, didUpdateHeading: CLHeading) {
            val heading = didUpdateHeading.magneticHeading.toDouble()
            val bearingToQibla = calculateBearing(currentLat, currentLng, qiblaLat, qiblaLng)
            val direction = (bearingToQibla - heading + 360) % 360
            trySend(direction.toFloat())
        }

        override fun locationManagerShouldDisplayHeadingCalibration(manager: CLLocationManager): Boolean {
            return true
        }
    }

    locationManager.delegate = delegate
    locationManager.startUpdatingHeading()

    awaitClose {
        locationManager.stopUpdatingHeading()
        locationManager.delegate = null
    }
}

fun calculateBearing(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val φ1 = lat1.toRadians()
    val φ2 = lat2.toRadians()
    val Δλ = (lon2 - lon1).toRadians()

    val y = sin(Δλ) * cos(φ2)
    val x = cos(φ1) * sin(φ2) - sin(φ1) * cos(φ2) * cos(Δλ)

    return (atan2(y, x).toDegrees() + 360) % 360
}

fun Double.toRadians() = this * PI / 180.0
fun Double.toDegrees() = this * 180.0 / PI

@OptIn(ExperimentalForeignApi::class)
actual suspend fun readCsvFile(filename: String): List<String> {
    val nameWithoutExtension = filename.substringBeforeLast(".")
    val ext = filename.substringAfterLast(".", "")

    val path = NSBundle.mainBundle.pathForResource(nameWithoutExtension, ext) ?: return emptyList()

    val nsString: NSString? =
        NSString.stringWithContentsOfFile(path, NSUTF8StringEncoding, null) as NSString?
    val content: String = nsString?.toString() ?: return emptyList()

    return content
        .split("\n")
        .map { it.trim() }
        .filter { it.isNotEmpty() }
}

actual fun getPlatformLocationService(): LocationService {
    return IOSLocationService()
}

actual suspend fun getPlaceName(latitude: Double, longitude: Double): AddressInfo {
    return suspendCancellableCoroutine { continuation ->
        val geocoder = CLGeocoder()
        val location = CLLocation(latitude = latitude, longitude = longitude)

        geocoder.reverseGeocodeLocation(location) { placemarks, error ->
            if (error != null || placemarks == null || placemarks.count() == 0) {
                continuation.resume(
                    AddressInfo("", "", "", "")
                )
                return@reverseGeocodeLocation
            }

            val placemark = placemarks[0] as? CLPlacemark

            val address = listOfNotNull(
                placemark?.name,
                placemark?.locality,
                placemark?.country
            ).joinToString(", ")

            continuation.resume(
                AddressInfo(
                    district = placemark?.subAdministrativeArea,
                    city = placemark?.locality,
                    country = placemark?.country,
                    address = address
                )
            )
        }
    }
}

actual fun openNearbyMosques() {
    val googleMapsUrl = NSURL(string = "comgooglemaps://?q=mosque")
    val appleMapsUrl = NSURL(string = "http://maps.apple.com/?q=mosque")

    val app = UIApplication.sharedApplication
    when {
        app.canOpenURL(googleMapsUrl) -> app.openURL(googleMapsUrl)
        else -> app.openURL(appleMapsUrl)
    }
}

@OptIn(ExperimentalForeignApi::class)
actual fun getScreenWidthPx(): Int =
    UIScreen.mainScreen.bounds.useContents { size.width.toInt() }

@OptIn(ExperimentalForeignApi::class)
actual fun getScreenHeightPx(): Int =
    UIScreen.mainScreen.bounds.useContents { size.height.toInt() }

actual fun createDataStore(producePath: () -> String): DataStore<Preferences> =
    PreferenceDataStoreFactory.createWithPath(
        produceFile = { producePath().toPath() }
    )

actual fun getPrayerNotificationService(): PrayerNotificationService {
    return IOSPrayerNotificationService()
}


