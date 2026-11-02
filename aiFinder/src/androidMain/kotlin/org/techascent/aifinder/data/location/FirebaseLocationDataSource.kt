package org.techascent.aifinder.data.location

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import org.techascent.aifinder.BuildConfig

class FirebaseLocationDataSource(
    private val database: FirebaseDatabase = defaultDatabase(),
) : LocationRemoteDataSource {

    override fun observe(userId: String): Flow<List<Pair<String, LocationDto>>> = callbackFlow {
        val ref = database.getReference(RealtimeDatabasePaths.locations(userId))
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val nodes = snapshot.children.mapNotNull { child ->
                    val id = child.key ?: return@mapNotNull null
                    val dto = (child.value as? Map<*, *>)?.let(LocationDto::fromMap) ?: return@mapNotNull null
                    id to dto
                }
                trySend(nodes)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    override suspend fun create(userId: String, location: LocationDto): String {
        val ref = database.getReference(RealtimeDatabasePaths.locations(userId)).push()
        val id = checkNotNull(ref.key) { "Realtime Database didn't generate a key." }
        ref.setValue(location.toMap()).await()
        return id
    }

    private companion object {
        fun defaultDatabase(): FirebaseDatabase {
            val url = BuildConfig.FIREBASE_DATABASE_URL
            return if (url.isBlank()) FirebaseDatabase.getInstance() else FirebaseDatabase.getInstance(url)
        }
    }
}
