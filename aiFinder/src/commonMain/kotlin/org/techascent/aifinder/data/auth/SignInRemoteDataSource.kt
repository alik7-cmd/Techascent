package org.techascent.aifinder.data.auth

import org.techascent.aifinder.domain.auth.AuthProvider
import org.techascent.aifinder.util.Platform

data class SignedInData(
    val platform: Platform,
    val provider: AuthProvider,
    val deviceName: String,
    val signedInAt: Long
) {
    fun toMap(): Map<String, String> = mapOf(
        "platform" to platform.name,
        "provider" to provider.name,
        "deviceName" to deviceName,
        "signedInAt" to "$signedInAt"
     )
}

interface SignInRemoteDataSource {
    suspend fun save(userId: String, data: SignedInData)
}
