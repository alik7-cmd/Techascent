package org.techascent.muslim.prayer.location

import org.techascent.shared.data.common.AddressInfo

/**
 * Result of an [AddressResolver.resolve] call.
 *
 * Using a sealed class instead of nullable/boolean pairs makes the three
 * distinct outcomes explicit and forces callers to handle each case.
 */
sealed class AddressResolutionResult {

    /**
     * Address resolved successfully from the geocoder or from a proximity
     * shortcut (user hasn't moved).
     *
     * @param address       The resolved address.
     * @param locationChanged `true` when the user has moved to a meaningfully
     *   different prayer-time area — the caller should invalidate the prayer
     *   cache in this case.
     */
    data class Success(
        val address: AddressInfo,
        val locationChanged: Boolean,
    ) : AddressResolutionResult()

    /**
     * The geocoder failed (e.g. offline), but a previously persisted address
     * was available. Prayer times can still be shown; the user should not be
     * shown an error.
     */
    data class Fallback(val cachedAddress: AddressInfo) : AddressResolutionResult()

    /**
     * Neither the geocoder nor any cache could produce an address. The caller
     * should emit an error state.
     */
    data object Unavailable : AddressResolutionResult()
}

/**
 * Resolves a GPS coordinate to a human-readable [AddressInfo].
 *
 * Encapsulates:
 * - In-memory address caching
 * - DataStore address persistence
 * - Proximity shortcut to skip the geocoder when the user hasn't moved
 * - Area-change detection to trigger prayer-cache invalidation
 *
 * Testable: inject a fake [AddressResolver] in unit tests to avoid geocoder
 * and DataStore dependencies.
 */
interface AddressResolver {

    /** Returns the last persisted address without calling the geocoder. */
    suspend fun getCachedAddress(): AddressInfo?

    /**
     * Resolves [latitude]/[longitude] to an [AddressResolutionResult].
     *
     * - If the coordinate is within ~1 km of the cached address, returns
     *   [AddressResolutionResult.Success] with `locationChanged = false`
     *   immediately — no geocoder call.
     * - Otherwise calls the platform geocoder and compares the result with
     *   the cached area.
     */
    suspend fun resolve(latitude: Double, longitude: Double): AddressResolutionResult

    /** Clears in-memory and persisted address state. */
    suspend fun clearCache()
}

