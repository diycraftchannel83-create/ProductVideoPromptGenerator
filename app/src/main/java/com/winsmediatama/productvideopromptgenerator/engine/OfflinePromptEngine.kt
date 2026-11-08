package com.winsmediatama.productvideopromptgenerator.engine

data class PromptRequest(
    val productName: String, val category: String, val style: String,
    val camera: String, val lighting: String, val background: String,
    val duration: String, val aspectRatio: String, val language: String,
    val multiScene: Boolean,
    val productDetails: String = ""
)
data class PromptResult(val title: String, val prompt: String, val negativePrompt: String)

object OfflinePromptEngine {
    private const val NEGATIVE = "Do not alter the original product identity, silhouette, proportions, colors, logo, label or packaging. No duplicated products, warped geometry, misspelled text, invented branding, flicker, morphing, jitter, unnatural motion, blurry details, low-resolution textures or unwanted objects."

    fun generate(r: PromptRequest): List<PromptResult> {
        val product = if (r.productName.isBlank()) "the product in the reference image" else r.productName
        val visualDetails = r.productDetails.trim().takeIf { it.isNotEmpty() }?.let { " User-provided visible product details: $it. Treat these notes as reference observations and do not add unsupported claims." } ?: ""
        val direction = when (r.category) {
            "Product Commercial" -> "Create a persuasive professional product commercial showcasing the product's visual appeal."
            "Cinematic Product" -> "Create a cinematic hero presentation with deliberate composition and refined visual storytelling."
            "Luxury Advertising" -> "Create an elegant luxury campaign with restrained movement and sophisticated atmosphere."
            "Social Media Marketing" -> "Create an attention-grabbing social media advertisement with a strong opening and clear hero moment."
            "Image to Video" -> "Animate the supplied still image into coherent motion while preserving its original composition."
            "Food & Beverage Commercial" -> "Create an appetizing food and beverage commercial emphasizing freshness and visible texture."
            "Fashion & Beauty" -> "Create a polished fashion and beauty campaign with refined editorial presentation."
            "Real Estate Promotion" -> "Create a polished real-estate promotional sequence emphasizing architecture and inviting atmosphere."
            else -> "Create a professional commercial video."
        }
        val concepts = listOf(
            Pair("Hero Reveal", "Start with a clean establishing composition, then perform a slow " + r.camera + " toward the product. Finish on a confident hero frame."),
            Pair("Detail Showcase", "Open on a refined close-up of visible details, then smoothly reveal the complete product with controlled focus."),
            Pair("Atmospheric Campaign", "Build an atmospheric scene around the unchanged product, using subtle environmental movement and a graceful camera reveal.")
        )
        return concepts.map { concept ->
            val scene = if (r.multiScene) {
                "Scene 1 (0-25%): Establish the setting and reveal " + product + ".\n" +
                "Scene 2 (25-55%): Show a close-up of visible product details; keep branding and geometry stable.\n" +
                "Scene 3 (55-80%): " + concept.second + "\n" +
                "Scene 4 (80-100%): End with a clean hero shot and a brief visual hold."
            } else concept.second
            val common = direction + " " + concept.first + ". Use the uploaded image as the primary visual reference for " + product +
                ". Preserve its exact visible design, shape, proportions, colors, logo, label and packaging; do not invent unseen specifications. " +
                "Style: " + r.style + ". Environment: " + r.background + ". Camera: " + r.camera +
                ". Lighting: " + r.lighting + ". Use realistic materials, natural shadows, controlled depth of field and smooth physically plausible motion. " +
                scene + " Output: " + r.duration + ", " + r.aspectRatio + ". Maintain consistent product identity in every frame."
            val prompt = if (r.language == "Indonesia") {
                "Buat video iklan " + r.category + " dengan konsep " + concept.first + ". Gunakan foto yang diunggah sebagai referensi utama untuk " + product +
                    ". Pertahankan desain, bentuk, proporsi, warna, logo, label, dan kemasan yang terlihat. Jangan mengarang spesifikasi yang tidak terlihat. " +
                    (visualDetails.takeIf { it.isNotBlank() }?.let { "Detail visual yang diberikan pengguna: " + r.productDetails.trim() + ". " } ?: "") +
                    "Gaya: " + r.style + ". Latar: " + r.background + ". Kamera: " + r.camera + ". Pencahayaan: " + r.lighting +
                    ". Gunakan material realistis, bayangan alami, depth of field terkontrol, dan gerakan halus. " + scene +
                    " Output: " + r.duration + ", rasio " + r.aspectRatio + ". Jaga konsistensi produk di setiap frame."
            } else common
            PromptResult(concept.first, prompt, NEGATIVE)
        }
    }
}
