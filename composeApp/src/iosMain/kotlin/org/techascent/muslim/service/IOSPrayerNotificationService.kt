package org.techascent.muslim.service

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.datetime.Instant
import org.techascent.muslim.PrayerNotificationService
import platform.AVFAudio.AVAudioPlayer
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryPlayback
import platform.AVFAudio.setActive
import platform.Foundation.NSCalendar
import platform.Foundation.NSCalendarUnitDay
import platform.Foundation.NSCalendarUnitHour
import platform.Foundation.NSCalendarUnitMinute
import platform.Foundation.NSCalendarUnitMonth
import platform.Foundation.NSCalendarUnitSecond
import platform.Foundation.NSCalendarUnitYear
import platform.Foundation.NSBundle
import platform.Foundation.NSDate
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.registerForRemoteNotifications
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNCalendarNotificationTrigger
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNNotificationSound
import platform.UserNotifications.UNUserNotificationCenter


private var audioPlayer: AVAudioPlayer? = null


@OptIn(ExperimentalForeignApi::class)
class IOSPrayerNotificationService : PrayerNotificationService {

    init {
        requestNotificationPermissions()
    }

    private fun requestNotificationPermissions() {
        val center = UNUserNotificationCenter.currentNotificationCenter()
        center.requestAuthorizationWithOptions(
            UNAuthorizationOptionAlert or UNAuthorizationOptionSound or UNAuthorizationOptionBadge
        ) { granted, _ ->
            if (granted) {
                UIApplication.sharedApplication.registerForRemoteNotifications()
            }
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    override suspend fun scheduleNotification(
        prayerName: String,
        scheduledTime: Instant,
        title: String,
        message: String,
        audioFile: String
    ) {
        val content = UNMutableNotificationContent()
        content.setTitle(title)
        content.setBody(message)

        // Only play sound if audioFile is provided (adhan enabled)
        if (audioFile.isNotEmpty()) {
            content.setSound(UNNotificationSound.defaultSound())
        }

        val timeIntervalSince1970 = scheduledTime.toEpochMilliseconds() / 1000.0
        val nsDate = NSDate(timeIntervalSince1970)

        val calendar = NSCalendar.currentCalendar
        val unitFlags = NSCalendarUnitYear or
                NSCalendarUnitMonth or
                NSCalendarUnitDay or
                NSCalendarUnitHour or
                NSCalendarUnitMinute or
                NSCalendarUnitSecond

        val components = calendar.components(unitFlags, fromDate = nsDate)

        val trigger = UNCalendarNotificationTrigger.triggerWithDateMatchingComponents(
            components,
            repeats = false
        )

        val request = UNNotificationRequest.requestWithIdentifier(
            "prayer_$prayerName",
            content = content,
            trigger = trigger
        )

        UNUserNotificationCenter.currentNotificationCenter()
            .addNotificationRequest(request) { error ->
                if (error != null) {
                    println("Failed to schedule notification: ${error.localizedDescription}")
                } else {
                    println("Scheduled notification for $prayerName")
                }
            }
    }

    @OptIn(ExperimentalForeignApi::class)
    override suspend fun playAudio(audioFile: String) {
        try {
            // Look for the audio file in the app bundle (placed via composeResources/files/)
            val localPath = getBundledAudioPath() ?: run {
                println("Failed to find bundled audio file")
                return
            }

            val fileUrl = NSURL.fileURLWithPath(localPath)

            val audioSession = AVAudioSession.sharedInstance()
            audioSession.setCategory(AVAudioSessionCategoryPlayback, error = null)
            audioSession.setActive(true, error = null)

            audioPlayer?.stop()
            audioPlayer = AVAudioPlayer(contentsOfURL = fileUrl, error = null)
            audioPlayer?.volume = 1.0f
            audioPlayer?.prepareToPlay()
            val started = audioPlayer?.play() ?: false
            if (started) {
                println("Audio playback started from local bundle")
            } else {
                println("Audio playback failed to start")
            }
        } catch (e: Exception) {
            println("Failed to play audio: ${e.message}")
        }
    }

    /**
     * Finds the bundled azan audio file in the app bundle.
     * Compose Resources places files from composeResources/files/ into the bundle.
     */
    private fun getBundledAudioPath(): String? {
        // Compose Multiplatform bundles files from composeResources/files/ into the app bundle
        // Try the compose resources bundle path first
        val bundle = NSBundle.mainBundle

        // Try finding the file with common Compose Resources bundle paths
        val path = bundle.pathForResource("azan", ofType = "ogg")
            ?: bundle.pathForResource("files/azan", ofType = "ogg")
            ?: bundle.pathForResource("compose-resources/composeapp.composeapp.generated.resources/files/azan", ofType = "ogg")

        if (path != null) {
            println("Found bundled audio at: $path")
            return path
        }

        // Search all bundle resources for the file
        val resourcePath = bundle.resourcePath
        if (resourcePath != null) {
            println("Bundle resource path: $resourcePath")
        }

        println("Could not find azan.ogg in app bundle")
        return null
    }

    override suspend fun cancelNotification(notificationId: String) {
        UNUserNotificationCenter.currentNotificationCenter()
            .removePendingNotificationRequestsWithIdentifiers(listOf("prayer_$notificationId"))
    }

    override suspend fun cancelAllNotifications() {
        UNUserNotificationCenter.currentNotificationCenter().removeAllPendingNotificationRequests()
        stopAudio()
    }

    private fun stopAudio() {
        audioPlayer?.let {
            if (it.isPlaying()) {
                it.stop()
                println("Audio stopped")
            }
            audioPlayer = null
        }
    }
}