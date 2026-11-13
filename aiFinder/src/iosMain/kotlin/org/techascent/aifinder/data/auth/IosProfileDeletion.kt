package org.techascent.aifinder.data.auth

import org.techascent.aifinder.data.firebase.firebaseCall
import org.techascent.aifinder.domain.auth.usecase.ProfileDeletionRemote

class IosProfileDeletion : ProfileDeletionRemote {
    override suspend fun delete(userId: String) {
        firebaseCall<Unit> { bridge, done ->
            bridge.deleteProfile(userId) { error -> done(Unit, error) }
        }
    }
}
