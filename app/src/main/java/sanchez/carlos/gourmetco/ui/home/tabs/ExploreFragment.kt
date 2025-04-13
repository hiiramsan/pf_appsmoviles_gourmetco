package sanchez.carlos.gourmetco.ui.home.tabs

import android.content.Context
import android.content.Intent
import android.graphics.Color
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
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import com.google.android.flexbox.FlexboxLayout
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import sanchez.carlos.gourmetco.R
import sanchez.carlos.gourmetco.Recipe
import sanchez.carlos.gourmetco.ui.RecipeAdapter
import sanchez.carlos.gourmetco.ui.detallesreceta.DetallesReceta


class ExploreFragment : Fragment() {

    private lateinit var listView: ListView
    private lateinit var recipeAdapter: RecipeAdapter
    private var recipes = mutableListOf<Recipe>()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_explore, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        listView = view.findViewById(R.id.lvRecipes)
        recipeAdapter = RecipeAdapter(requireContext(), recipes)
        listView.adapter = recipeAdapter
        loadSharedRecipes()

    }

    private fun loadSharedRecipes() {
        db.collection("recipes")
            .whereEqualTo("isShared", true)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                recipes.clear()
                for (document in documents) {
                    val recipe = document.toObject(Recipe::class.java)
                    recipes.add(recipe)
                }
                recipeAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error cargando recetas COMPARTIDAS", e)
                Toast.makeText(requireContext(), "Error cargando recetas COMPRATIDAS", Toast.LENGTH_SHORT).show()
            }
    }


}
