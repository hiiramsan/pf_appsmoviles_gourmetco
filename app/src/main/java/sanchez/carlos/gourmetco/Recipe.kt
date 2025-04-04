package sanchez.carlos.gourmetco

import android.widget.RemoteViews.DrawInstructions
import sanchez.carlos.gourmetco.ui.create.Ingredient

data class Recipe(
    var id: String = "",
    val title: String = "",
    val image: String? = null,
    val calories: Long = 0L,
    val time: Long = 0L,
    val author: String = "",
    val photoUriL: String? = null,
    val isShared: Boolean = false,
    val instructions: String = "",
    val ingredients: List<Ingredient> = emptyList(),
    val categories: List<String> = emptyList(),
    val userId: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    constructor() : this("", "", null, 0L, 0L, "", null, false, "", emptyList(), emptyList(), "", 0)
}



