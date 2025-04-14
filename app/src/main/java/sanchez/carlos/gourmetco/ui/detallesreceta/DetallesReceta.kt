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
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.android.flexbox.FlexboxLayout
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import sanchez.carlos.gourmetco.R
import sanchez.carlos.gourmetco.Recipe
import sanchez.carlos.gourmetco.ui.create.Ingredient

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [DetallesReceta.newInstance] factory method to
 * create an instance of this fragment.
 */
class DetallesReceta : Fragment() {
    private lateinit var recipeId: String
    private val db: FirebaseFirestore = Firebase.firestore
    private var ingredientsList = ArrayList<Ingredient>()

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
        view.findViewById<TextView>(R.id.tvDescription).text = recipe.instructions

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

        // Configurar bookmark (si está guardado por el usuario actual)
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        val isSaved = currentUserId?.let { recipe.savedBy.contains(it) } ?: false
        view.findViewById<ImageView>(R.id.ivBookmark).setImageResource(
            if (isSaved) R.drawable.love_vector_filled else R.drawable.ic_bookmark
        )
    }

    // Función de extensión para convertir dp a px
    private fun Int.dpToPx(context: Context): Int {
        return (this * context.resources.displayMetrics.density).toInt()
    }

    private fun formatSteps(steps: List<String>): String {
        return steps.mapIndexed { index, step ->
            "${index + 1}. $step"
        }.joinToString("\n\n")
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment DetallesReceta.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) = DetallesReceta().apply {
            arguments = Bundle().apply {
                putString(ARG_PARAM1, param1)
                putString(ARG_PARAM2, param2)
            }
        }
    }

    private class AdaptadorProductos(
        private val context: Context, private val ingredients: List<Ingredient>
    ) : BaseAdapter() {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val view = convertView ?: LayoutInflater.from(context)
                .inflate(R.layout.ingredientes_view, parent, false)

            val ingredient = getItem(position) as Ingredient

            // Debug: Verifica los datos que llegan
            Log.d(
                "IngredientAdapter",
                "Showing: ${ingredient.name} - ${ingredient.quantity} ${ingredient.unit}"
            )

            view.findViewById<TextView>(R.id.ing_nombre).text = ingredient.name
            view.findViewById<TextView>(R.id.ing_cantidad).text = ingredient.quantity
            view.findViewById<TextView>(R.id.ing_medida).text = ingredient.unit

            return view
        }

        override fun getCount(): Int = ingredients.size
        override fun getItem(position: Int): Ingredient = ingredients[position]
        override fun getItemId(position: Int): Long = position.toLong()
    }
}