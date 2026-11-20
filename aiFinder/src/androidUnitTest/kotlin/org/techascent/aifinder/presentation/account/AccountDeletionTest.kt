package org.techascent.aifinder.presentation.account

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.techascent.aifinder.domain.auth.AuthProvider
import org.techascent.aifinder.domain.auth.AuthRepository
import org.techascent.aifinder.domain.auth.AuthUser
import org.techascent.aifinder.domain.auth.usecase.*
import org.techascent.aifinder.domain.subscription.PurchaseGateway
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class AccountDeletionTest {
    @Test
    fun confirmationIsRequiredBusyRequestsAreIgnoredAndSuccessSignsOut() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val completion = CompletableDeferred<Unit>()
            var calls = 0
            var resets = 0
            val auth = fakeAuth()
            val vm = AccountViewModel(
                GetCurrentUserUseCase(auth),
                SignOutUseCase(auth) {},
                object : PurchaseGateway {
                    override fun prepare() = true
                    override suspend fun identify(userId: String) = Unit
                    override suspend fun reset() { resets++ }
                },
                DeleteProfileUseCase(auth, ProfileDeletionRemote { calls++; completion.await() }) {},
            )
            vm.load()
            vm.confirmDeletion()
            runCurrent()
            assertEquals(0, calls)
            vm.requestDeletion()
            assertTrue((vm.state.value as AccountUiState.Success).showDeleteConfirmation)
            vm.dismissDeletion()
            assertFalse((vm.state.value as AccountUiState.Success).showDeleteConfirmation)
            vm.requestDeletion()
            vm.confirmDeletion()
            runCurrent()
            assertTrue((vm.state.value as AccountUiState.Success).isDeleting)
            vm.confirmDeletion()
            vm.requestLogout()
            vm.dismissDeletion()
            vm.load()
            runCurrent()
            assertEquals(1, calls)
            val busy = vm.state.value as AccountUiState.Success
            assertTrue(busy.isDeleting)
            assertTrue(busy.showDeleteConfirmation)
            assertFalse(busy.showLogoutConfirmation)
            completion.complete(Unit)
            advanceUntilIdle()
            assertEquals(AccountUiState.SignedOut, vm.state.value)
            assertEquals(1, resets)
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun failedDeletionStaysVisibleAndRetryable() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val auth = fakeAuth()
            var shouldFail = true
            var calls = 0
            val vm = AccountViewModel(
                GetCurrentUserUseCase(auth), SignOutUseCase(auth) {},
                object : PurchaseGateway {
                    override fun prepare() = true
                    override suspend fun identify(userId: String) = Unit
                    override suspend fun reset() = Unit
                },
                DeleteProfileUseCase(auth, ProfileDeletionRemote {
                    calls++
                    if (shouldFail) error("Storage unavailable")
                }) {},
            )
            vm.load()
            vm.requestDeletion()
            vm.confirmDeletion()
            advanceUntilIdle()
            val failed = vm.state.value as AccountUiState.Success
            assertTrue(failed.showDeleteConfirmation)
            assertTrue(failed.deletionFailed)
            assertFalse(failed.isDeleting)
            assertNotNull(auth.currentUser)
            vm.load()
            vm.requestDeletion()
            assertEquals(failed, vm.state.value)
            assertEquals(1, calls)
            shouldFail = false
            vm.confirmDeletion()
            assertTrue((vm.state.value as AccountUiState.Success).showDeleteConfirmation)
            assertTrue((vm.state.value as AccountUiState.Success).isDeleting)
            advanceUntilIdle()
            assertEquals(2, calls)
            assertEquals(AccountUiState.SignedOut, vm.state.value)
            assertNull(auth.currentUser)
        } finally {
            Dispatchers.resetMain()
        }
    }

    private fun fakeAuth() = object : AuthRepository {
        override var currentUser: AuthUser? = AuthUser("alice", "Alice", null, null)
        override suspend fun signIn(provider: AuthProvider) = error("Not used")
        override suspend fun signOut() { currentUser = null }
    }
}
