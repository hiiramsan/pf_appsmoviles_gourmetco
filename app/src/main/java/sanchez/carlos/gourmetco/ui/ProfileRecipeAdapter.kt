package sanchez.carlos.gourmetco.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import sanchez.carlos.gourmetco.R
import sanchez.carlos.gourmetco.Recipe

class ProfileRecipeAdapter(
    private val context: Context,
    private val recipes: MutableList<Recipe>,
    private val currentUserId: String?,
    private val onEditClick: (Recipe) -> Unit = {},
    private val onDeleteClick: (Recipe) -> Unit = {}
) : BaseAdapter() {

    private val db = FirebaseFirestore.getInstance()

    override fun getCount(): Int = recipes.size

    override fun getItem(position: Int): Recipe = recipes[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_recipe_profile, parent, false)

        val recipe = getItem(position)

        // Configurar título de receta
        val tvRecipeName = view.findViewById<TextView>(R.id.tvRecipeName)
        tvRecipeName.text = recipe.title

        // Configurar botones de editar y eliminar
        val btnEdit = view.findViewById<Button>(R.id.btnEdit)
        val btnDelete = view.findViewById<Button>(R.id.btnDelete)

        // Mostrar u ocultar botones según el propietario de la receta
        if (recipe.userId == currentUserId) {
            btnEdit.visibility = View.VISIBLE
            btnDelete.visibility = View.VISIBLE

            btnEdit.setOnClickListener { onEditClick(recipe) }
            btnDelete.setOnClickListener { onDeleteClick(recipe) }
        } else {
            btnEdit.visibility = View.GONE
            btnDelete.visibility = View.GONE
        }

        // Configurar la imagen con Glide
        val imageView = view.findViewById<ImageView>(R.id.ivRecipeImg)
        loadRecipeImage(imageView, recipe.image)

        return view
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

    fun clear() {
        recipes.clear()
        notifyDataSetChanged()
    }

    fun addAll(newRecipes: List<Recipe>) {
        recipes.addAll(newRecipes)
        notifyDataSetChanged()
    }
}