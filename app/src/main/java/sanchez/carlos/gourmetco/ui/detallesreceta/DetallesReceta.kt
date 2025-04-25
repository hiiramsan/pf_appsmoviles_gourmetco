package sanchez.carlos.gourmetco.ui.detallesreceta

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.android.flexbox.FlexboxLayout
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import sanchez.carlos.gourmetco.R
import sanchez.carlos.gourmetco.Recipe
import sanchez.carlos.gourmetco.ui.create.Ingredient


/**
 * A simple [Fragment] subclass.
 * Use the [DetallesReceta.newInstance] factory method to
 * create an instance of this fragment.
 */
class DetallesReceta : Fragment() {
    private lateinit var recipeId: String
    private val db: FirebaseFirestore = Firebase.firestore
    private var ingredientsList = ArrayList<Ingredient>()
    private var isSaved = false
    private lateinit var bookmarkView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            recipeId = it.getString("recipeId") ?: ""
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_detalles_receta, container, false)

        val ivBack = view.findViewById<ImageView>(R.id.ivBack)
        ivBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        // Inicializar la vista del bookmark
        bookmarkView = view.findViewById(R.id.tvLikes)

        // Configurar el clic en el bookmark para guardar/quitar la receta
        bookmarkView.setOnClickListener {
            toggleSaveRecipe()
        }

        // Cargar los datos de la receta desde Firestore
        loadRecipeDetails(view)

        return view
    }

    private fun loadRecipeDetails(view: View) {
        db.collection("recipes").document(recipeId).get().addOnSuccessListener { document ->
            if (document.exists()) {
                val recipe = document.toObject(Recipe::class.java)
                recipe?.let {
                    // Actualizar la UI con los datos de la receta
                    updateUI(view, it)
                }
            }
        }.addOnFailureListener { exception ->
            Log.e("DetallesReceta", "Error al cargar receta", exception)
        }
    }

    private fun updateUI(view: View, recipe: Recipe) {
        // Configurar imagen (usando Glide como ejemplo)
        val imageView = view.findViewById<ImageView>(R.id.ivRecipeImage)
        if (!recipe.image.isNullOrEmpty()) {
            Glide.with(this).load(recipe.image).into(imageView)
        } else if (!recipe.photoUriL.isNullOrEmpty()) {
            Glide.with(this).load(recipe.photoUriL).into(imageView)
        }

        // Configurar título
        view.findViewById<TextView>(R.id.tvRecipeTitle).text = recipe.title

        // Configurar autor
        view.findViewById<TextView>(R.id.tvAuthorName).text = recipe.author

        // Configurar veces guardado (como likes)
        view.findViewById<TextView>(R.id.tvLikes).text = recipe.timesSaved.toString()

        // Configurar descripción (usando instructions)
        view.findViewById<TextView>(R.id.tvDescription).text = recipe.description.toString()

        // Configurar ingredientes
        ingredientsList.clear()
        ingredientsList.addAll(recipe.ingredients)
        val listView = view.findViewById<ListView>(R.id.listView_ingredientes)
        listView.adapter = AdaptadorProductos(requireContext(), ingredientsList)

        // Configurar pasos de preparación (si necesitas separar las instrucciones en pasos)
        val steps = recipe.instructions.split("\n").filter { it.isNotBlank() }
        view.findViewById<TextView>(R.id.tvStepsContent).text = formatSteps(steps)

        // Configurar categorías (tags)
        val flexTags = view.findViewById<FlexboxLayout>(R.id.flexTags)
        flexTags.removeAllViews()
        recipe.categories.forEach { category ->
            val textView = TextView(context).apply {
                text = category
                setTextColor(ContextCompat.getColor(context, android.R.color.white))
                textSize = 12f
                background =
                    ContextCompat.getDrawable(context, R.drawable.rounded_background_etiquetas)
                val params = ViewGroup.MarginLayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    marginEnd = 8.dpToPx(context)
                }
                layoutParams = params
                setPadding(
                    8.dpToPx(context), 4.dpToPx(context), 8.dpToPx(context), 4.dpToPx(context)
                )
            }
            flexTags.addView(textView)
        }

        // Verificar si la receta está guardada por el usuario actual
        checkIfRecipeIsSaved()
    }

    private fun checkIfRecipeIsSaved() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        db.collection("recipes").document(recipeId).get().addOnSuccessListener { document ->
            if (document.exists()) {
                val recipe = document.toObject(Recipe::class.java)
                recipe?.let {
                    // Comprobar si el usuario actual ha guardado esta receta
                    isSaved = it.savedBy.contains(currentUserId)

                    // Actualizar la apariencia del bookmark según el estado
                    updateBookmarkAppearance()
                }
            }
        }
    }

    private fun updateBookmarkAppearance() {
        val drawableRes = if (isSaved) R.drawable.bookmarkcheck else R.drawable.bookmark
        bookmarkView.setCompoundDrawablesWithIntrinsicBounds(drawableRes, 0, 0, 0)
    }

    private fun toggleSaveRecipe() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserId == null) {
            Toast.makeText(context, "Debes iniciar sesión para guardar recetas", Toast.LENGTH_SHORT)
                .show()
            return
        }

        val recipeRef = db.collection("recipes").document(recipeId)

        db.runTransaction { transaction ->
            val snapshot = transaction.get(recipeRef)
            val currentSavedBy = snapshot.get("savedBy") as? List<String> ?: emptyList()

            val newSavedBy = if (currentUserId in currentSavedBy) {
                currentSavedBy - currentUserId
            } else {
                currentSavedBy + currentUserId
            }

            transaction.update(
                recipeRef, "savedBy", newSavedBy, "timesSaved", newSavedBy.size.toLong()
            )

            currentUserId in newSavedBy
        }.addOnSuccessListener { newSavedState ->
            isSaved = newSavedState

            // Update the UI
            bookmarkView.text = if (isSaved) {
                (bookmarkView.text.toString().toInt() + 1).toString()
            } else {
                (bookmarkView.text.toString().toInt() - 1).toString()
            }

            updateBookmarkAppearance()

            val message = if (isSaved) "Receta guardada correctamente"
            else "Receta eliminada de tus guardados"

            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }.addOnFailureListener { e ->
            Log.e("DetallesReceta", "Error al actualizar guardado", e)
            Toast.makeText(context, "Error al guardar receta", Toast.LENGTH_SHORT).show()
        }
    }

    private fun formatSteps(steps: List<String>): String {
        return steps.mapIndexed { index, step ->
            "${index + 1}. $step"
        }.joinToString("\n\n")
    }

    companion object {
        private const val ARG_PARAM1 = "param1"
        private const val ARG_PARAM2 = "param2"

        @JvmStatic
        fun newInstance(param1: String, param2: String) = DetallesReceta().apply {
            arguments = Bundle().apply {
                putString(ARG_PARAM1, param1)
                putString(ARG_PARAM2, param2)
            }
        }

        // Función de extensión para convertir dp a px
        fun Int.dpToPx(context: Context): Int {
            return (this * context.resources.displayMetrics.density).toInt()
        }
    }


    private class AdaptadorProductos(
        private val context: Context, private val ingredients: List<Ingredient>
    ) : BaseAdapter() {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val view = convertView ?: LayoutInflater.from(context)
                .inflate(R.layout.ingredientes_view, parent, false)

            val ingredient = getItem(position)

            view.findViewById<TextView>(R.id.ing_nombre).text = ingredient.name
            view.findViewById<TextView>(R.id.ing_cantidad).text = ingredient.quantity
            view.findViewById<TextView>(R.id.ing_medida).text = ingredient.unit

            val layoutParams = ViewGroup.MarginLayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
            )
            val marginBottom = if (position < count - 1) 10.dpToPx(context) else 0
            layoutParams.setMargins(0, 0, 0, marginBottom)
            view.layoutParams = layoutParams

            return view
        }

        override fun getCount(): Int = ingredients.size
        override fun getItem(position: Int): Ingredient = ingredients[position]
        override fun getItemId(position: Int): Long = position.toLong()
    }
}