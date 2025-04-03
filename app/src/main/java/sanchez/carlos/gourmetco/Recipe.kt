package sanchez.carlos.gourmetco

import android.widget.RemoteViews.DrawInstructions
import sanchez.carlos.gourmetco.ui.create.Ingredient

data class Recipe(
    val title: String = "",
    val image: String? = null,
    val calories: Long = 0,
    val time: Long = 0,
    val author: String = "",
    val photoUriL: String? = null,
    val isShared: Boolean = false,
    val instructions: String = "",
    val ingredients: List<Ingredient> = emptyList(),
    val categories: List<String> = emptyList(),
    val userId: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    constructor() : this("", null, 0, 0, "", null, false, "", emptyList(), emptyList(), "", 0)
}



