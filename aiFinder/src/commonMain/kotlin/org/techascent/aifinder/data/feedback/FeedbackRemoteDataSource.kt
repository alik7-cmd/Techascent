package org.techascent.aifinder.data.feedback

interface FeedbackRemoteDataSource {
    suspend fun submit(userId: String, reviewId: String, review: String)
}

expect fun createFeedbackRemote(): FeedbackRemoteDataSource

internal fun feedbackPath(userId: String, reviewId: String): String {
    require(userId.isNotBlank() && userId.none { it in ".#$[]/" })
    require(Regex("[a-f0-9-]{36}").matches(reviewId))
    return "aifinder/appReviews/$userId/$reviewId"
}

internal fun feedbackRecord(userId: String, review: String): Map<String, Any> {
    require(userId.isNotBlank())
    require(review.isNotBlank() && review.length <= 2000)
    return mapOf(
        "userId" to userId,
        "review" to review,
        "createdAt" to mapOf(".sv" to "timestamp"),
    )
}
