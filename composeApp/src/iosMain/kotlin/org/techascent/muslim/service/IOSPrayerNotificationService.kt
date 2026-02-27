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
import platform.Foundation.NSData
import platform.Foundation.NSDate
import platform.Foundation.NSFileManager
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSUserDomainMask
import platform.Foundation.NSURL
import platform.Foundation.dataWithContentsOfURL
import platform.Foundation.writeToFile
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
        audioUrl: String
    ) {
        val content = UNMutableNotificationContent()
        content.setTitle(title)
        content.setBody(message)
        content.setSound(UNNotificationSound.defaultSound())

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
    override suspend fun playAudio(audioUrl: String) {
        try {
            // Download to local cache first since AVAudioPlayer doesn't support remote URLs
            val localPath = downloadAndCacheAudio(audioUrl) ?: run {
                println("Failed to download audio for playback")
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
                println("Audio playback started")
            } else {
                println("Audio playback failed to start")
            }
        } catch (e: Exception) {
            println("Failed to play audio: ${e.message}")
        }
    }

    private fun downloadAndCacheAudio(audioUrl: String): String? {
        return try {
            val cacheDir = NSSearchPathForDirectoriesInDomains(
                NSCachesDirectory,
                NSUserDomainMask,
                true
            ).firstOrNull() as? String ?: return null

            val fileName = "prayer_audio_${audioUrl.hashCode()}.ogg"
            val localPath = "$cacheDir/$fileName"

            // Return cached file if exists
            if (NSFileManager.defaultManager.fileExistsAtPath(localPath)) {
                println("Using cached audio: $localPath")
                return localPath
            }

            val url = NSURL(string = audioUrl)
            val data = NSData.dataWithContentsOfURL(url)
            if (data != null) {
                data.writeToFile(localPath, atomically = true)
                println("Audio cached to: $localPath")
                localPath
            } else {
                println("Failed to download audio from: $audioUrl")
                null
            }
        } catch (e: Exception) {
            println("Error caching audio: ${e.message}")
            null
        }
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