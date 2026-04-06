package org.techascent.shared.data.mapper

import kotlinx.serialization.Serializable
import org.techascent.shared.data.Product


@Serializable
data class FlaggedIngredient(
    val name: String,
    val type: FlagType,
)

@Serializable
enum class FlagType {
    NON_HALAL,
    DOUBTFUL,
}

@Serializable
data class HalalResult(
    val status: HalalStatus,
    val flaggedIngredients: List<FlaggedIngredient> = emptyList(),
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
        // English
        "pork", "gelatin", "lard", "bacon", "ham", "wine", "rum", "beer", "alcohol",
        "ethanol", "pepsin", "rennet", "carmine", "cochineal", "shellac", "lard oil",
        "pig fat", "pork fat", "swine", "blood", "plasma",
        // Arabic
        "خنزير", "لحم خنزير", "شحم خنزير", "جيلاتين", "كحول", "نبيذ", "بيرة",
        "دم", "لاردو", "كارمين",
        // Turkish
        "domuz", "domuz yağı", "domuz jelatini", "jelatin", "alkol", "şarap", "bira",
        "kan", "domuz eti",
        // Malay / Indonesian
        "babi", "khinzir", "lemak babi", "gelatin babi", "arak", "alkohol", "bir",
        "darah",
        // French
        "porc", "gélatine", "saindoux", "jambon", "vin", "bière", "alcool",
        "graisse de porc", "sang",
        // German
        "schwein", "schweinefleisch", "schweinefett", "gelatine", "speck",
        "schinken", "wein", "bier", "alkohol", "blut",
        // Spanish
        "cerdo", "grasa de cerdo", "gelatina", "tocino", "jamón", "vino",
        "cerveza", "sangre",
        // Urdu
        "سور", "خنزیر", "شراب", "الکحل", "جلیٹن",
        // Bengali
        "শূকর", "শূকরের মাংস", "মদ", "জেলাটিন",
        // Hindi
        "सूअर", "शराब", "जिलेटिन",
    )

    private val doubtfulENumbers = listOf(
        "e120", "e153", "e441",
        "e470", "e470a", "e470b",
        "e471", "e472", "e472a", "e472b", "e472c", "e472d", "e472e", "e472f",
        "e473", "e474", "e475", "e476", "e477", "e478", "e479", "e479b",
        "e481", "e482", "e483",
        "e491", "e492", "e493", "e494", "e495",
        "e542", "e570", "e572", "e585",
        "e631", "e635", "e640",
        "e904", "e920", "e921", "e966",
    )

    private val halalKeywords = listOf("halal", "halal-certified", "halal-certification", "halal-by")

    private fun findNonHalalMatches(text: String): Set<String> {
        val lower = text.lowercase()
        return nonHalalIndicators.filter { lower.contains(it) }.toSet()
    }

    private fun findDoubtfulENumberMatches(text: String): Set<String> {
        val normalised = text.lowercase().replace("e-", "e").replace("e ", "e")
        return doubtfulENumbers.filter { normalised.contains(it) }.toSet()
    }

    fun assessHalalStatus(product: Product): HalalResult {
        val ingredients = product.ingredients_text?.lowercase() ?: ""

        val allTags = buildList {
            addAll(product.labelsTags.orEmpty())
            addAll(product.certificationTag.orEmpty())
            product.labels?.split(",")?.map { it.trim().lowercase() }?.let { addAll(it) }
        }.map { it.lowercase() }

        if (allTags.any { tag -> halalKeywords.any { keyword -> tag.contains(keyword) } }) {
            return HalalResult(status = HalalStatus.HALAL_CERTIFIED)
        }

        val flagged = mutableListOf<FlaggedIngredient>()

        val nonHalalMatches = findNonHalalMatches(ingredients)
        nonHalalMatches.forEach { flagged.add(FlaggedIngredient(name = it, type = FlagType.NON_HALAL)) }

        val doubtfulMatches = findDoubtfulENumberMatches(ingredients)
        doubtfulMatches.forEach { flagged.add(FlaggedIngredient(name = it, type = FlagType.DOUBTFUL)) }

        if (nonHalalMatches.isNotEmpty()) {
            return HalalResult(status = HalalStatus.NOT_HALAL, flaggedIngredients = flagged)
        }

        if (doubtfulMatches.isNotEmpty()) {
            return HalalResult(status = HalalStatus.HALAL_DOUBTFUL, flaggedIngredients = flagged)
        }

        if (ingredients.isNotBlank()) {
            return HalalResult(status = HalalStatus.HALAL_POSSIBLE)
        }

        return HalalResult(status = HalalStatus.UNKNOWN)
    }

    fun flagIngredients(ingredientText: String): List<FlaggedIngredient> {
        val flagged = mutableListOf<FlaggedIngredient>()
        val lower = ingredientText.lowercase()
        findNonHalalMatches(lower).forEach { flagged.add(FlaggedIngredient(name = it, type = FlagType.NON_HALAL)) }
        findDoubtfulENumberMatches(lower).forEach { flagged.add(FlaggedIngredient(name = it, type = FlagType.DOUBTFUL)) }
        return flagged
    }
}