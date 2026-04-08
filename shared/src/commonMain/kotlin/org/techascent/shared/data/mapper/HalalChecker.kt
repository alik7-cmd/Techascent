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

        // ===== ENGLISH =====
        "pork", "porcine", "swine", "hog",
        "gelatin", "gelatine", "animal gelatin", "porcine gelatin",
        "lard", "lard oil", "pork fat", "pig fat", "animal fat",
        "bacon", "ham", "prosciutto", "salami", "pepperoni",
        "blood", "blood plasma", "plasma", "hemoglobin",
        "meat extract", "animal extract", "animal shortening",
        "rennet", "animal rennet", "pepsin", "trypsin",
        "enzymes", "animal enzymes",
        "collagen",
        "carmine", "cochineal", "carminic acid",
        "shellac", "resinous glaze",
        "wine", "beer", "rum", "vodka", "whiskey", "brandy",
        "alcohol", "ethanol", "ethyl alcohol", "spirit",
        "marshmallow", // often gelatin-based
        "mono- and diglycerides", "emulsifier",
        "glycerin", "glycerol", "fatty acids",
        "shortening",

        // ===== ARABIC =====
        "خنزير", "لحم خنزير", "دهن خنزير", "شحم خنزير",
        "جيلاتين", "جيلاتين حيواني",
        "كحول", "إيثانول", "نبيذ", "بيرة",
        "دم", "بلازما",
        "إنزيمات", "منفحة",

        // ===== TURKISH =====
        "domuz", "domuz eti", "domuz yağı",
        "jelatin", "hayvansal jelatin",
        "alkol", "etanol", "şarap", "bira",
        "kan", "plazma",
        "enzim", "rennet",

        // ===== MALAY / INDONESIAN =====
        "babi", "khinzir", "lemak babi",
        "gelatin", "gelatin babi",
        "alkohol", "etanol", "arak", "bir",
        "darah",
        "enzim", "rennet",

        // ===== FRENCH =====
        "porc", "graisse de porc", "saindoux",
        "gélatine", "gélatine animale",
        "alcool", "éthanol", "vin", "bière",
        "sang", "plasma",
        "enzymes", "présure",

        // ===== GERMAN =====
        "schwein", "schweinefleisch", "schweinefett",
        "gelatine", "tierische gelatine",
        "alkohol", "ethanol", "wein", "bier",
        "blut", "plasma",
        "enzyme", "lab",

        // ===== SPANISH =====
        "cerdo", "grasa de cerdo",
        "gelatina", "gelatina animal",
        "alcohol", "etanol", "vino", "cerveza",
        "sangre", "plasma",
        "enzimas", "cuajo",

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

        // ===== NORWEGIAN (BOKMÅL + COMMON LABEL TERMS) =====
        "svin", "svinekjøtt", "svinefett", "svineflesk",
        "gris", "grisekjøtt", "grisefett",
        "bacon", "skinke", "spekeskinke", "salami", "pepperoni",
        "ister", "smult", // lard equivalents
        "animalsk fett", "animalsk olje", "animalsk forkortning",
        "gelatin", "gelatine", "animalsk gelatin", "svinegelatin",
        "kollagen",
        "blod", "blodplasma", "plasma",
        "enzym", "enzymer", "animalske enzymer",
        "løpe", "animalsk løpe", // rennet
        "pepsin", "trypsin",
        "karmin", "kochenille", "karminsyrer",
        "skjellakk", "shellac",
        "alkohol", "etanol", "etylalkohol", "sprit",
        "vin", "øl", "brennevin", "rom", "vodka", "whisky",
        "emulgator", "mono- og diglyserider",
        "glyserol", "glyserin",
        "fettsyrer",
        "smaksstoff", "aroma", "naturlig aroma", // often doubtful
        "stabilisator", "fortykningsmiddel",
        "animalsk protein", "animalsk ekstrakt",
        "kjøttekstrakt",
        "myseprotein", "kasein", // dairy but sometimes mixed processing
        "marshmallow", // often gelatin-based

        // ===== GENERIC / SCIENTIFIC FLAGS =====
        "e120", "e441", "e904",
        "e471", "e472", "e473", "e474", "e475", "e476",
        "e477", "e481", "e482", "e491", "e492",
        "e493", "e494", "e495",
        "e542", "e570", "e572",
        "e631", "e635",
        "e920"
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