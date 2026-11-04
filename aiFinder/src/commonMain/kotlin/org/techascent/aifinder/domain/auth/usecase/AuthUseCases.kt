package org.techascent.aifinder.domain.auth.usecase

import org.techascent.aifinder.domain.auth.AuthProvider
import org.techascent.aifinder.domain.auth.AuthRepository
import org.techascent.aifinder.domain.auth.AuthUser

class SignInUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(provider: AuthProvider): Result<AuthUser> = repository.signIn(provider)
}

class GetCurrentUserUseCase(private val repository: AuthRepository) {
    operator fun invoke(): AuthUser? = repository.currentUser
}

class SignOutUseCase(
    private val repository: AuthRepository,
    private val clearUserCache: suspend (String) -> Unit,
) {
    suspend operator fun invoke() {
        val userId = repository.currentUser?.id
        if (userId != null) clearUserCache(userId)
        repository.signOut()
    }
}
