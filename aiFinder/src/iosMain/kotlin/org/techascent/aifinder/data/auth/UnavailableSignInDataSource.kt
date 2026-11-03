package org.techascent.aifinder.data.auth

class UnavailableSignInDataSource : SignInRemoteDataSource {
    override suspend fun save(userId: String, data: SignedInData): Unit =
        error("Firebase sign-in metadata storage is not available on iOS yet.")
}
