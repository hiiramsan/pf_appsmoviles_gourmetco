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
    private val allRecipes = mutableListOf<Recipe>()
    private val displayedRecipes = mutableListOf<Recipe>()
    private var currentSearchQuery = ""
    private lateinit var auth: FirebaseAuth
    private var currentUserId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_explore, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        currentUserId = auth.currentUser?.uid

        listView = view.findViewById(R.id.lvRecipes)
        adapter = RecipeAdapter(requireContext(), displayedRecipes, currentUserId)
        listView.adapter = adapter

        listView.setOnItemClickListener { _, _, position, _ ->
            val selectedRecipe = displayedRecipes[position]
            navigateToRecipeDetail(selectedRecipe)
        }

        loadPublicRecipes()
    }

    private fun loadPublicRecipes() {
        db.collection("recipes")
            .whereEqualTo("isShared", true)
            .addSnapshotListener { documents, error ->
                if (error != null) {
                    Log.w("ExploreFragment", "Listen failed.", error)
                    return@addSnapshotListener
                }

                allRecipes.clear()

                documents?.forEach { document ->
                    try {
                        val recipe = document.toObject(Recipe::class.java).apply {
                            id = document.id
                        }
                        allRecipes.add(recipe)
                    } catch (e: Exception) {
                        Log.e("ExploreFragment", "Error converting document", e)
                    }
                }

                // After loading all recipes, apply current search filter
                applySearchFilter(currentSearchQuery)
            }
    }

    fun applySearchFilter(query: String) {
        currentSearchQuery = query

        displayedRecipes.clear()

        if (query.isEmpty()) {
            displayedRecipes.addAll(allRecipes)
        } else {
            displayedRecipes.addAll(allRecipes.filter { recipe ->
                recipe.title.contains(query, ignoreCase = true)
            })
        }

        adapter.notifyDataSetChanged()
        listView.invalidateViews()

        if (displayedRecipes.isEmpty()) {
            if (allRecipes.isEmpty()) {
                Toast.makeText(
                    context,
                    "No public recipes available",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    context,
                    "No recipes match your search",
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