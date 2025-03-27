package sanchez.carlos.gourmetco.ui.bookmark

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.core.os.bundleOf
import sanchez.carlos.gourmetco.R
import sanchez.carlos.gourmetco.Recipe
import androidx.navigation.fragment.findNavController
import sanchez.carlos.gourmetco.ui.RecipeAdapter

class BookmarkFragment : Fragment() {

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


        val bookmarkedRecipes = listOf(
            Recipe(
                "Spinach Salad",
                R.drawable.salad,
                "165 cal",
                "15 min",
                "Carlos Sanchez",
                listOf("Fast", "Breakfast")
            ),
            Recipe(
                "Avocado Toast",
                R.drawable.salad,
                "250 cal",
                "10 min",
                "Cristi Castro",
                listOf("Fast", "Breakfast")
            )
        )

        val listView = view.findViewById<ListView>(R.id.lvRecipes)
        listView.adapter = RecipeAdapter(requireContext(), bookmarkedRecipes)

        listView.setOnItemClickListener { _, _, position, _ ->
            findNavController().navigate(
                R.id.action_navigation_home_to_detallesRecetaFragment,
                bundleOf("recipeId" to 0)
            )
        }
    }
}