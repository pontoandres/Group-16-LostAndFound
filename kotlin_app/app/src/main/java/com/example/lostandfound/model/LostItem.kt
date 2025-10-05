import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class LostItem(
    val id: String,
    val user_id: String,
    val title: String,
    val description: String,
    val place: String? = null,
    val lost_date: String? = null,
    val category: String? = null,
    val image_url: String? = null,
    val status: String? = null,
    val created_at: String? = null,
    val location: String? = null,
    val lost_at: String? = null,
    val postedBy: String? = null
)