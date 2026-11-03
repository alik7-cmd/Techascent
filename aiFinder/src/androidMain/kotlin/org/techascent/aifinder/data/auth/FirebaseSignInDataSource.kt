package org.techascent.aifinder.data.auth

import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await
import org.techascent.aifinder.BuildConfig
import org.techascent.aifinder.data.location.RealtimeDatabasePaths

class FirebaseSignInDataSource(
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance(BuildConfig.FIREBASE_DATABASE_URL),
) : SignInRemoteDataSource {
    override suspend fun save(userId: String, data: SignedInData) {
        database.getReference(RealtimeDatabasePaths.signedIn(userId))
            .updateChildren(data.toMap()).await()
    }
}
