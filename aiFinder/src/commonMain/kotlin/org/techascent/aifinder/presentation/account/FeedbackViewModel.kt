package org.techascent.aifinder.presentation.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.techascent.aifinder.data.feedback.FeedbackRemoteDataSource
import org.techascent.aifinder.domain.auth.AuthRepository
import org.techascent.aifinder.util.logDebug
import kotlin.uuid.Uuid

data class FeedbackState(
    val text: String = "",
    val busy: Boolean = false,
    val sent: Boolean = false,
    val failed: Boolean = false,
) {
    val canSend: Boolean get() = text.trim().isNotEmpty() && text.length <= FEEDBACK_MAX_LENGTH && !busy && !sent
}

const val FEEDBACK_MAX_LENGTH = 2000

@OptIn(kotlin.uuid.ExperimentalUuidApi::class)
class FeedbackViewModel(
    private val auth: AuthRepository,
    private val remote: FeedbackRemoteDataSource,
) : ViewModel() {
    private val _state = MutableStateFlow(FeedbackState())
    val state = _state.asStateFlow()
    private var reviewId = Uuid.random().toString()

    fun edit(text: String) {
        if (_state.value.busy || _state.value.sent) return
        if (_state.value.failed && text != _state.value.text) reviewId = Uuid.random().toString()
        _state.update { it.copy(text = text, failed = false) }
    }

    fun send() {
        val draft = _state.value
        if (!draft.canSend) return
        _state.update { it.copy(busy = true, failed = false) }
        viewModelScope.launch {
            try {
                val userId = checkNotNull(auth.currentUser?.id) { "Sign in before sending feedback." }
                remote.submit(userId, reviewId, draft.text.trim())
                _state.update { it.copy(busy = false, sent = true, text = "") }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                logDebug("Feedback", "Submission failed: ${e.message}")
                _state.update { it.copy(busy = false, failed = true) }
            }
        }
    }
}
