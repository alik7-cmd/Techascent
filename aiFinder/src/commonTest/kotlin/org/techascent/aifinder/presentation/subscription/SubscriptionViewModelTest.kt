package org.techascent.aifinder.presentation.subscription

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
import org.techascent.aifinder.domain.subscription.ScanAccess
import org.techascent.aifinder.domain.subscription.ScanAccessRepository

@OptIn(ExperimentalCoroutinesApi::class)
class SubscriptionViewModelTest {
    @BeforeTest
    fun setUp() = Dispatchers.setMain(StandardTestDispatcher())

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun onlyUsersWithCreditOrMasterAccessContinue() = runTest {
        for (access in listOf(ScanAccess("user", 0), ScanAccess("user", 2), ScanAccess("master", 0, true))) {
            val viewModel = SubscriptionViewModel(FakeRepository(access))
            var allowed = 0
            viewModel.refresh { allowed++ }
            viewModel.refresh { allowed++ }
            runCurrent()
            assertEquals(if (access.canScan) 1 else 0, allowed)
            assertEquals(SubscriptionUiState.Ready(access), viewModel.state.value)
        }
    }

    @Test
    fun failureIsRetryableAndResetCancelsPendingNavigation() = runTest {
        val repository = FakeRepository(ScanAccess("user", 1))
        repository.fail = true
        val viewModel = SubscriptionViewModel(repository)
        viewModel.refresh()
        runCurrent()
        assertIs<SubscriptionUiState.Error>(viewModel.state.value)
        repository.fail = false
        var allowed = 0
        viewModel.refresh { allowed++ }
        viewModel.reset()
        runCurrent()
        assertEquals(0, allowed)
        viewModel.refresh { allowed++ }
        runCurrent()
        assertEquals(1, allowed)
    }

    private class FakeRepository(private val value: ScanAccess) : ScanAccessRepository {
        var fail = false
        override suspend fun refresh(): ScanAccess {
            check(!fail) { "Offline" }
            return value
        }
        override suspend fun consume(userId: String, scanId: String) = error("Not used")
    }
}
