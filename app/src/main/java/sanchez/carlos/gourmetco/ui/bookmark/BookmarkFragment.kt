package sanchez.carlos.gourmetco.ui.bookmark

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
import sanchez.carlos.gourmetco.R
import sanchez.carlos.gourmetco.Recipe
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import sanchez.carlos.gourmetco.ui.RecipeAdapter

class BookmarkFragment : Fragment() {

    private lateinit var listView: ListView
    private lateinit var adapter: RecipeAdapter
    private val db: FirebaseFirestore = Firebase.firestore
    private var recipesList = mutableListOf<Recipe>()
    private lateinit var auth: FirebaseAuth
    private var currentUserId: String? = null

    companion object {
        fun newInstance() = BookmarkFragment()
    }

    private val viewModel: BookmarkViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_bookmark, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        currentUserId = auth.currentUser?.uid

        listView = view.findViewById(R.id.lvRecipes)
        adapter = RecipeAdapter(requireContext(), recipesList, currentUserId)
        listView.adapter = adapter

        loadBookmarkedRecipes()

        listView.setOnItemClickListener { _, _, position, _ ->
            val selectedRecipe = recipesList[position]
            navigateToRecipeDetail(selectedRecipe)
        }

    }

    private fun navigateToRecipeDetail(recipe: Recipe) {
        val bundle = bundleOf("recipeId" to recipe.id)
        findNavController().navigate(R.id.action_bookmark_to_detallesRecetaFragment, bundle)

    }


    private fun loadBookmarkedRecipes() {
        currentUserId ?: return

        db.collection("recipes")
            .whereArrayContains("savedBy", currentUserId!!)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                recipesList.clear()
                for (document in documents) {
                    try {
                        val recipe = document.toObject(Recipe::class.java).apply {
                            id = document.id
                        }
                        recipesList.add(recipe)
                    } catch (e: Exception) {
                        Log.e("BookmarksFragment", "Error convirtiendo recets", e)
                    }
                }
                adapter.notifyDataSetChanged()

                if (recipesList.isEmpty()) {
                    showEmptyState(true)
                } else {
                    showEmptyState(false)
                }
            }
            .addOnFailureListener { exception ->
                Log.e("BookmarksFragment", "Error cargando recetasgardadas", exception)
                Toast.makeText(
                    context,
                    "Error cargarndo recetasguardads: ${exception.localizedMessage}",
                    Toast.LENGTH_SHORT
                ).show()

                showEmptyState(true)
            }
    }

    private fun showEmptyState(show: Boolean) {
        view?.findViewById<TextView>(R.id.tvEmptyState)?.visibility =
            if (show) View.VISIBLE else View.GONE
        listView.visibility = if (show) View.GONE else View.VISIBLE
    }

}