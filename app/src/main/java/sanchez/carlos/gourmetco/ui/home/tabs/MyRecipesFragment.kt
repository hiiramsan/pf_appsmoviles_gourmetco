package sanchez.carlos.gourmetco.ui.home.tabs

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import sanchez.carlos.gourmetco.R
import sanchez.carlos.gourmetco.Recipe

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
        return inflater.inflate(R.layout.fragment_my_recipes, container, false)

        val recipes = listOf(
            Recipe("Spinach Salad", R.drawable.salad, "165 cal", "15 min", "Carlos Sanchez"),
            Recipe("Avocado Toast", R.drawable.salad, "250 cal", "10 min", "Cristi Castro")
        )

        val listView = view?.findViewById<ListView>(R.id.lvRecipes)
        listView?.adapter = RecipeAdapter(requireContext(), recipes)

    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment MyRecipesFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MyRecipesFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}

class RecipeAdapter(private val context: Context, private val recipes: List<Recipe>) : BaseAdapter() {

    override fun getCount(): Int = recipes.size

    override fun getItem(position: Int): Any = recipes[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_recipe, parent, false)

        val recipe = recipes[position]
        val title = view.findViewById<TextView>(R.id.tvRecipeName)
        val image = view.findViewById<ImageView>(R.id.ivRecipeImg)
        val calories = view.findViewById<TextView>(R.id.tvCalories)
        val time = view.findViewById<TextView>(R.id.tvTime)
        val author = view.findViewById<TextView>(R.id.tvAuthor)

        title.text = recipe.title
        image.setImageResource(recipe.image)
        calories.text = recipe.calories
        time.text = recipe.time
        author.text = recipe.author

        return view
    }
}
