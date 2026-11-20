package org.techascent.aifinder.domain.auth

interface WelcomeRepository {
    suspend fun hasSeenWelcome(): Boolean
    suspend fun markWelcomeSeen()
}

enum class WelcomeDestination { Welcome, Login, Home }

class ResolveWelcomeDestinationUseCase(
    private val auth: AuthRepository,
    private val welcome: WelcomeRepository,
) {
    suspend operator fun invoke(): WelcomeDestination {
        if (auth.currentUser != null) return WelcomeDestination.Home
        if (welcome.hasSeenWelcome()) return WelcomeDestination.Login
        welcome.markWelcomeSeen()
        return WelcomeDestination.Welcome
    }
}
