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
        ) { granted, error ->
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

        if (audioUrl.isNotEmpty()) {
            try {
                val soundUrl = NSURL(string = audioUrl)
                if (soundUrl != null) {
                    val sound = UNNotificationSound.soundNamed("custom_sound")
                    content.setSound(sound)
                }
            } catch (e: Exception) {
                println("Failed to set custom sound: ${e.message}")
            }
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
                }
            }
    }

    @OptIn(ExperimentalForeignApi::class)
    override suspend fun playAudio(audioUrl: String) {
        try {
            val url = NSURL(string = audioUrl)
            if (url == null) {
                println("Invalid audio URL")
                return
            }

            val audioSession = AVAudioSession.sharedInstance()
            audioSession.setCategory(AVAudioSessionCategoryPlayback, error = null)
            audioSession.setActive(true, error = null)

            audioPlayer = AVAudioPlayer(contentsOfURL = url, fileTypeHint = "mp3", error = null)
            audioPlayer?.volume = 1.0f
            audioPlayer?.play()
        } catch (e: Exception) {
            println("Failed to play audio: ${e.message}")
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