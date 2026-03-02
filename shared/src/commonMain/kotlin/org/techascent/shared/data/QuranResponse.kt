package org.techascent.shared.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ---- Surah List Response ----
@Serializable
data class SurahListResponse(
    val code: Int,
    val status: String,
    val data: List<SurahInfo>
)

@Serializable
data class SurahInfo(
    val number: Int,
    val name: String,
    val englishName: String,
    val englishNameTranslation: String,
    val numberOfAyahs: Int,
    val revelationType: String
)

// ---- Surah Detail (with Arabic text + audio) ----
@Serializable
data class SurahDetailResponse(
    val code: Int,
    val status: String,
    val data: SurahDetailData
)

@Serializable
data class SurahDetailData(
    val number: Int,
    val name: String,
    val englishName: String,
    val englishNameTranslation: String,
    val revelationType: String,
    val numberOfAyahs: Int,
    val ayahs: List<AyahData>,
    val edition: EditionData? = null
)

@Serializable
data class AyahData(
    val number: Int,
    val text: String,
    val numberInSurah: Int,
    val juz: Int,
    val manzil: Int,
    val page: Int,
    val ruku: Int,
    val hizbQuarter: Int,
    val sajda: Boolean = false,
    val audio: String? = null,
    val audioSecondary: List<String>? = null
)

@Serializable
data class EditionData(
    val identifier: String? = null,
    val language: String? = null,
    val name: String? = null,
    val englishName: String? = null,
    val format: String? = null,
    val type: String? = null,
    val direction: String? = null
)

