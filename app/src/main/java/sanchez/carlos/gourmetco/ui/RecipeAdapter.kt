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

class RecipeAdapter(private val context: Context, private val recipes: List<Recipe>) : BaseAdapter() {

    override fun getCount(): Int = recipes.size

    override fun getItem(position: Int): Any = recipes[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_recipe, parent, false)

        val recipe = recipes[position]
        val title = view.findViewById<TextView>(R.id.tvRecipeName)
        val image = view.findViewById<ImageView>(R.id.ivRecipeImg)
        val calories = view.findViewById<TextView>(R.id.tvCalories)
        val time = view.findViewById<TextView>(R.id.tvTime)
        val author = view.findViewById<TextView>(R.id.tvAuthor)

        val flexbox = view.findViewById<FlexboxLayout>(R.id.flexCategories)
        addCategories(flexbox, recipe.categories, context)

        title.text = recipe.title
        calories.text = "${recipe.calories.toString()} cal"
        time.text = "${recipe.time.toString()} min"
        author.text = " ${recipe.author}"

        // Cargar imagen con GLide
        Glide.with(context)
            .load(recipe.image)
            .into(image)

        return view
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
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(8, 4, 8, 4)
        }

        flexbox.addView(categoryTextView, params)
    }
}