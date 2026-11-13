package org.techascent.aifinder.data.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.tasks.await
import org.techascent.aifinder.domain.auth.usecase.ProfileDeletionRemote

class FirebaseProfileDeletion : ProfileDeletionRemote {
    override suspend fun delete(userId: String) {
        check(FirebaseAuth.getInstance().currentUser?.uid == userId) { "The signed-in account changed." }
        val response = FirebaseFunctions.getInstance("us-central1")
            .getHttpsCallable("deleteProfile")
            .apply { setTimeout(540, java.util.concurrent.TimeUnit.SECONDS) }
            .call().await().data as? Map<*, *>
        check(response?.get("deleted") == true) { "The server did not confirm account deletion." }
    }
}
