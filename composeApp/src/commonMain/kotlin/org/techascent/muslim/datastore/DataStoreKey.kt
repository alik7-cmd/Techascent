@file:Suppress("MatchingDeclarationName")

package org.techascent.muslim.datastore

/**
 * Re-exports shared [org.techascent.shared.data.common.DataStoreKey] so
 * existing composeApp imports keep working without changes.
 * New code should import from [org.techascent.shared.data.common.DataStoreKey].
 */
typealias DataStoreKey = org.techascent.shared.data.common.DataStoreKey

/** UI-only content that lives in composeApp, not shared. */
object AppContent {
    val motivationHtml = """
    <h3>Why I Built This App</h3>
    <p>I created this app purely out of personal motivation and the hope of doing something meaningful. It wasn't made to earn money, and it remains completely free and free of ads. Every part of it was developed during my spare time alongside my regular work.</p>

    <h3>A Small Effort With a Bigger Purpose</h3>
    <p>My intention is simple: if even one person benefits from this app, I believe the reward from Allah will be greater than anything material. That belief alone has been my inspiration from start to finish.</p>

    <h3>Giving Back Through Technology</h3>
    <p>This is my small contribution—using the skills I have to build something that can bring ease, peace, or consistency to someone's daily routine.</p>
""".trimIndent()
}
