package sanchez.carlos.gourmetco.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
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

        title.text = recipe.title
        image.setImageResource(recipe.image)
        calories.text = recipe.calories
        time.text = recipe.time
        author.text = recipe.author

        return view
    }
}