package org.techascent.shared.data.mapper

import kotlinx.serialization.Serializable
import org.techascent.shared.data.Product


@Serializable
data class HalalResult(
    val status: HalalStatus,
)

@Serializable
enum class HalalStatus {
    HALAL_CERTIFIED,
    HALAL_POSSIBLE,
    HALAL_DOUBTFUL,
    NOT_HALAL,
    UNKNOWN
}

object HalalChecker {

    private val nonHalalIndicators = listOf(
        "pork", "gelatin", "lard", "bacon", "ham", "wine", "rum", "beer", "alcohol"
    )

    private val doubtfulENumbers = listOf(
        "e120", "e441", "e471", "e472", "e473", "e474", "e475", "e476", "e542", "e904"
    )

    private val halalKeywords = listOf("halal", "halal-certified", "halal-certification", "halal-by")

    fun assessHalalStatus(product: Product): HalalResult {

        val ingredients = product.ingredients_text?.lowercase() ?: ""

        // Safely collect tags as lowercase strings
        val allTags = buildList {
            addAll(product.labelsTags.orEmpty())
            addAll(product.certificationTag.orEmpty())
            product.labels?.split(",")?.map { it.trim().lowercase() }?.let { addAll(it) }
        }.map { it.lowercase() }

        // ✅ Certified or labeled as halal
        if (allTags.any { tag -> halalKeywords.any { keyword -> tag.contains(keyword) } }) {
            return HalalResult(
                HalalStatus.HALAL_CERTIFIED,
                /*"Certified or labeled as halal."*/
            )
        }

        // ❌ Explicitly haram
        if (nonHalalIndicators.any { ingredients.contains(it) }) {
            return HalalResult(
                HalalStatus.NOT_HALAL,
               /* "Contains non-halal ingredients (e.g. pork, alcohol)."*/
            )
        }

        // ⚠️ Doubtful additives
        if (doubtfulENumbers.any { ingredients.contains(it) }) {
            return HalalResult(
                HalalStatus.HALAL_DOUBTFUL,
               /* "Contains additives that may come from animal sources (e.g. E471)."*/
            )
        }

        // ❓ Probably halal
        if (ingredients.isNotBlank()) {
            return HalalResult(
                HalalStatus.HALAL_POSSIBLE,
                /*"No haram ingredients detected, but not certified halal."*/
            )
        }

        // 🤷 Not enough info
        return HalalResult(
            HalalStatus.UNKNOWN,
            /*"Insufficient data to determine halal status."*/
        )
    }
}