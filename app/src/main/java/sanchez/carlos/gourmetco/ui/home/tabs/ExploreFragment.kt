package sanchez.carlos.gourmetco.ui.home.tabs

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import sanchez.carlos.gourmetco.R
import sanchez.carlos.gourmetco.Recipe
import sanchez.carlos.gourmetco.ui.RecipeAdapter

class ExploreFragment : Fragment() {

    private lateinit var listView: ListView
    private lateinit var adapter: RecipeAdapter
    private val db: FirebaseFirestore = Firebase.firestore
    private var recipesList = mutableListOf<Recipe>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_explore, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializar ListView y Adapter
        listView = view.findViewById(R.id.lvRecipes)
        adapter = RecipeAdapter(requireContext(), recipesList)
        listView.adapter = adapter

        // Cargar recetas públicas
        loadPublicRecipes()

        // Configurar click listener para los items
        listView.setOnItemClickListener { _, _, position, _ ->
            val selectedRecipe = recipesList[position]
            navigateToRecipeDetail(selectedRecipe)
        }
    }

    private fun loadPublicRecipes() {
        db.collection("recipes").whereEqualTo("isShared", true).get()
            .addOnSuccessListener { documents ->
                recipesList.clear()
                for (document in documents) {
                    try {
                        val recipe = document.toObject(Recipe::class.java).apply {
                            id = document.id  // Asignamos el ID del documento
                        }
                        recipesList.add(recipe)
                    } catch (e: Exception) {
                        Log.e("ExploreFragment", "Error al convertir documento: ${document.id}", e)
                    }
                }
                adapter.notifyDataSetChanged()

                if (recipesList.isEmpty()) {
                    Toast.makeText(
                        context, "No hay recetas públicas disponibles", Toast.LENGTH_SHORT
                    ).show()
                }
            }.addOnFailureListener { exception ->
                Log.w("ExploreFragment", "Error al cargar recetas", exception)
                Toast.makeText(
                    context,
                    "Error al cargar recetas: ${exception.localizedMessage}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun navigateToRecipeDetail(recipe: Recipe) {
        // Pasar la receta completa al fragmento de detalles
        val bundle = bundleOf("recipe" to recipe)
        findNavController().navigate(
            R.id.action_navigation_home_to_detallesRecetaFragment, bundle
        )
    }

    companion object {
        fun newInstance() = ExploreFragment()
    }
}