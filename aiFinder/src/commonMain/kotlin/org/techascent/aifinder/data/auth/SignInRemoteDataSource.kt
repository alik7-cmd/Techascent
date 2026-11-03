package org.techascent.aifinder.data.auth

import org.techascent.aifinder.domain.auth.AuthProvider
import org.techascent.aifinder.util.Platform

data class SignedInData(
    val platform: Platform,
    val provider: AuthProvider,
    val deviceName: String,
) {
    fun toMap(): Map<String, String> = mapOf(
        "platform" to platform.name,
        "provider" to provider.name,
        "deviceName" to deviceName,
     )
}

interface SignInRemoteDataSource {
    suspend fun save(userId: String, data: SignedInData)
}
