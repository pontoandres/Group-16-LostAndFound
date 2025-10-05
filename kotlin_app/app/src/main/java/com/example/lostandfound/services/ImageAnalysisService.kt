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
        android.util.Log.d("ImageAnalysisService", "Starting image analysis for: ${imageFile.absolutePath}")
        
        val bitmap = BitmapFactory.decodeFile(imageFile.absolutePath)
        if (bitmap == null) {
            android.util.Log.e("ImageAnalysisService", "Failed to decode bitmap from file")
            return emptyList()
        }
        
        val image = InputImage.fromBitmap(bitmap, 0)
        android.util.Log.d("ImageAnalysisService", "Created InputImage from bitmap")
        
        val suggestions = mutableListOf<ItemSuggestion>()
        
        try {
            // Analyze with image labeling
            android.util.Log.d("ImageAnalysisService", "Starting image labeling analysis")
            val labels = suspendCancellableCoroutine<List<com.google.mlkit.vision.label.ImageLabel>> { continuation ->
                imageLabeler.process(image)
                    .addOnSuccessListener { result ->
                        android.util.Log.d("ImageAnalysisService", "Image labeling success: ${result.size} labels found")
                        continuation.resume(result)
                    }
                    .addOnFailureListener { exception ->
                        android.util.Log.e("ImageAnalysisService", "Image labeling failed", exception)
                        continuation.resumeWithException(exception)
                    }
            }
            android.util.Log.d("ImageAnalysisService", "Processing ${labels.size} labels")
            suggestions.addAll(processLabels(labels))
            
            // Analyze with object detection
            android.util.Log.d("ImageAnalysisService", "Starting object detection analysis")
            val objects = suspendCancellableCoroutine<List<com.google.mlkit.vision.objects.DetectedObject>> { continuation ->
                objectDetector.process(image)
                    .addOnSuccessListener { result ->
                        android.util.Log.d("ImageAnalysisService", "Object detection success: ${result.size} objects found")
                        continuation.resume(result)
                    }
                    .addOnFailureListener { exception ->
                        android.util.Log.e("ImageAnalysisService", "Object detection failed", exception)
                        continuation.resumeWithException(exception)
                    }
            }
            android.util.Log.d("ImageAnalysisService", "Processing ${objects.size} objects")
            suggestions.addAll(processObjects(objects))
            
        } catch (e: Exception) {
            android.util.Log.e("ImageAnalysisService", "Analysis failed", e)
            e.printStackTrace()
        }
        
        val finalSuggestions = suggestions.distinctBy { it.title }.sortedByDescending { it.confidence }
        android.util.Log.d("ImageAnalysisService", "Final suggestions: ${finalSuggestions.size}")
        finalSuggestions.forEach { suggestion ->
            android.util.Log.d("ImageAnalysisService", "Suggestion: ${suggestion.title} (${suggestion.confidence})")
        }
        
        return finalSuggestions
    }
    
    private fun processLabels(labels: List<com.google.mlkit.vision.label.ImageLabel>): List<ItemSuggestion> {
        android.util.Log.d("ImageAnalysisService", "Processing ${labels.size} labels")
        return labels.map { label ->
            android.util.Log.d("ImageAnalysisService", "Label: ${label.text} (confidence: ${label.confidence})")
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
        android.util.Log.d("ImageAnalysisService", "Processing ${objects.size} objects")
        return objects.mapNotNull { obj ->
            obj.labels.firstOrNull()?.let { label ->
                android.util.Log.d("ImageAnalysisService", "Object label: ${label.text} (confidence: ${label.confidence})")
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
