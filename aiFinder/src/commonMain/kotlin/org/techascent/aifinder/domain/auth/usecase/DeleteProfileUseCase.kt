package org.techascent.aifinder.domain.auth.usecase

import org.techascent.aifinder.domain.auth.AuthRepository

fun interface ProfileDeletionRemote {
    suspend fun delete(userId: String)
}

class DeleteProfileUseCase(
    private val auth: AuthRepository,
    private val remote: ProfileDeletionRemote,
    private val clearUserData: suspend (String) -> Unit,
) {
    private var deletedUserId: String? = null

    suspend operator fun invoke() {
        val userId = deletedUserId ?: checkNotNull(auth.currentUser?.id) { "Sign in before deleting your account." }
        // Clear this device before removing Auth; a failed cloud operation remains retryable.
        clearUserData(userId)
        if (deletedUserId != userId) {
            remote.delete(userId)
            deletedUserId = userId
        }
        clearUserData(userId)
        auth.signOut()
        deletedUserId = null
    }
}
