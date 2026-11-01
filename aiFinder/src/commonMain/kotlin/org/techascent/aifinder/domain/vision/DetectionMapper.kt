package org.techascent.aifinder.domain.vision

import org.techascent.aifinder.domain.model.DetectionStatus
import org.techascent.aifinder.domain.model.InventoryItem
import org.techascent.aifinder.domain.model.ItemDetection

private const val MIN_CONFIDENCE = 0.35f
private const val MAX_ITEMS = 12

// Scene/surface labels that describe the space rather than a storable item.
private val ignoredLabels = setOf(
    "room", "interior design", "floor", "flooring", "wall", "ceiling", "indoor", "structure",
    "wood", "hardwood", "pattern", "material", "sky", "event", "fun", "smile", "darkness",
    "light", "lighting", "home goods", "home good", "fashion good", "place",
    "person", "people", "adult", "selfie", "cool", "textile"
)

private val categoryKeywords = listOf(
    "Electronics" to listOf("phone", "computer", "laptop", "keyboard", "mouse", "cable", "monitor", "television", "tv", "camera", "remote", "headphone", "speaker", "charger", "adapter", "electronic", "gadget", "tablet", "screen"),
    "Tools" to listOf("tool", "hammer", "screwdriver", "wrench", "drill", "pliers", "tape", "scissors"),
    "Kitchen" to listOf("cup", "mug", "bottle", "plate", "bowl", "spoon", "fork", "knife", "pan", "pot", "kitchen", "glass", "tableware", "jar"),
    "Clothing" to listOf("shoe", "clothing", "shirt", "jacket", "hat", "bag", "sock", "jeans", "dress", "glove", "backpack"),
    "Documents" to listOf("book", "paper", "document", "notebook", "envelope", "folder", "pen", "pencil"),
    "Furniture" to listOf("chair", "table", "desk", "shelf", "cabinet", "drawer", "couch", "sofa", "bed", "furniture"),
    "Toys" to listOf("toy", "lego", "doll", "ball", "game", "puzzle"),
    "Storage" to listOf("box", "basket", "container", "bin", "crate", "carton")
)

private fun String.prettyLabel(): String =
    replace('_', ' ').trim().replaceFirstChar { it.uppercase() }

private fun categoryFor(label: String): String {
    val lower = label.lowercase()
    return categoryKeywords.firstOrNull { (_, words) -> words.any { lower.contains(it) } }?.first ?: "General"
}

/** Merges, filters and maps raw recognition results into the app's [ItemDetection] model. */
fun List<RecognizedObject>.toItemDetections(photoId: String): List<ItemDetection> =
    asSequence()
        .map { it.copy(label = it.label.prettyLabel()) }
        .filter { it.confidence >= MIN_CONFIDENCE && it.label.isNotBlank() }
        .filterNot { it.label.lowercase() in ignoredLabels }
        .groupBy { it.label.lowercase() }
        .map { (_, sameLabel) ->
            // Prefer results that carry a location in the photo, then highest confidence.
            sameLabel.maxWith(compareBy<RecognizedObject> { it.bounds != null }.thenBy { it.confidence })
        }
        .sortedWith(compareByDescending<RecognizedObject> { it.bounds != null }.thenByDescending { it.confidence })
        .take(MAX_ITEMS)
        .mapIndexed { index, result ->
            val slug = result.label.lowercase().replace(Regex("[^a-z0-9]+"), "-").trim('-')
            ItemDetection(
                photoId = photoId,
                item = InventoryItem(
                    id = "$photoId-$index-$slug",
                    name = result.label,
                    category = result.category?.takeIf { it.isNotBlank() } ?: categoryFor(result.label),
                    tags = listOf(result.label.lowercase())
                ),
                confidence = result.confidence,
                status = DetectionStatus.SUGGESTED,
                bounds = result.bounds
            )
        }
