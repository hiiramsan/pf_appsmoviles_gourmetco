package sanchez.carlos.gourmetco.ui.home.tabs

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import com.google.android.flexbox.FlexboxLayout
import com.google.firebase.firestore.Query
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import sanchez.carlos.gourmetco.R
import sanchez.carlos.gourmetco.Recipe
import sanchez.carlos.gourmetco.ui.RecipeAdapter

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [MyRecipesFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MyRecipesFragment : Fragment() {

    private lateinit var listView: ListView
    private lateinit var adapter: RecipeAdapter
    private val db: FirebaseFirestore = Firebase.firestore
    private var recipesList = mutableListOf<Recipe>()
    private var allRecipes = mutableListOf<Recipe>()
    private lateinit var auth: FirebaseAuth
    private var currentUserId: String? = null
    private var isDataLoaded = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_my_recipes, container, false)
    }

    override fun onResume() {
        super.onResume()
        loadUserRecipes()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        currentUserId = auth.currentUser?.uid

        listView = view.findViewById(R.id.lvRecipes)
        adapter = RecipeAdapter(requireContext(), recipesList, currentUserId)
        listView.adapter = adapter

        if (!isDataLoaded) {
            loadUserRecipes()
            isDataLoaded = true
        }

        listView.setOnItemClickListener { _, _, position, _ ->
            val selectedRecipe = recipesList[position]
            navigateToRecipeDetail(selectedRecipe)
        }
    }

    private fun loadUserRecipes() {
        currentUserId ?: return

        // Cambiamos el get() por addSnapshotListener para actualización en tiempo real
        db.collection("recipes").whereEqualTo("userId", currentUserId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { documents, error ->
                if (error != null) {
                    Log.w("MyRecipesFragment", "Error al escuchar cambios", error)
                    Toast.makeText(
                        context,
                        "Error al cargar recetas: ${error.localizedMessage}",
                        Toast.LENGTH_LONG
                    ).show()
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
                        Log.e("MyRecipesFragment", "Error converting document: ${document.id}", e)
                    }
                }

                adapter.notifyDataSetChanged()
                listView.invalidateViews() // Forzar redibujado de la vista

                if (recipesList.isEmpty()) {
                    Toast.makeText(
                        context, "No tienes recetas guardadas", Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    fun getAllRecipes(): List<Recipe> {
        return allRecipes
    }

    private fun navigateToRecipeDetail(recipe: Recipe) {
        val bundle = bundleOf("recipeId" to recipe.id)
        findNavController().navigate(
            R.id.action_navigation_home_to_detallesRecetaFragment, bundle
        )
    }

    companion object {
        fun newInstance() = MyRecipesFragment()
    }
}