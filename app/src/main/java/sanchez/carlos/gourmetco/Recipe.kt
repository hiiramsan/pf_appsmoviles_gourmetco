package sanchez.carlos.gourmetco

import android.widget.RemoteViews.DrawInstructions
import sanchez.carlos.gourmetco.ui.create.Ingredient

data class Recipe(
    val title: String,
    val image: Int,
    val calories: String,
    val time: String,
    val author: String,
    val photoUriL: String? = null,
    val isShared: Boolean = false,
    val instructions: String,
    val ingredients: List<Ingredient>,
    val categories: List<String> = emptyList(),
    val userId: String = "",
    val createdAt: Long = System.currentTimeMillis(),
)

