package org.techascent.aifinder.data.subscription

import kotlinx.coroutines.withTimeoutOrNull
import org.techascent.aifinder.config.MasterUsers
import org.techascent.aifinder.domain.auth.AuthRepository
import org.techascent.aifinder.domain.subscription.ScanAccess
import org.techascent.aifinder.domain.subscription.ScanAccessRepository

class ScanAccessRepositoryImpl(
    private val auth: AuthRepository,
    private val remote: ScanAccessRemoteDataSource,
    private val masterUserIds: Set<String> = MasterUsers.userIds,
) : ScanAccessRepository {
    override suspend fun refresh(): ScanAccess = withTimeoutOrNull(15_000L) {
        val userId = requireUser()
        if (userId in masterUserIds) return@withTimeoutOrNull ScanAccess(userId, 0, isMaster = true)
        remote.load(userId).also {
            check(requireUser() == userId) { "The signed-in user changed. Please try again." }
        }
    } ?: error("Checking scan credits timed out. Please try again.")

    override suspend fun consume(userId: String, scanId: String): ScanAccess = withTimeoutOrNull(15_000L) {
        check(requireUser() == userId) { "The signed-in user changed. Please scan again." }
        if (userId in masterUserIds) return@withTimeoutOrNull ScanAccess(userId, 0, isMaster = true)
        remote.consume(userId, scanId)
    } ?: error("Updating scan credits timed out. Please try again.")

    private fun requireUser(): String = checkNotNull(auth.currentUser) { "Please sign in to scan." }.id
}
