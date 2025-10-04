package com.example.lostandfound.services

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ItemSuggestion(
    val title: String,
    val category: String,
    val confidence: Float,
    val description: String? = null
) : Parcelable

class ImageAnalysisService {
    
    private val imageLabeler = ImageLabeling.getClient(
        ImageLabelerOptions.Builder()
            .setConfidenceThreshold(0.7f)
            .build()
    )
    
    private val objectDetector = ObjectDetection.getClient(
        ObjectDetectorOptions.Builder()
            .setDetectorMode(ObjectDetectorOptions.SINGLE_IMAGE_MODE)
            .enableMultipleObjects()
            .enableClassification()
            .build()
    )
    
    suspend fun analyzeImage(imageFile: File): List<ItemSuggestion> {
        val bitmap = BitmapFactory.decodeFile(imageFile.absolutePath)
        val image = InputImage.fromBitmap(bitmap, 0)
        
        val suggestions = mutableListOf<ItemSuggestion>()
        
        try {
            // Analyze with image labeling
            val labels = suspendCancellableCoroutine<List<com.google.mlkit.vision.label.ImageLabel>> { continuation ->
                imageLabeler.process(image)
                    .addOnSuccessListener { result ->
                        continuation.resume(result)
                    }
                    .addOnFailureListener { exception ->
                        continuation.resumeWithException(exception)
                    }
            }
            suggestions.addAll(processLabels(labels))
            
            // Analyze with object detection
            val objects = suspendCancellableCoroutine<List<com.google.mlkit.vision.objects.DetectedObject>> { continuation ->
                objectDetector.process(image)
                    .addOnSuccessListener { result ->
                        continuation.resume(result)
                    }
                    .addOnFailureListener { exception ->
                        continuation.resumeWithException(exception)
                    }
            }
            suggestions.addAll(processObjects(objects))
            
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        return suggestions.distinctBy { it.title }.sortedByDescending { it.confidence }
    }
    
    private fun processLabels(labels: List<com.google.mlkit.vision.label.ImageLabel>): List<ItemSuggestion> {
        return labels.map { label ->
            val category = mapLabelToCategory(label.text)
            ItemSuggestion(
                title = formatTitle(label.text),
                category = category,
                confidence = label.confidence,
                description = generateDescription(label.text, category)
            )
        }
    }
    
    private fun processObjects(objects: List<com.google.mlkit.vision.objects.DetectedObject>): List<ItemSuggestion> {
        return objects.mapNotNull { obj ->
            obj.labels.firstOrNull()?.let { label ->
                val category = mapLabelToCategory(label.text)
                ItemSuggestion(
                    title = formatTitle(label.text),
                    category = category,
                    confidence = label.confidence,
                    description = generateDescription(label.text, category)
                )
            }
        }
    }
    
    private fun mapLabelToCategory(label: String): String {
        val lowerLabel = label.lowercase()
        
        return when {
            lowerLabel.contains("phone") || lowerLabel.contains("mobile") || lowerLabel.contains("smartphone") -> "Electronics"
            lowerLabel.contains("laptop") || lowerLabel.contains("computer") || lowerLabel.contains("notebook") -> "Electronics"
            lowerLabel.contains("headphone") || lowerLabel.contains("earphone") || lowerLabel.contains("headset") -> "Electronics"
            lowerLabel.contains("watch") || lowerLabel.contains("clock") -> "Accessories"
            lowerLabel.contains("glasses") || lowerLabel.contains("sunglasses") -> "Accessories"
            lowerLabel.contains("bag") || lowerLabel.contains("backpack") || lowerLabel.contains("purse") -> "Accessories"
            lowerLabel.contains("wallet") || lowerLabel.contains("purse") -> "Accessories"
            lowerLabel.contains("key") || lowerLabel.contains("keys") -> "Keys"
            lowerLabel.contains("book") || lowerLabel.contains("textbook") || lowerLabel.contains("notebook") -> "Books"
            lowerLabel.contains("umbrella") -> "Accessories"
            lowerLabel.contains("clothing") || lowerLabel.contains("shirt") || lowerLabel.contains("jacket") -> "Clothing"
            lowerLabel.contains("shoe") || lowerLabel.contains("boot") -> "Clothing"
            else -> "Other"
        }
    }
    
    private fun formatTitle(label: String): String {
        return label.split(" ")
            .joinToString(" ") { word ->
                word.lowercase().replaceFirstChar { it.uppercase() }
            }
    }
    
    private fun generateDescription(label: String, category: String): String {
        return when (category) {
            "Electronics" -> "Electronic device found. Please contact to verify ownership details."
            "Accessories" -> "Personal accessory found. Contact to arrange pickup."
            "Keys" -> "Keys found. Please describe the keychain or provide identifying details."
            "Books" -> "Book or document found. Check for personal information inside."
            "Clothing" -> "Clothing item found. Please describe size and any distinctive features."
            else -> "Item found. Please contact to verify ownership."
        }
    }
    
    fun cleanup() {
        imageLabeler.close()
        objectDetector.close()
    }
}
