package org.techascent.aifinder.data.location

import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await
import org.techascent.aifinder.BuildConfig

class FirebaseLocationDataSource(
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance(BuildConfig.FIREBASE_DATABASE_URL),
) : LocationRemoteDataSource {

    override suspend fun fetchAll(userId: String): List<Pair<String, LocationDto>> =
        database.getReference(RealtimeDatabasePaths.locations(userId)).get().await()
            .children.mapNotNull { child ->
                val id = child.key ?: return@mapNotNull null
                val dto = (child.value as? Map<*, *>)?.let(LocationDto::fromMap) ?: return@mapNotNull null
                id to dto
            }

    override suspend fun create(userId: String, location: LocationDto): String {
        val ref = database.getReference(RealtimeDatabasePaths.locations(userId)).push()
        val id = checkNotNull(ref.key) { "Realtime Database didn't generate a key." }
        ref.setValue(location.toMap()).await()
        return id
    }
}
