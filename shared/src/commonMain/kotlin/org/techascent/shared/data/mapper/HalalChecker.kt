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

    // ── Text Sanitisations ────────────────────────────────────────────────────
    // Remove these phrases before scanning to prevent false positives.
    // "spirit vinegar" is produced from fermented grain but the alcohol is
    // fully consumed during production — accepted as halal by most scholars.
    private val textSanitisations = listOf(
        "spirit vinegar",
        "cider vinegar",
        "apple cider vinegar",
        "vinaigre d'alcool",   // French: spirit vinegar
        "eddiksprit",          // Norwegian: spirit vinegar
    )

    // ── Word-Boundary Terms ───────────────────────────────────────────────────
    // Short or ambiguous Latin-script terms that are substrings of benign words.
    // Matched with regex \b word boundaries instead of plain contains().
    // Examples: "ham" in "hamburger", "vin" in "vinegar", "kan" in "kaneel".
    private val wordBoundaryTerms = setOf(
        "ham",    // "hamburger", "graham"
        "hog",
        "rum",    // "rumen" etc.
        "wine",
        "beer",
        "blood",  // "blood orange"
        "plasma", // scientific usage
        "vin",    // French/Norwegian "wine" vs "vinegar", "vitamin"
        "sang",   // French "blood" vs "sangria" (wine-based, but flag via "wine")
        "bier",   // German "beer"
        "blut",   // German "blood"
        "wein",   // German "wine"
        "rom",    // Norwegian "rum" vs "rom" (room)
        "kan",    // Turkish "blood" vs "kaneel" (cinnamon), "kanten" (agar)
        "bir",    // Malay "beer" vs many unrelated words
        "lab",    // German "rennet" (very short)
    )

    // ── Confirmed Non-Halal Indicators ────────────────────────────────────────
    // Only terms where the ingredient is definitively haram regardless of source.
    // Removed: bare "enzymes", "collagen", "emulsifier", "glycerin/ol",
    //          "fatty acids", "shortening", "spirit", "mono- and diglycerides",
    //          bare "rennet" — these are moved to doubtfulIngredientIndicators.
    // Removed: all E-numbers — handled exclusively by doubtfulENumbers below.
    private val nonHalalIndicators = listOf(

        // ===== ENGLISH =====
        "pork", "porcine", "swine", "hog",
        "gelatin", "gelatine", "animal gelatin", "porcine gelatin",
        "lard", "lard oil", "pork fat", "pig fat", "animal fat",
        "bacon", "ham", "prosciutto", "salami", "pepperoni",
        "blood", "blood plasma", "plasma", "hemoglobin",
        "meat extract", "animal extract", "animal shortening",
        "animal rennet", "pepsin", "trypsin",
        "animal enzymes",
        "carmine", "cochineal", "carminic acid",
        "shellac", "resinous glaze",
        "wine", "beer", "rum", "vodka", "whiskey", "brandy",
        "alcohol", "ethanol", "ethyl alcohol",
        "marshmallow",

        // ===== ARABIC =====
        "خنزير", "لحم خنزير", "دهن خنزير", "شحم خنزير",
        "جيلاتين", "جيلاتين حيواني",
        "كحول", "إيثانول", "نبيذ", "بيرة",
        "دم", "بلازما",
        "إنزيمات حيوانية", "منفحة حيوانية",

        // ===== TURKISH =====
        "domuz", "domuz eti", "domuz yağı",
        "jelatin", "hayvansal jelatin",
        "alkol", "etanol", "şarap", "bira",
        "kan", "plazma",

        // ===== MALAY / INDONESIAN =====
        "babi", "khinzir", "lemak babi",
        "gelatin babi",
        "alkohol", "etanol", "arak", "bir",
        "darah",

        // ===== FRENCH =====
        "porc", "graisse de porc", "saindoux",
        "gélatine", "gélatine animale",
        "alcool", "éthanol", "vin", "bière",
        "sang", "plasma",

        // ===== GERMAN =====
        "schwein", "schweinefleisch", "schweinefett",
        "gelatine", "tierische gelatine",
        "alkohol", "ethanol", "wein", "bier",
        "blut",

        // ===== SPANISH =====
        "cerdo", "grasa de cerdo",
        "gelatina", "gelatina animal",
        "alcohol", "etanol", "vino", "cerveza",
        "sangre", "plasma",

        // ===== URDU =====
        "سور", "خنزیر", "سور کا گوشت",
        "جیلیٹن", "جلیٹن",
        "شراب", "الکحل",
        "خون",

        // ===== BENGALI =====
        "শূকর", "শূকরের মাংস", "শূকরের চর্বি",
        "জেলাটিন", "প্রাণিজ জেলাটিন",
        "অ্যালকোহল", "ইথানল", "মদ",
        "রক্ত",

        // ===== HINDI =====
        "सूअर", "सूअर का मांस",
        "जिलेटिन", "पशु जिलेटिन",
        "अल्कोहल", "एथेनॉल", "शराब",
        "खून",

        // ===== NORWEGIAN =====
        "svin", "svinekjøtt", "svinefett", "svineflesk",
        "gris", "grisekjøtt", "grisefett",
        "bacon", "skinke", "spekeskinke", "salami", "pepperoni",
        "ister", "smult",
        "animalsk fett", "animalsk olje", "animalsk forkortning",
        "animalsk gelatin", "svinegelatin",
        "blod", "blodplasma",
        "animalske enzymer",
        "animalsk løpe",
        "pepsin", "trypsin",
        "karmin", "kochenille", "karminsyrer",
        "skjellakk", "shellac",
        "alkohol", "etanol", "etylalkohol", "sprit",
        "vin", "øl", "brennevin", "rom", "vodka", "whisky",
        "animalsk protein", "animalsk ekstrakt",
        "kjøttekstrakt",
        "marshmallow",
    )

    // ── Doubtful Ingredient Indicators ───────────────────────────────────────
    // These CAN be haram but are also commonly halal depending on their source
    // (e.g. plant-derived glycerin, microbial enzymes, vegetable shortening).
    // Shown as a warning — not an outright rejection.
    private val doubtfulIngredientIndicators = listOf(
        // English – source-dependent
        "rennet",                    // animal or microbial; animal rennet is caught above
        "enzymes",                   // microbial/fungal enzymes are halal
        "collagen",                  // can be marine-derived (fish collagen)
        "emulsifier",                // e.g. sunflower lecithin is halal
        "glycerin", "glycerol",      // vegetable glycerin is widely accepted
        "fatty acids",               // often palm/plant-derived
        "shortening",                // vegetable shortening is halal
        "spirit",                    // catch residual alcohol refs after whitelist removal
        "mono- and diglycerides",    // can be plant-based
        "natural flavour", "natural flavor", "natural flavouring",
        // French
        "présure", "enzymes",
        // German
        "enzyme", "lab",
        // Spanish
        "enzimas", "cuajo",
        // Turkish
        "enzim",
        // Malay / Indonesian
        "gelatin",                   // bare gelatin without qualifier (could be fish)
        // Norwegian
        "enzym", "enzymer",
        "løpe",                      // rennet — can be microbial
        "emulgator", "mono- og diglyserider",
        "glyserol", "glyserin", "fettsyrer",
        "smaksstoff", "naturlig aroma",
        "myseprotein", "kasein",
        "stabilisator",
    ).distinct()                     // deduplicate cross-language entries

    // ── Doubtful E-Numbers ────────────────────────────────────────────────────
    // E-numbers whose origin can be animal or plant depending on manufacturer.
    // Previously some were duplicated in nonHalalIndicators — removed from there,
    // classified exclusively here as DOUBTFUL.
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

    // ── Private Helpers ───────────────────────────────────────────────────────

    /** Remove whitelisted phrases (e.g. "spirit vinegar") before scanning. */
    private fun sanitise(text: String): String {
        var result = text.lowercase()
        textSanitisations.forEach { phrase -> result = result.replace(phrase, " ") }
        return result
    }

    /**
     * Match an indicator against text. Terms in [wordBoundaryTerms] use regex
     * \b word-boundaries to avoid substring false positives ("ham" ≠ "hamburger",
     * "vin" ≠ "vinegar"). Non-Latin script terms use plain contains() since \b
     * is ASCII-only and those scripts are naturally space-separated.
     */
    private fun matchesIndicator(text: String, indicator: String): Boolean {
        return if (indicator in wordBoundaryTerms) {
            Regex("\\b${Regex.escape(indicator)}\\b").containsMatchIn(text)
        } else {
            text.contains(indicator)
        }
    }

    private fun findNonHalalMatches(sanitisedText: String): Set<String> =
        nonHalalIndicators.filter { matchesIndicator(sanitisedText, it) }.toSet()

    private fun findDoubtfulIngredientMatches(sanitisedText: String): Set<String> =
        doubtfulIngredientIndicators.filter { sanitisedText.contains(it) }.toSet()

    private fun findDoubtfulENumberMatches(sanitisedText: String): Set<String> {
        val normalised = sanitisedText.replace("e-", "e").replace("e ", "e")
        return doubtfulENumbers.filter { normalised.contains(it) }.toSet()
    }

    /**
     * Single source of truth for building the flagged-ingredients list.
     * Used by both [assessHalalStatus] and [flagIngredients].
     */
    private fun buildFlaggedList(ingredientsText: String): List<FlaggedIngredient> {
        val sanitised = sanitise(ingredientsText)
        val flagged = mutableListOf<FlaggedIngredient>()
        findNonHalalMatches(sanitised).forEach {
            flagged.add(FlaggedIngredient(name = it, type = FlagType.NON_HALAL))
        }
        (findDoubtfulIngredientMatches(sanitised) + findDoubtfulENumberMatches(sanitised)).forEach {
            flagged.add(FlaggedIngredient(name = it, type = FlagType.DOUBTFUL))
        }
        return flagged
    }

    // ── Public API ────────────────────────────────────────────────────────────

    fun assessHalalStatus(product: Product): HalalResult {
        val ingredients = product.ingredients_text ?: ""

        val allTags = buildList {
            addAll(product.labelsTags.orEmpty())
            addAll(product.certificationTag.orEmpty())
            product.labels?.split(",")?.map { it.trim().lowercase() }?.let { addAll(it) }
        }.map { it.lowercase() }

        val isCertified = allTags.any { tag -> halalKeywords.any { keyword -> tag.contains(keyword) } }

        // Always scan ingredients even when a halal tag is present — tags on
        // OpenFoodFacts are user-submitted and can be wrong. The UI uses
        // flaggedIngredients to show a disclaimer if anything suspicious is found.
        val flagged = buildFlaggedList(ingredients)

        if (isCertified) {
            return HalalResult(status = HalalStatus.HALAL_CERTIFIED, flaggedIngredients = flagged)
        }

        val hasNonHalal = flagged.any { it.type == FlagType.NON_HALAL }
        val hasDoubtful = flagged.any { it.type == FlagType.DOUBTFUL }

        return when {
            hasNonHalal          -> HalalResult(status = HalalStatus.NOT_HALAL, flaggedIngredients = flagged)
            hasDoubtful          -> HalalResult(status = HalalStatus.HALAL_DOUBTFUL, flaggedIngredients = flagged)
            ingredients.isNotBlank() -> HalalResult(status = HalalStatus.HALAL_POSSIBLE)
            else                 -> HalalResult(status = HalalStatus.UNKNOWN)
        }
    }

    /** Re-compute flagged ingredients from raw text (used for history items). */
    fun flagIngredients(ingredientText: String): List<FlaggedIngredient> =
        buildFlaggedList(ingredientText)
}