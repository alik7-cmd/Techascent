package org.techascent.aifinder.domain.vision

/** Instructions sent to Vertex AI Gemini; the JSON shape is enforced by the request's response schema. */
object ItemDetectionPrompt {
    val categories = listOf(
        "Electronics", "Tools", "Kitchen", "Clothing", "Documents", "Toys",
        "Storage", "Accessories", "Medicine", "Sports", "General"
    )

    val system: String = """
        You are the vision engine of "Where Did I Put It?", an app that catalogues what people keep in
        drawers, boxes, shelves and cabinets so they can find things later.
        You only report physical objects that are actually present in the photo.
    """.trimIndent()

    val text: String = """
        List every distinct physical item in this photo that someone might store and later look for.

        Rules:
        - Name the object itself, never a picture, print, logo or text on it.
          A medal with a shoe printed on it is a "Medal", not "Shoes". A box of tea is "Tea box", not "Tea leaves".
        - Ignore furniture, walls, floors, surfaces, people, and the container being photographed (drawer, shelf, box).
        - Use short, specific, everyday names in English, singular form (e.g. "HDMI cable", "AA battery", "Passport").
        - Merge identical items into one entry.
        - category must be one of: ${categories.joinToString()}.
        - confidence is your certainty from 0.0 to 1.0. Omit items below 0.3.
        - box_2d is the item's bounding box as [ymin, xmin, ymax, xmax], normalized to 0-1000.
        - Return at most 15 items, most prominent first. Return an empty list if you see no items.
    """.trimIndent()
}
