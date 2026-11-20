package org.techascent.aifinder.presentation.account

import org.techascent.aifinder.domain.auth.AuthProvider
import org.techascent.aifinder.domain.auth.AuthUser
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AccountProfileTest {
    @Test
    fun usesDisplayNameForInitialAndKeepsProvider() {
        val profile = AuthUser("uid", " ada lovelace ", "ada@example.com", null, AuthProvider.Google).toAccountProfile()
        assertEquals("ada lovelace", profile.name)
        assertEquals("ada@example.com", profile.email)
        assertEquals("A", profile.initial)
        assertEquals(AuthProvider.Google, profile.provider)
    }

    @Test
    fun fallsBackToEmailThenPlaceholder() {
        val emailOnly = AuthUser("uid", "  ", "zoe@example.com", null).toAccountProfile()
        assertNull(emailOnly.name)
        assertEquals("Z", emailOnly.initial)
        assertNull(emailOnly.provider)

        assertEquals("?", AuthUser("uid", null, null, null).toAccountProfile().initial)
    }
}
