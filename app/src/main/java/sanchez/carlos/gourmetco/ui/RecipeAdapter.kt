package sanchez.carlos.gourmetco.ui

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.android.flexbox.FlexboxLayout
import sanchez.carlos.gourmetco.R
import sanchez.carlos.gourmetco.Recipe

class RecipeAdapter(private val context: Context, private val recipes: List<Recipe>) :
    BaseAdapter() {

    override fun getCount(): Int = recipes.size

    override fun getItem(position: Int): Recipe = recipes[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view =
            convertView ?: LayoutInflater.from(context).inflate(R.layout.item_recipe, parent, false)

        val recipe = getItem(position)

        // Configurar los textos
        view.findViewById<TextView>(R.id.tvRecipeName).text = recipe.title
        view.findViewById<TextView>(R.id.tvCalories).text = "${recipe.calories} cal"
        view.findViewById<TextView>(R.id.tvTime).text = "${recipe.time} min"
        view.findViewById<TextView>(R.id.tvAuthor).text = recipe.author

        // Configurar la imagen con Glide
        val imageView = view.findViewById<ImageView>(R.id.ivRecipeImg)
        loadRecipeImage(imageView, recipe.image)

        // Configurar categorías
        val flexbox = view.findViewById<FlexboxLayout>(R.id.flexCategories)
        addCategories(flexbox, recipe.categories, context)

        return view
    }

    private fun loadRecipeImage(imageView: ImageView, imageUrl: String?) {
        // Limpiar la imagen antes de cargar una nueva
        Glide.with(context).clear(imageView)

        if (!imageUrl.isNullOrEmpty()) {
            Glide.with(context).load(imageUrl).centerCrop().into(imageView)
        }
    }
}

// para cateogorias
fun addCategories(flexbox: FlexboxLayout, categories: List<String>, context: Context) {
    flexbox.removeAllViews()

    for (category in categories) {
        val categoryTextView = TextView(context).apply {
            text = category
            setPadding(16, 8, 16, 8)
            setTextColor(Color.WHITE)
            textSize = 12f
            background = ContextCompat.getDrawable(context, R.drawable.rounded_background_etiquetas)
        }

        val params = FlexboxLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(8, 4, 8, 4)
        }

        flexbox.addView(categoryTextView, params)
    }
}