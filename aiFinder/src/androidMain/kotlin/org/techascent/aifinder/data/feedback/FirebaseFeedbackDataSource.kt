package org.techascent.aifinder.data.feedback

import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await
import org.techascent.aifinder.BuildConfig

actual fun createFeedbackRemote(): FeedbackRemoteDataSource = FirebaseFeedbackDataSource()

private class FirebaseFeedbackDataSource : FeedbackRemoteDataSource {
    override suspend fun submit(userId: String, reviewId: String, review: String) {
        FirebaseDatabase.getInstance(BuildConfig.FIREBASE_DATABASE_URL)
            .getReference(feedbackPath(userId, reviewId))
            .setValue(feedbackRecord(userId, review)).await()
    }
}
