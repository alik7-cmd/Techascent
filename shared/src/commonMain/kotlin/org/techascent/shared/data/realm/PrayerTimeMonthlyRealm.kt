package org.techascent.shared.data.realm

import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.annotations.PrimaryKey

// ---------- MAIN RESPONSE ----------

class PrayerTimeMonthlyRealm : RealmObject {
    @PrimaryKey
    var id: String = "monthly" // You can make it dynamic if needed
    var code: Int? = null
    var status: String? = null
    var prayerData: RealmList<PrayerDataRealm> = realmListOf()
}

// ---------- PRAYER DATA ----------

class PrayerDataRealm : RealmObject {
    @PrimaryKey
    var id: String = "" // use timestamp or date as unique key
    var date: DateRealm? = null
    var timings: TimingsRealm? = null
}

// ---------- DATE ----------

class DateRealm : RealmObject {
    var readable: String? = null
    var timestamp: String? = null
    var gregorian: GregorianRealm? = null
    var hijri: HijriRealm? = null
}

class GregorianRealm : RealmObject {
    var date: String? = null
    var day: String? = null
    var monthName: String? = null
    var year: String? = null
}

class HijriRealm : RealmObject {
    var date: String? = null
    var day: String? = null
    var monthName: String? = null
    var year: String? = null
}

class TimingsRealm : RealmObject {
    var fajr: String? = null
    var sunrise: String? = null
    var dhuhr: String? = null
    var asr: String? = null
    var maghrib: String? = null
    var isha: String? = null
}
