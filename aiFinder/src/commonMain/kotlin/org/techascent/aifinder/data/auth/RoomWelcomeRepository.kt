package org.techascent.aifinder.data.auth

import org.techascent.aifinder.data.cache.WelcomePreference
import org.techascent.aifinder.data.cache.WelcomePreferenceDao
import org.techascent.aifinder.domain.auth.WelcomeRepository

class RoomWelcomeRepository(private val dao: WelcomePreferenceDao) : WelcomeRepository {
    override suspend fun hasSeenWelcome(): Boolean = dao.hasSeenWelcome() == true

    override suspend fun markWelcomeSeen() = dao.upsert(WelcomePreference())
}
