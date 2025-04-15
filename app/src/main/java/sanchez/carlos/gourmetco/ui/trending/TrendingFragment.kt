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

    companion object {
        fun newInstance() = TrendingFragment()
    }

    private val viewModel: TrendingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // ViewModel si se llega a usar en el futuro
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_trending, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        listView = view.findViewById(R.id.lvRecipes)
        adapter = RecipeAdapter(requireContext(), recipesList, null)
        listView.adapter = adapter

        loadTrendingRecipes()

        listView.setOnItemClickListener { _, _, position, _ ->
            val recipeId = recipesList[position].id
            findNavController().navigate(
                R.id.action_navigation_home_to_detallesRecetaFragment,
                bundleOf("recipeId" to recipeId)
            )
        }
    }

    private fun loadTrendingRecipes() {
        db.collection("recipes")
            .orderBy("timesSaved", Query.Direction.DESCENDING)
            .limit(4)
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
                        Log.e("TrendingFragment", "Error convirtiendo receta", e)
                    }
                }
                adapter.notifyDataSetChanged()

                showEmptyState(recipesList.isEmpty())
            }
            .addOnFailureListener { exception ->
                Log.e("TrendingFragment", "Error cargando recetas trending", exception)
                Toast.makeText(
                    context,
                    "Error al cargar recetas populares: ${exception.localizedMessage}",
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
