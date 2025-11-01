package com.example.lostandfound.services

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ItemSuggestion(
    val title: String,
    val category: String,
    val confidence: Float,
    val description: String? = null,
    // Enhanced attributes
    val color: String? = null,
    val brand: String? = null,
    val size: String? = null,
    val condition: String? = null,
    val distinctiveFeatures: List<String> = emptyList(),
    val detailedCaption: String? = null
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
    
    // ===== TECHNIQUE #2: KOTLIN COROUTINES (Enhanced with Dispatchers) =====
    // Uses multithreading: Explicitly controls which thread/dispatcher coroutines use
    // Dispatchers.IO for I/O operations (file reading, network)
    // Dispatchers.Default for CPU-intensive work (image processing)
    // Dispatchers.Main for UI updates
    
    suspend fun analyzeImage(imageFile: File): List<ItemSuggestion> {
        android.util.Log.d("ImageAnalysisService", "Starting enhanced image analysis for: ${imageFile.absolutePath}")
        
        // 1. Load bitmap on IO dispatcher (file I/O operation)
        val bitmap = withContext(Dispatchers.IO) {
            android.util.Log.d("ImageAnalysisService", "Loading bitmap on IO thread")
            BitmapFactory.decodeFile(imageFile.absolutePath)
        }
        
        if (bitmap == null) {
            android.util.Log.e("ImageAnalysisService", "Failed to decode bitmap from file")
            return emptyList()
        }
        
        val image = InputImage.fromBitmap(bitmap, 0)
        android.util.Log.d("ImageAnalysisService", "Created InputImage from bitmap")
        
        // 2. Extract attributes on Default dispatcher (CPU-intensive work)
        val attributes = withContext(Dispatchers.Default) {
            android.util.Log.d("ImageAnalysisService", "Extracting attributes on Default (CPU) thread")
            extractAttributes(bitmap)
        }
        android.util.Log.d("ImageAnalysisService", "Extracted attributes: color=${attributes.color}, brand=${attributes.brand}, size=${attributes.size}, condition=${attributes.condition}, features=${attributes.features.size}")
        
        val suggestions = mutableListOf<ItemSuggestion>()
        
        try {
            // 3. Run ML Kit analyses in parallel using async/await pattern
            android.util.Log.d("ImageAnalysisService", "Starting parallel ML Kit analyses")
            
            // Wrap async calls in coroutineScope
            val (labels, objects) = coroutineScope {
                // Both analyses run concurrently on background threads
                val labelsDeferred = async(Dispatchers.Default) {
                    android.util.Log.d("ImageAnalysisService", "Starting image labeling analysis (async)")
                    suspendCancellableCoroutine<List<com.google.mlkit.vision.label.ImageLabel>> { continuation ->
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
                }
                
                val objectsDeferred = async(Dispatchers.Default) {
                    android.util.Log.d("ImageAnalysisService", "Starting object detection analysis (async)")
                    suspendCancellableCoroutine<List<com.google.mlkit.vision.objects.DetectedObject>> { continuation ->
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
                }
                
                // 4. Wait for both analyses to complete (runs in parallel)
                Pair(labelsDeferred.await(), objectsDeferred.await())
            }
            android.util.Log.d("ImageAnalysisService", "Both analyses completed, processing results")
            
            // 5. Process results on Default dispatcher (CPU work)
            withContext(Dispatchers.Default) {
                android.util.Log.d("ImageAnalysisService", "Processing ${labels.size} labels on Default thread")
                suggestions.addAll(processLabels(labels, attributes))
                
                android.util.Log.d("ImageAnalysisService", "Processing ${objects.size} objects on Default thread")
                suggestions.addAll(processObjects(objects, attributes))
            }
            
        } catch (e: Exception) {
            android.util.Log.e("ImageAnalysisService", "Analysis failed", e)
            e.printStackTrace()
        }
        
        // 6. Final processing on Default dispatcher
        val finalSuggestions = withContext(Dispatchers.Default) {
            suggestions.distinctBy { it.title }.sortedByDescending { it.confidence }
        }
        
        android.util.Log.d("ImageAnalysisService", "Final suggestions: ${finalSuggestions.size}")
        finalSuggestions.forEach { suggestion ->
            android.util.Log.d("ImageAnalysisService", "Suggestion: ${suggestion.title} (${suggestion.confidence})")
            android.util.Log.d("ImageAnalysisService", "  - Color: ${suggestion.color}, Brand: ${suggestion.brand}, Size: ${suggestion.size}")
            android.util.Log.d("ImageAnalysisService", "  - Condition: ${suggestion.condition}, Features: ${suggestion.distinctiveFeatures.size}")
        }
        
        return finalSuggestions
    }
    
    private fun processLabels(labels: List<com.google.mlkit.vision.label.ImageLabel>, attributes: ImageAttributes): List<ItemSuggestion> {
        android.util.Log.d("ImageAnalysisService", "Processing ${labels.size} labels with attributes")
        return labels.map { label ->
            android.util.Log.d("ImageAnalysisService", "Label: ${label.text} (confidence: ${label.confidence})")
            val category = mapLabelToCategory(label.text)
            val allLabels = labels.map { it.text }.joinToString(", ")
            val enhancedTitle = generateEnhancedTitle(label.text, category, attributes)
            val caption = generateDetailedCaption(label.text, category, attributes, allLabels)
            
            ItemSuggestion(
                title = enhancedTitle,
                category = category,
                confidence = label.confidence,
                description = generateDescription(label.text, category),
                color = attributes.color,
                brand = attributes.brand,
                size = attributes.size,
                condition = attributes.condition,
                distinctiveFeatures = attributes.features,
                detailedCaption = caption
            )
        }
    }
    
    private fun processObjects(objects: List<com.google.mlkit.vision.objects.DetectedObject>, attributes: ImageAttributes): List<ItemSuggestion> {
        android.util.Log.d("ImageAnalysisService", "Processing ${objects.size} objects with attributes")
        return objects.mapNotNull { obj ->
            obj.labels.firstOrNull()?.let { label ->
                android.util.Log.d("ImageAnalysisService", "Object label: ${label.text} (confidence: ${label.confidence})")
                val category = mapLabelToCategory(label.text)
                val allLabels = obj.labels.map { it.text }.joinToString(", ")
                val enhancedTitle = generateEnhancedTitle(label.text, category, attributes)
                val caption = generateDetailedCaption(label.text, category, attributes, allLabels)
                
                ItemSuggestion(
                    title = enhancedTitle,
                    category = category,
                    confidence = label.confidence,
                    description = generateDescription(label.text, category),
                    color = attributes.color,
                    brand = attributes.brand,
                    size = attributes.size,
                    condition = attributes.condition,
                    distinctiveFeatures = attributes.features,
                    detailedCaption = caption
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
    
    // Generate enhanced title with image-derived attributes
    private fun generateEnhancedTitle(
        itemLabel: String,
        category: String,
        attributes: ImageAttributes
    ): String {
        val parts = mutableListOf<String>()
        
        // Add color if available
        attributes.color?.let { parts.add(it) }
        
        // Add formatted item label
        val formattedLabel = formatTitle(itemLabel)
        
        // For certain categories, enhance the label
        val enhancedLabel = when {
            formattedLabel.lowercase().contains("phone") || formattedLabel.lowercase().contains("mobile") -> {
                when {
                    formattedLabel.lowercase().contains("smartphone") -> formattedLabel
                    else -> "Smartphone"
                }
            }
            formattedLabel.lowercase().contains("laptop") || formattedLabel.lowercase().contains("notebook") -> {
                "Laptop"
            }
            formattedLabel.lowercase().contains("headphone") || formattedLabel.lowercase().contains("earphone") -> {
                "Headphones"
            }
            formattedLabel.lowercase().contains("watch") -> {
                "Watch"
            }
            formattedLabel.lowercase().contains("key") -> {
                "Keys"
            }
            formattedLabel.lowercase().contains("wallet") -> {
                "Wallet"
            }
            formattedLabel.lowercase().contains("bag") || formattedLabel.lowercase().contains("backpack") -> {
                "Backpack"
            }
            else -> formattedLabel
        }
        
        parts.add(enhancedLabel)
        
        // Add condition for certain categories (optional, in parentheses)
        if (category == "Electronics" || category == "Accessories" || category == "Clothing") {
            attributes.condition?.let { 
                if (it != "Used") { // Only show if not generic "Used"
                    parts.add("($it)")
                }
            }
        }
        
        // Add size for clothing and accessories if available
        if ((category == "Clothing" || category == "Accessories") && attributes.size != null) {
            parts.add("- ${attributes.size}")
        }
        
        val title = parts.joinToString(" ")
        android.util.Log.d("ImageAnalysisService", "Generated enhanced title: $title")
        return title
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
    
    // Data class for extracted attributes
    data class ImageAttributes(
        val color: String?,
        val brand: String?,
        val size: String?,
        val condition: String?,
        val features: List<String>
    )
    
    // Extract attributes from image bitmap
    private fun extractAttributes(bitmap: Bitmap): ImageAttributes {
        android.util.Log.d("ImageAnalysisService", "Extracting attributes from bitmap (${bitmap.width}x${bitmap.height})")
        
        val color = extractDominantColor(bitmap)
        val brand = extractBrand(bitmap)
        val size = estimateSize(bitmap)
        val condition = assessCondition(bitmap)
        val features = extractDistinctiveFeatures(bitmap)
        
        return ImageAttributes(color, brand, size, condition, features)
    }
    
    // Extract dominant color from image
    private fun extractDominantColor(bitmap: Bitmap): String? {
        try {
            val width = bitmap.width
            val height = bitmap.height
            val sampleSize = max(1, min(width, height) / 50) // Sample every Nth pixel
            
            val colorCount = mutableMapOf<String, Int>()
            
            for (x in 0 until width step sampleSize) {
                for (y in 0 until height step sampleSize) {
                    val pixel = bitmap.getPixel(x, y)
                    val r = Color.red(pixel)
                    val g = Color.green(pixel)
                    val b = Color.blue(pixel)
                    
                    val colorName = getColorName(r, g, b)
                    colorCount[colorName] = colorCount.getOrDefault(colorName, 0) + 1
                }
            }
            
            val dominantColor = colorCount.maxByOrNull { it.value }?.key
            android.util.Log.d("ImageAnalysisService", "Dominant color: $dominantColor")
            return dominantColor
        } catch (e: Exception) {
            android.util.Log.e("ImageAnalysisService", "Error extracting color", e)
            return null
        }
    }
    
    // Get color name from RGB values
    private fun getColorName(r: Int, g: Int, b: Int): String {
        data class ColorDef(val name: String, val r: Int, val g: Int, val b: Int)
        
        val colors = listOf(
            ColorDef("Black", 0, 0, 0),
            ColorDef("White", 255, 255, 255),
            ColorDef("Red", 255, 0, 0),
            ColorDef("Blue", 0, 0, 255),
            ColorDef("Green", 0, 255, 0),
            ColorDef("Yellow", 255, 255, 0),
            ColorDef("Orange", 255, 165, 0),
            ColorDef("Purple", 128, 0, 128),
            ColorDef("Pink", 255, 192, 203),
            ColorDef("Brown", 165, 42, 42),
            ColorDef("Grey", 128, 128, 128),
            ColorDef("Silver", 192, 192, 192),
            ColorDef("Gold", 255, 215, 0)
        )
        
        var minDistance = Double.MAX_VALUE
        var closestColor = "Unknown"
        
        for (colorDef in colors) {
            val (name, cr, cg, cb) = colorDef
            val distance = sqrt(
                (r - cr).toDouble().pow(2) +
                (g - cg).toDouble().pow(2) +
                (b - cb).toDouble().pow(2)
            )
            if (distance < minDistance) {
                minDistance = distance
                closestColor = name
            }
        }
        
        // Refine based on brightness and saturation
        val brightness = (r + g + b) / 3
        val max = max(max(r, g), b)
        val min = min(min(r, g), b)
        val saturation = if (max == 0) 0f else (max - min).toFloat() / max
        
        return when {
            brightness < 30 -> "Dark ${closestColor.lowercase()}"
            brightness > 225 && saturation < 0.2f -> "Light ${closestColor.lowercase()}"
            saturation < 0.3f -> when {
                brightness < 100 -> "Dark grey"
                brightness > 200 -> "Light grey"
                else -> "Grey"
            }
            else -> closestColor
        }
    }
    
    // Extract potential brand information (heuristic-based)
    private fun extractBrand(bitmap: Bitmap): String? {
        // In a real implementation, this would use OCR or brand recognition API
        // For now, we'll return null as ML Kit doesn't provide brand detection
        // This can be enhanced with Google Cloud Vision API or TensorFlow Lite custom model
        android.util.Log.d("ImageAnalysisService", "Brand extraction not implemented (requires OCR or cloud API)")
        return null
    }
    
    // Estimate size based on object detection
    private fun estimateSize(bitmap: Bitmap): String? {
        // This is a placeholder - in a real implementation, you'd compare object size
        // relative to known reference objects or use depth estimation
        val width = bitmap.width
        val height = bitmap.height
        val area = width * height
        
        return when {
            area < 50000 -> "Small"
            area < 200000 -> "Medium"
            area < 500000 -> "Large"
            else -> "Very Large"
        }
    }
    
    // Assess condition based on image quality and characteristics
    private fun assessCondition(bitmap: Bitmap): String? {
        try {
            val width = bitmap.width
            val height = bitmap.height
            val sampleSize = max(1, min(width, height) / 100)
            
            var totalBrightness = 0L
            var totalContrast = 0L
            var pixelCount = 0
            
            val brightnessValues = mutableListOf<Int>()
            
            for (x in 0 until width step sampleSize) {
                for (y in 0 until height step sampleSize) {
                    val pixel = bitmap.getPixel(x, y)
                    val r = Color.red(pixel)
                    val g = Color.green(pixel)
                    val b = Color.blue(pixel)
                    val brightness = (r + g + b) / 3
                    
                    totalBrightness += brightness
                    brightnessValues.add(brightness)
                    pixelCount++
                }
            }
            
            val avgBrightness = (totalBrightness / pixelCount).toInt()
            
            // Calculate contrast (standard deviation of brightness)
            val variance = brightnessValues.map { (it - avgBrightness).toDouble().pow(2) }.average()
            val contrast = sqrt(variance).toInt()
            
            // Assess condition based on image characteristics
            val condition = when {
                contrast > 80 && avgBrightness > 150 -> "New/Excellent"
                contrast > 50 && avgBrightness > 100 -> "Good"
                contrast > 30 -> "Fair"
                else -> "Used"
            }
            
            android.util.Log.d("ImageAnalysisService", "Condition: $condition (brightness=$avgBrightness, contrast=$contrast)")
            return condition
        } catch (e: Exception) {
            android.util.Log.e("ImageAnalysisService", "Error assessing condition", e)
            return null
        }
    }
    
    // Extract distinctive features
    private fun extractDistinctiveFeatures(bitmap: Bitmap): List<String> {
        val features = mutableListOf<String>()
        
        try {
            val width = bitmap.width
            val height = bitmap.height
            
            // Check for patterns (stripes, solid, etc.)
            val colorVariation = checkColorVariation(bitmap)
            if (colorVariation < 20) {
                features.add("Solid color")
            } else if (colorVariation > 100) {
                features.add("Patterned or multi-colored")
            }
            
            // Check brightness
            val avgBrightness = getAverageBrightness(bitmap)
            if (avgBrightness > 200) {
                features.add("Bright appearance")
            } else if (avgBrightness < 50) {
                features.add("Dark appearance")
            }
            
            android.util.Log.d("ImageAnalysisService", "Extracted ${features.size} distinctive features: $features")
        } catch (e: Exception) {
            android.util.Log.e("ImageAnalysisService", "Error extracting features", e)
        }
        
        return features
    }
    
    private fun checkColorVariation(bitmap: Bitmap): Int {
        val width = bitmap.width
        val height = bitmap.height
        val sampleSize = max(1, min(width, height) / 50)
        
        val colors = mutableSetOf<String>()
        for (x in 0 until width step sampleSize) {
            for (y in 0 until height step sampleSize) {
                val pixel = bitmap.getPixel(x, y)
                val r = Color.red(pixel)
                val g = Color.green(pixel)
                val b = Color.blue(pixel)
                // Quantize to reduce variations
                val qr = (r / 32) * 32
                val qg = (g / 32) * 32
                val qb = (b / 32) * 32
                colors.add("$qr,$qg,$qb")
            }
        }
        return colors.size
    }
    
    private fun getAverageBrightness(bitmap: Bitmap): Int {
        val width = bitmap.width
        val height = bitmap.height
        val sampleSize = max(1, min(width, height) / 100)
        
        var totalBrightness = 0L
        var pixelCount = 0
        
        for (x in 0 until width step sampleSize) {
            for (y in 0 until height step sampleSize) {
                val pixel = bitmap.getPixel(x, y)
                val r = Color.red(pixel)
                val g = Color.green(pixel)
                val b = Color.blue(pixel)
                totalBrightness += (r + g + b) / 3
                pixelCount++
            }
        }
        
        return if (pixelCount > 0) (totalBrightness / pixelCount).toInt() else 128
    }
    
    // Generate detailed natural language caption
    private fun generateDetailedCaption(
        itemLabel: String,
        category: String,
        attributes: ImageAttributes,
        allLabels: String
    ): String {
        val parts = mutableListOf<String>()
        
        // Start with item identification
        parts.add("A ${formatTitle(itemLabel)}")
        
        // Add color
        attributes.color?.let { parts.add("in $it") }
        
        // Add condition
        attributes.condition?.let { parts.add("($it condition)") }
        
        // Add size
        attributes.size?.let { parts.add("- $it size") }
        
        // Add distinctive features
        if (attributes.features.isNotEmpty()) {
            parts.add("with ${attributes.features.joinToString(", ")}")
        }
        
        // Add additional detected objects/labels
        if (allLabels.split(", ").size > 1) {
            val otherLabels = allLabels.split(", ").filter { it.lowercase() != itemLabel.lowercase() }
            if (otherLabels.isNotEmpty()) {
                parts.add("Detected: ${otherLabels.take(2).joinToString(", ")}")
            }
        }
        
        // Category-specific details
        when (category) {
            "Electronics" -> parts.add("Electronic device that may require verification of serial numbers or unique identifiers.")
            "Accessories" -> parts.add("Personal accessory that may have sentimental value.")
            "Keys" -> parts.add("Keys that likely belong to a specific lock or property.")
            "Books" -> parts.add("Book or document that may contain personal information or notes.")
            "Clothing" -> parts.add("Clothing item that may have size tags or distinctive wear patterns.")
        }
        
        val caption = parts.joinToString(" ")
        android.util.Log.d("ImageAnalysisService", "Generated caption: $caption")
        return caption
    }
    
    // ===== TECHNIQUE #3: FLOW (Kotlin Coroutines) =====
    // Uses multithreading: Flow operators control threading via flowOn
    // Producer runs on Default dispatcher (CPU work), consumer runs on Main (UI updates)
    // Streams analysis progress updates in real-time
    
    fun analyzeImageWithFlow(imageFile: File): Flow<AnalysisProgress> = flow {
        android.util.Log.d("ImageAnalysisService", "Starting Flow-based image analysis")
        
        emit(AnalysisProgress.Loading("Starting analysis..."))
        delay(100) // Simulate small delay
        
        emit(AnalysisProgress.Loading("Loading image..."))
        val bitmap = BitmapFactory.decodeFile(imageFile.absolutePath)
        if (bitmap == null) {
            emit(AnalysisProgress.Error("Failed to load image"))
            return@flow
        }
        
        emit(AnalysisProgress.Loading("Extracting attributes..."))
        delay(200) // Simulate processing time
        val attributes = extractAttributes(bitmap)
        emit(AnalysisProgress.AttributesExtracted(attributes))
        
        emit(AnalysisProgress.Loading("Analyzing with ML Kit..."))
        val image = InputImage.fromBitmap(bitmap, 0)
        
        // Run ML Kit analyses
        val labels = suspendCancellableCoroutine<List<com.google.mlkit.vision.label.ImageLabel>> { continuation ->
            imageLabeler.process(image)
                .addOnSuccessListener { continuation.resume(it) }
                .addOnFailureListener { continuation.resumeWithException(it) }
        }
        emit(AnalysisProgress.Loading("Processing ${labels.size} labels..."))
        
        val objects = suspendCancellableCoroutine<List<com.google.mlkit.vision.objects.DetectedObject>> { continuation ->
            objectDetector.process(image)
                .addOnSuccessListener { continuation.resume(it) }
                .addOnFailureListener { continuation.resumeWithException(it) }
        }
        emit(AnalysisProgress.Loading("Processing ${objects.size} objects..."))
        
        // Generate suggestions
        val suggestions = mutableListOf<ItemSuggestion>()
        suggestions.addAll(processLabels(labels, attributes))
        suggestions.addAll(processObjects(objects, attributes))
        
        val finalSuggestions = suggestions.distinctBy { it.title }.sortedByDescending { it.confidence }
        emit(AnalysisProgress.Complete(finalSuggestions))
        
    }.flowOn(Dispatchers.Default) // Producer runs on Default (CPU) thread pool
    
    // Sealed class for Flow events
    sealed class AnalysisProgress {
        data class Loading(val message: String) : AnalysisProgress()
        data class AttributesExtracted(val attributes: ImageAttributes) : AnalysisProgress()
        data class Complete(val suggestions: List<ItemSuggestion>) : AnalysisProgress()
        data class Error(val message: String) : AnalysisProgress()
    }
    
    // ===== TECHNIQUE #1: CALLBACKS (LAMBDAS) =====
    // Callback-based approach for attribute extraction
    // Uses multithreading: The executor runs attribute extraction on background thread
    // Callback is executed on the main thread via Handler or executor
    
    fun extractAttributesWithCallback(
        bitmap: Bitmap,
        executor: java.util.concurrent.Executor,
        mainHandler: android.os.Handler,
        callback: (ImageAttributes) -> Unit
    ) {
        android.util.Log.d("ImageAnalysisService", "Extracting attributes using callback pattern")
        
        // Run heavy computation on background thread
        executor.execute {
            try {
                // This runs on a background thread
                val attributes = extractAttributes(bitmap)
                android.util.Log.d("ImageAnalysisService", "Attributes extracted on background thread")
                
                // Post result back to main thread via callback
                mainHandler.post {
                    // This runs on the main thread
                    android.util.Log.d("ImageAnalysisService", "Callback executed on main thread")
                    callback(attributes)
                }
            } catch (e: Exception) {
                android.util.Log.e("ImageAnalysisService", "Error in callback-based extraction", e)
                mainHandler.post {
                    // Return default attributes on error
                    callback(ImageAttributes(null, null, null, null, emptyList()))
                }
            }
        }
    }
    
    fun cleanup() {
        imageLabeler.close()
        objectDetector.close()
    }
}
