package sanchez.carlos.gourmetco.ui.trending

import androidx.fragment.app.viewModels
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import sanchez.carlos.gourmetco.R
import sanchez.carlos.gourmetco.Recipe
import sanchez.carlos.gourmetco.ui.RecipeAdapter

class TrendingFragment : Fragment() {

    private lateinit var listView: ListView
    private lateinit var adapter: RecipeAdapter
    private val db: FirebaseFirestore = Firebase.firestore
    private var recipesList = mutableListOf<Recipe>()
    private var allRecipes = mutableListOf<Recipe>()
    private lateinit var auth: FirebaseAuth
    private var currentUserId: String? = null
    private var isDataLoaded = false

    companion object {
        fun newInstance() = TrendingFragment()
    }

    private val viewModel: TrendingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        currentUserId = auth.currentUser?.uid
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_trending, container, false)
    }

    override fun onResume() {
        super.onResume()
        loadTrendingRecipes()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        listView = view.findViewById(R.id.lvRecipes)
        adapter = RecipeAdapter(
            requireContext(), recipesList, currentUserId
        )
        listView.adapter = adapter

        if (!isDataLoaded) {
            loadTrendingRecipes()
            isDataLoaded = true
        }

        listView.setOnItemClickListener { _, _, position, _ ->
            val selectedRecipe = recipesList[position]
            navigateToRecipeDetail(selectedRecipe)
        }
    }

    private fun loadTrendingRecipes() {
        // Usamos addSnapshotListener para actualización en tiempo real
        db.collection("recipes").whereGreaterThan("timesSaved", 0)
            .orderBy("timesSaved", Query.Direction.DESCENDING).limit(4)
            .addSnapshotListener { documents, error ->
                if (error != null) {
                    Log.e("TrendingFragment", "Error escuchando cambios", error)
                    Toast.makeText(
                        context,
                        "Error al cargar recetas: ${error.localizedMessage}",
                        Toast.LENGTH_LONG
                    ).show()
                    showEmptyState(true)
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
                        Log.e("TrendingFragment", "Error converting document: ${document.id}", e)
                    }
                }

                adapter.notifyDataSetChanged()
                listView.invalidateViews() // Forzar redibujado de la vista
                showEmptyState(recipesList.isEmpty())
            }
    }

    fun getAllRecipes(): List<Recipe> {
        return allRecipes
    }

    private fun navigateToRecipeDetail(recipe: Recipe) {
        val bundle = bundleOf("recipeId" to recipe.id)
        findNavController().navigate(R.id.action_trending_to_detallesRecetaFragment, bundle)
    }

    private fun showEmptyState(show: Boolean) {
        view?.findViewById<TextView>(R.id.tvEmptyState)?.visibility =
            if (show) View.VISIBLE else View.GONE
        listView.visibility = if (show) View.GONE else View.VISIBLE
    }
}