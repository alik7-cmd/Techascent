package org.techascent.aifinder.presentation.account

import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.techascent.aifinder.data.feedback.*
import org.techascent.aifinder.domain.auth.*
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class FeedbackViewModelTest {
    private class Auth : AuthRepository {
        override var currentUser: AuthUser? = AuthUser("user", null, null, null)
        override suspend fun signIn(provider: AuthProvider): Result<AuthUser> = error("Unused")
        override suspend fun signOut() { currentUser = null }
    }
    private class Remote : FeedbackRemoteDataSource {
        val writes = mutableListOf<Triple<String, String, String>>()
        var fail = false
        var gate: CompletableDeferred<Unit>? = null
        override suspend fun submit(userId: String, reviewId: String, review: String) {
            writes += Triple(userId, reviewId, review)
            gate?.await()
            check(!fail) { "Offline" }
        }
    }
    @BeforeTest fun setup() = Dispatchers.setMain(UnconfinedTestDispatcher())
    @AfterTest fun cleanup() = Dispatchers.resetMain()

    @Test fun whitespaceAndOverlongMessagesCannotSubmit() {
        val remote = Remote()
        val vm = FeedbackViewModel(Auth(), remote)
        vm.edit("  \n"); vm.send()
        vm.edit("a".repeat(2001)); vm.send()
        assertTrue(remote.writes.isEmpty())
        assertFalse(vm.state.value.canSend)
    }

    @Test fun submissionUsesAccountIdTrimsReviewAndWaitsForAcknowledgement() {
        val remote = Remote().apply { gate = CompletableDeferred() }
        val vm = FeedbackViewModel(Auth(), remote)
        vm.edit("  Better search please  ")
        vm.send(); vm.send()
        assertEquals(1, remote.writes.size)
        assertTrue(vm.state.value.busy)
        assertFalse(vm.state.value.sent)
        assertEquals("user", remote.writes.single().first)
        assertEquals("Better search please", remote.writes.single().third)
        remote.gate!!.complete(Unit)
        assertTrue(vm.state.value.sent)
        assertEquals("", vm.state.value.text)
        vm.send()
        assertEquals(1, remote.writes.size)
    }

    @Test fun retryPreservesDraftAndIdInsteadOfCreatingDuplicateReview() {
        val remote = Remote().apply { fail = true }
        val vm = FeedbackViewModel(Auth(), remote)
        vm.edit("Make places clearer")
        vm.send()
        assertTrue(vm.state.value.failed)
        assertEquals("Make places clearer", vm.state.value.text)
        remote.fail = false
        vm.send()
        assertEquals(remote.writes[0].second, remote.writes[1].second)
        assertTrue(vm.state.value.sent)
    }

    @Test fun signedOutSubmissionFailsWithoutWriting() {
        val remote = Remote()
        val vm = FeedbackViewModel(Auth().apply { currentUser = null }, remote)
        vm.edit("Feedback")
        vm.send()
        assertTrue(vm.state.value.failed)
        assertTrue(remote.writes.isEmpty())
    }

    @Test fun recordIsSeparateFromUsersAndUsesServerTimestamp() {
        val id = "12345678-1234-1234-1234-123456789012"
        assertEquals("aifinder/appReviews/user/$id", feedbackPath("user", id))
        val record = feedbackRecord("user", "Helpful app")
        assertEquals(setOf("userId", "review", "createdAt"), record.keys)
        assertEquals(mapOf(".sv" to "timestamp"), record["createdAt"])
        assertFailsWith<IllegalArgumentException> { feedbackPath("../other", id) }
        assertFailsWith<IllegalArgumentException> { feedbackRecord("user", "") }
    }
}
