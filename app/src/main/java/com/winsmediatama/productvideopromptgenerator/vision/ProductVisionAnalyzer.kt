package com.winsmediatama.productvideopromptgenerator.vision

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

data class ProductVisionResult(
    val labels: List<String>,
    val visibleText: List<String>
) {
    fun toPromptNotes(): String = buildList {
        if (labels.isNotEmpty()) add("AI image labels: ${labels.joinToString(", ")}")
        if (visibleText.isNotEmpty()) add("Text visibly detected on the product/image: ${visibleText.joinToString(" | ")}")
    }.joinToString(". ")
}

/**
 * On-device ML Kit analysis. The first model use may require Google Play services
 * to download its model; subsequent analysis can run locally when the model is available.
 */
class ProductVisionAnalyzer {
    suspend fun analyze(context: Context, uri: Uri): ProductVisionResult {
        val image = InputImage.fromFilePath(context, uri)
        val labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        return try {
            val labels = labeler.process(image).await()
                .sortedByDescending { it.confidence }
                .take(8)
                .map { "${it.text} (${(it.confidence * 100).toInt()}%)" }
            val text = recognizer.process(image).await().text
                .lines()
                .map { it.trim() }
                .filter { it.isNotBlank() }
                .distinct()
                .take(12)
            ProductVisionResult(labels, text)
        } finally {
            labeler.close()
            recognizer.close()
        }
    }
}

private suspend fun <T> com.google.android.gms.tasks.Task<T>.await(): T =
    suspendCancellableCoroutine { continuation ->
        addOnSuccessListener { result ->
            if (continuation.isActive) continuation.resume(result)
        }
        addOnFailureListener { error ->
            if (continuation.isActive) continuation.resumeWithException(error)
        }
        addOnCanceledListener {
            continuation.cancel()
        }
    }
