package sanchez.carlos.gourmetco.ui.home.tabs

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import com.google.android.flexbox.FlexboxLayout
import sanchez.carlos.gourmetco.R
import sanchez.carlos.gourmetco.Recipe
import sanchez.carlos.gourmetco.ui.RecipeAdapter
import sanchez.carlos.gourmetco.ui.detallesreceta.DetallesReceta

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ExploreFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ExploreFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_explore, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recipes = listOf(
            Recipe("Spinach Salad", R.drawable.salad, "165 cal", "15 min", "Carlos Sanchez", listOf("Fast", "Breakfast")),
            Recipe("Avocado Toast", R.drawable.salad, "250 cal", "10 min", "Cristi Castro", listOf("Fast", "Breakfast"))
        )

        val listView = view.findViewById<ListView>(R.id.lvRecipes)
        listView.adapter = RecipeAdapter(requireContext(), recipes)

        listView.setOnItemClickListener { _, _, position, _ ->
            findNavController().navigate(
                R.id.action_navigation_home_to_detallesRecetaFragment,
                bundleOf("recipeId" to 0)
            )
        }

    }

    // para cateogorias
    fun addCategories(flexbox: FlexboxLayout, categories: List<String>, context: Context) {
        flexbox.removeAllViews()

        for (category in categories) {
            val categoryTextView = TextView(context).apply {
                text = category
                setPadding(16, 8, 16, 8)
                setTextColor(Color.WHITE)
                textSize = 12f
                background = ContextCompat.getDrawable(context, R.drawable.rounded_background_etiquetas)
            }

            val params = FlexboxLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(8, 4, 8, 4)
            }

            flexbox.addView(categoryTextView, params)
        }
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ExploreFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ExploreFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
