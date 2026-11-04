package org.techascent.aifinder.presentation.login

import apphub.aifinder.generated.resources.Res
import apphub.aifinder.generated.resources.login_error_network
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.techascent.aifinder.domain.auth.AuthError
import org.techascent.aifinder.domain.auth.AuthException
import org.techascent.aifinder.domain.auth.AuthProvider
import org.techascent.aifinder.domain.auth.AuthRepository
import org.techascent.aifinder.domain.auth.AuthUser
import org.techascent.aifinder.domain.auth.usecase.GetCurrentUserUseCase
import org.techascent.aifinder.domain.auth.usecase.SignInUseCase
import org.techascent.aifinder.domain.subscription.PurchaseGateway
import org.techascent.aifinder.domain.subscription.ScanAccess
import org.techascent.aifinder.domain.subscription.ScanAccessRepository
import org.techascent.aifinder.domain.subscription.ScanPurchase

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {
    @BeforeTest
    fun setUp() = Dispatchers.setMain(StandardTestDispatcher())

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun successfulSignInTransitionsThroughLoadingAndHandlesNavigation() = runTest {
        val repository = FakeAuthRepository()
        val viewModel = newViewModel(repository)
        assertEquals(LoginUiState.Idle, viewModel.state.value)

        viewModel.onSignIn(AuthProvider.Apple)
        assertEquals(LoginUiState.Loading, viewModel.state.value)
        viewModel.onSignIn(AuthProvider.Google)
        viewModel.dismissError()
        viewModel.onSignedInHandled()
        assertEquals(LoginUiState.Loading, viewModel.state.value)
        runCurrent()

        assertIs<LoginUiState.SignedIn>(viewModel.state.value)
        assertEquals(1, repository.signInCalls)
        viewModel.onSignedInHandled()
        assertEquals(LoginUiState.Idle, viewModel.state.value)
    }

    @Test
    fun failedSignInShowsAnErrorAndCanBeDismissed() = runTest {
        val viewModel = newViewModel(FakeAuthRepository(Result.failure(AuthException(AuthError.Network))))
        viewModel.onSignIn(AuthProvider.Google)
        runCurrent()

        assertEquals(LoginUiState.Error(Res.string.login_error_network), viewModel.state.value)
        viewModel.onSignedInHandled()
        assertIs<LoginUiState.Error>(viewModel.state.value)
        viewModel.dismissError()
        assertEquals(LoginUiState.Idle, viewModel.state.value)
    }

    @Test
    fun cancelledSignInReturnsToIdleWithoutAnError() = runTest {
        val viewModel = newViewModel(FakeAuthRepository(Result.failure(AuthException(AuthError.Cancelled))))
        viewModel.onSignIn(AuthProvider.Google)
        runCurrent()

        assertEquals(LoginUiState.Idle, viewModel.state.value)
    }

    @Test
    fun creditAndPurchaseFailuresDoNotBlockSignIn() = runTest {
        val purchases = FakePurchases(fail = true)
        val viewModel = newViewModel(FakeAuthRepository(), object : ScanAccessRepository {
            override suspend fun refresh(): ScanAccess = error("Permission denied")
            override suspend fun consume(userId: String, scanId: String) = error("Not used")
            override suspend fun recordPurchase(purchase: ScanPurchase) = error("Not used")
        }, purchases)
        viewModel.onSignIn(AuthProvider.Google)
        runCurrent()
        assertIs<LoginUiState.SignedIn>(viewModel.state.value)
        assertEquals(1, purchases.identifyCalls)
    }

    @Test
    fun signInIdentifiesTheFirebaseUserWithThePurchaseStore() = runTest {
        val purchases = FakePurchases()
        val viewModel = newViewModel(FakeAuthRepository(), purchases = purchases)
        viewModel.onSignIn(AuthProvider.Google)
        runCurrent()
        assertEquals(listOf("user"), purchases.identified)
    }

    private class FakePurchases(private val fail: Boolean = false) : PurchaseGateway {
        val identified = mutableListOf<String>()
        var identifyCalls = 0
        override fun prepare() = true
        override suspend fun identify(userId: String) {
            identifyCalls++
            check(!fail) { "Store offline" }
            identified += userId
        }
        override suspend fun reset() = Unit
    }

    private fun newViewModel(
        repository: AuthRepository,
        access: ScanAccessRepository = object : ScanAccessRepository {
            override suspend fun refresh() = ScanAccess("user", 0)
            override suspend fun consume(userId: String, scanId: String) = error("Not used")
            override suspend fun recordPurchase(purchase: ScanPurchase) = error("Not used")
        },
        purchases: PurchaseGateway = FakePurchases(),
    ) = LoginViewModel(SignInUseCase(repository), GetCurrentUserUseCase(repository), access, purchases)

    private class FakeAuthRepository(
        private val result: Result<AuthUser> = Result.success(AuthUser("user", null, null, null))
    ) : AuthRepository {
        override val currentUser: AuthUser? = null
        var signInCalls = 0

        override suspend fun signIn(provider: AuthProvider): Result<AuthUser> {
            signInCalls++
            return result
        }

        override suspend fun signOut() = Unit
    }
}
