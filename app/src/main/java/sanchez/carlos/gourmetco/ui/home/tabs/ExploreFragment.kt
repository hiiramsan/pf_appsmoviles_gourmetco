package sanchez.carlos.gourmetco.ui.home.tabs

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
import com.google.firebase.auth.FirebaseAuth
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
    private var allRecipes = mutableListOf<Recipe>() // Lista completa para filtrar
    private lateinit var auth: FirebaseAuth
    private var currentUserId: String? = null
    private var isDataLoaded = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_explore, container, false)
    }

    override fun onResume() {
        super.onResume()
        loadPublicRecipes()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        currentUserId = auth.currentUser?.uid

        listView = view.findViewById(R.id.lvRecipes)
        adapter = RecipeAdapter(requireContext(), recipesList, currentUserId)
        listView.adapter = adapter

        if (!isDataLoaded) {
            loadPublicRecipes()
            isDataLoaded = true
        }

        listView.setOnItemClickListener { _, _, position, _ ->
            val selectedRecipe = recipesList[position]
            navigateToRecipeDetail(selectedRecipe)
        }
    }

    private fun loadPublicRecipes() {
        db.collection("recipes")
            .whereEqualTo("isShared", true)
            .addSnapshotListener { documents, error ->
                if (error != null) {
                    Log.w("ExploreFragment", "Error al escuchar cambios", error)
                    return@addSnapshotListener
                }

                recipesList.clear()
                allRecipes.clear()

                documents?.forEach { document ->
                    try {
                        val recipe = document.toObject(Recipe::class.java).apply {
                            id = document.id
                        }
                        recipesList.add(recipe)
                        allRecipes.add(recipe)
                    } catch (e: Exception) {
                        Log.e("ExploreFragment", "Error al convertir documento", e)
                    }
                }

                adapter.notifyDataSetChanged()
                listView.invalidateViews()

                if (recipesList.isEmpty()) {
                    Toast.makeText(
                        context,
                        "No hay recetas públicas disponibles",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    // Método para acceder a todas las recetas (usado para búsqueda)
    fun getAllRecipes(): List<Recipe> {
        return allRecipes
    }

    private fun navigateToRecipeDetail(recipe: Recipe) {
        val bundle = bundleOf("recipeId" to recipe.id)
        findNavController().navigate(
            R.id.action_navigation_home_to_detallesRecetaFragment,
            bundle
        )
    }

    companion object {
        fun newInstance() = ExploreFragment()
    }
}