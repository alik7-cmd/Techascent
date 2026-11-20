package org.techascent.aifinder.domain.inventory

import org.techascent.aifinder.domain.location.pathOf
import org.techascent.aifinder.domain.model.LocatedItem
import org.techascent.aifinder.domain.model.StorageLocation

/** The user's locations and items joined together, as read from the device cache. */
data class InventorySnapshot(
    val locations: List<StorageLocation> = emptyList(),
    val items: List<LocatedItem> = emptyList(),
) {
    private val childrenByParent = locations.groupBy { it.parentId }

    /** Top-level locations (no parent, or a parent that no longer exists) with subtree counts. */
    val topLevelSpaces: List<StorageLocation>
        get() {
            val ids = locations.map { it.id }.toSet()
            return locations.filter { it.parentId == null || it.parentId !in ids }.map { it.withCounts() }
        }

    /** Every location with subtree counts, ordered by its full path. */
    val allSpaces: List<StorageLocation>
        get() = locations.sortedBy { locations.pathOf(it.id).lowercase() }.map { it.withCounts() }

    fun location(id: String): StorageLocation? = locations.firstOrNull { it.id == id }?.withCounts()

    fun pathOf(id: String): String = locations.pathOf(id)

    fun item(id: String): LocatedItem? = items.firstOrNull { it.item.id == id }

    /** Items stored in [locationId] or anywhere inside it. */
    fun itemsIn(locationId: String): List<LocatedItem> {
        val subtree = subtreeOf(locationId)
        return items.filter { it.location.id in subtree }
    }

    /** Items whose name, category, tags or location path contain every meaningful word of [query]. */
    fun search(query: String): List<LocatedItem> {
        val words = query.lowercase().split(Regex("[^\\p{L}\\p{N}]+"))
            .filter { it.isNotBlank() && it !in searchStopWords }
        if (words.isEmpty()) return items
        return items.mapNotNull { located ->
            val haystack = buildString {
                append(located.item.name).append(' ')
                append(located.item.category).append(' ')
                located.item.tags.forEach { append(it).append(' ') }
                append(located.path)
            }.lowercase()
            val tokens = haystack.split(Regex("[^\\p{L}\\p{N}]+")).filter(String::isNotBlank)
            var score = 0
            for (word in words) {
                val match = when {
                    word in haystack -> 3
                    tokens.any { canonicalSearchWord(it) == canonicalSearchWord(word) } -> 2
                    word.length >= 5 && word.all(Char::isLetter) &&
                        tokens.any { it.all(Char::isLetter) && oneEditApart(word, it) } -> 1
                    else -> return@mapNotNull null
                }
                score += match
            }
            located to score
        }.sortedByDescending { it.second }.map { it.first }
    }

    private fun StorageLocation.withCounts(): StorageLocation {
        val subtree = subtreeOf(id)
        return copy(
            spaceCount = subtree.size - 1,
            itemCount = items.count { it.location.id in subtree },
        )
    }

    private fun subtreeOf(id: String): Set<String> {
        val visited = mutableSetOf<String>()
        val pending = ArrayDeque(listOf(id))
        while (pending.isNotEmpty()) {
            val current = pending.removeFirst()
            if (visited.add(current)) childrenByParent[current].orEmpty().forEach { pending.addLast(it.id) }
        }
        return visited
    }
}

/** Joins items to their cached location; items whose location is missing are skipped. */
fun buildSnapshot(locations: List<StorageLocation>, items: List<StoredItem>): InventorySnapshot {
    val byId = locations.associateBy { it.id }
    return InventorySnapshot(
        locations = locations,
        items = items.mapNotNull { stored ->
            val location = byId[stored.locationId] ?: return@mapNotNull null
            LocatedItem(stored.item, location, locations.pathOf(location.id), stored.createdAt, stored.confirmedAt, stored.bounds)
        },
    )
}

// Lets natural questions like "Where is my HDMI cable?" match the item "HDMI cable".
private val searchStopWords = setOf(
    "where", "is", "are", "my", "the", "a", "an", "did", "i", "put", "it", "find", "in", "of",
)

private val searchAliases = listOf(
    setOf("charger", "chargers", "adapter", "adaptor", "adapters", "adaptors"),
    setOf("headphones", "headphone", "earphones", "earbuds", "headset"),
    setOf("sofa", "couch"),
    setOf("wardrobe", "closet"),
    setOf("usb", "usbc"),
    setOf("keys", "key"),
    setOf("cables", "cable", "cord", "cords"),
)

private fun canonicalSearchWord(word: String): String =
    searchAliases.firstOrNull { word in it }?.first() ?: word

/** At most one insertion, deletion, substitution or adjacent transposition; no fuzzy numeric IDs. */
internal fun oneEditApart(a: String, b: String): Boolean {
    if (kotlin.math.abs(a.length - b.length) > 1) return false
    if (a == b) return true
    var i = 0
    while (i < minOf(a.length, b.length) && a[i] == b[i]) i++
    if (i == minOf(a.length, b.length)) return true
    if (a.length == b.length) {
        return a.substring(i + 1) == b.substring(i + 1) ||
            (i + 1 < a.length && a[i] == b[i + 1] && a[i + 1] == b[i] &&
                a.substring(i + 2) == b.substring(i + 2))
    }
    return if (a.length > b.length) a.substring(i + 1) == b.substring(i)
    else a.substring(i) == b.substring(i + 1)
}
