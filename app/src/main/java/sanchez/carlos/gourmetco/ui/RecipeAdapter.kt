package sanchez.carlos.gourmetco.ui

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.android.flexbox.FlexboxLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import sanchez.carlos.gourmetco.R
import sanchez.carlos.gourmetco.Recipe

class RecipeAdapter(
    private val context: Context,
    private val recipes: MutableList<Recipe>,
    private val currentUserId: String?
) : BaseAdapter() {

    private val db = FirebaseFirestore.getInstance()

    override fun getCount(): Int = recipes.size

    override fun getItem(position: Int): Recipe = recipes[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_recipe, parent, false)

        val recipe = getItem(position)

        // Configurar los textos
        view.findViewById<TextView>(R.id.tvRecipeName).text = recipe.title
        view.findViewById<TextView>(R.id.tvCalories).text = "${recipe.calories} cal"
        view.findViewById<TextView>(R.id.tvTime).text = "${recipe.time} min"
        view.findViewById<TextView>(R.id.tvAuthor).text = recipe.author

        // Configurar likes
        val tvLikes = view.findViewById<TextView>(R.id.tvLikes)
        tvLikes.text = recipe.timesSaved.toString()

        // Verificar si el usuario actual ya dio like
        val isLiked = currentUserId in recipe.savedBy
        val drawableRes = if (isLiked) R.drawable.love_vector_filled else R.drawable.love_vector
        tvLikes.setCompoundDrawablesWithIntrinsicBounds(drawableRes, 0, 0, 0)

        // Configurar listener para likes
        tvLikes.setOnClickListener {
            if (currentUserId != null) {
                toggleLike(recipe, position)
            } else {
                Toast.makeText(context, "Debes iniciar sesión para guardar recetas", Toast.LENGTH_SHORT).show()
            }
        }

        // Configurar la imagen con Glide
        val imageView = view.findViewById<ImageView>(R.id.ivRecipeImg)
        loadRecipeImage(imageView, recipe.image)

        // Configurar categorías
        val flexbox = view.findViewById<FlexboxLayout>(R.id.flexCategories)
        addCategories(flexbox, recipe.categories, context)

        return view
    }

    private fun toggleLike(recipe: Recipe, position: Int) {
        val recipeRef = db.collection("recipes").document(recipe.id)

        db.runTransaction { transaction ->
            val snapshot = transaction.get(recipeRef)
            val currentSavedBy = snapshot.get("savedBy") as? List<String> ?: emptyList()
            val currentTimesSaved = snapshot.getLong("timesSaved") ?: 0L

            val newSavedBy = if (currentUserId in currentSavedBy) {
                currentSavedBy - currentUserId!!
            } else {
                currentSavedBy + currentUserId!!
            }

            transaction.update(recipeRef,
                "savedBy", newSavedBy,
                "timesSaved", newSavedBy.size.toLong()
            )

            // Actualizar la lista local
            recipes[position] = recipe.copy(
                savedBy = newSavedBy,
                timesSaved = newSavedBy.size.toLong()
            )
        }.addOnSuccessListener {
            notifyDataSetChanged()
        }.addOnFailureListener { e ->
            Log.e("RecipeAdapter", "Error al actualizar like", e)
            Toast.makeText(context, "Error al guardar receta", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadRecipeImage(imageView: ImageView, imageUrl: String?) {
        Glide.with(context).clear(imageView)
        if (!imageUrl.isNullOrEmpty()) {
            Glide.with(context)
                .load(imageUrl)
                .centerCrop()
                .into(imageView)
        }
    }
}

// Función para categorías
fun addCategories(flexbox: FlexboxLayout, categories: List<String>, context: Context) {
    flexbox.removeAllViews()

    for (category in categories.take(3)) { // Mostrar máximo 3 categorías
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

    if (categories.size > 3) {
        val moreTextView = TextView(context).apply {
            text = "+${categories.size - 3}"
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

        flexbox.addView(moreTextView, params)
    }
}