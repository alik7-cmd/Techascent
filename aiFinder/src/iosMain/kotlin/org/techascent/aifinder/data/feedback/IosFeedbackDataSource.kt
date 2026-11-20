package org.techascent.aifinder.data.feedback

import org.techascent.aifinder.data.firebase.firebaseWrite

actual fun createFeedbackRemote(): FeedbackRemoteDataSource = IosFeedbackDataSource()

private class IosFeedbackDataSource : FeedbackRemoteDataSource {
    override suspend fun submit(userId: String, reviewId: String, review: String) {
        firebaseWrite(feedbackPath(userId, reviewId), feedbackRecord(userId, review))
    }
}
