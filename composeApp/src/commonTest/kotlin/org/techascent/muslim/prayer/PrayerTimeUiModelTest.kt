package org.techascent.muslim.prayer

import apphub.composeapp.generated.resources.Res
import apphub.composeapp.generated.resources.text_dhuhr
import apphub.composeapp.generated.resources.text_fajr
import kotlinx.datetime.LocalDateTime
import org.techascent.muslim.common.toHourMinuteString
import org.techascent.muslim.prayer.uimodel.toDisplayString
import org.techascent.muslim.prayer.uimodel.toUiModel
import org.techascent.shared.data.dto.IftarTimeDto
import org.techascent.shared.data.dto.PrayerName
import org.techascent.shared.data.dto.PrayerTimeDto
import org.techascent.shared.data.dto.PrayerTimeInterval
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class PrayerTimeUiModelTest {

    private lateinit var samplePrayerTimeDto: PrayerTimeDto

    @BeforeTest
    fun setup() {
        samplePrayerTimeDto = PrayerTimeDto(
            intervals = listOf(
                PrayerTimeInterval(
                    name = PrayerName.FAJR,
                    startTime = LocalDateTime(2025, 7, 2, 4, 30),
                    endTime = LocalDateTime(2025, 7, 2, 5, 30)
                ),
                PrayerTimeInterval(
                    name = PrayerName.DUHR,
                    startTime = LocalDateTime(2025, 7, 2, 12, 0),
                    endTime = LocalDateTime(2025, 7, 2, 12, 30)
                )
            ),
            currentPrayer = PrayerTimeInterval(
                name = PrayerName.DUHR,
                startTime = LocalDateTime(2025, 7, 2, 12, 0),
                endTime = LocalDateTime(2025, 7, 2, 12, 30)
            ),
            iftarTime = IftarTimeDto(
                startTime = LocalDateTime(2025, 7, 2, 19, 45),
                endTime = LocalDateTime(2025, 7, 2, 3, 50)
            ),
            hijriDate = "1447-12-26",
            sunrise = LocalDateTime(2025, 7, 2, 5, 50),
            sunset = LocalDateTime(2025, 7, 2, 19, 40)
        )
    }

    @Test
    fun `toHourMinuteString should return correct string for 12-hour format`() {
        val expected = "4:30 AM"
        val actual = LocalDateTime(
            year = 2025,
            monthNumber = 7,
            dayOfMonth = 2,
            hour = 4,
            minute = 30
        ).toHourMinuteString(false)
        assertEquals(expected, actual)
    }

    @Test
    fun `toDisplayString should return correct string for respective salat time`() {

        val expected = Res.string.text_fajr
        val actual = PrayerName.FAJR.toDisplayString()
        assertEquals(expected, actual)
    }

    @Test
    fun `toUiModel should return correct mapping of prayer time`() {
        val uiModel = samplePrayerTimeDto.toUiModel()
        val fajrInterval = uiModel.intervals.first()
        assertEquals(expected = uiModel.intervals.size, actual = 2)
        assertEquals(expected = Res.string.text_fajr, actual = fajrInterval.name)
        assertEquals(
            expected = samplePrayerTimeDto.intervals.first().startTime.toHourMinuteString(false),
            actual = fajrInterval.displayableStartTime
        )

        val duhrInterval = uiModel.intervals[1]
        assertEquals(expected = Res.string.text_dhuhr, actual = duhrInterval.name)
        assertEquals(
            expected = samplePrayerTimeDto.intervals[1].startTime.toHourMinuteString(false),
            actual = duhrInterval.displayableStartTime
        )

        assertEquals(
            samplePrayerTimeDto.currentPrayer?.name?.toDisplayString(),
            Res.string.text_dhuhr
        )
        assertEquals(uiModel.sunrise, "5:50 AM")
        assertEquals(uiModel.sunset, "7:40 PM")
    }
}