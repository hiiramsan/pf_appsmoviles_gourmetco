package sanchez.carlos.gourmetco.ui.detallesreceta

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
import sanchez.carlos.gourmetco.ui.create.Ingredient

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [DetallesReceta.newInstance] factory method to
 * create an instance of this fragment.
 */
class DetallesReceta : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    var ingredients = ArrayList<Ingredient>()

    //  recipe id
    private lateinit var recipeId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            recipeId = it.getString("recipeId") ?: ""
        }

        agregarIngrediente()

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_detalles_receta, container, false)
        val listView = view.findViewById<ListView>(R.id.listView_ingredientes)

        val adaptador = AdaptadorProductos(requireContext(), ingredients)
        listView.adapter = adaptador

        return view
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment DetallesReceta.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            DetallesReceta().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }


    fun agregarIngrediente(){
        ingredients.add(Ingredient("Raw spinach", "1", "cup"))
        ingredients.add(Ingredient("Dried Cranberries", "24", "grams"))
    }
    private class AdaptadorProductos: BaseAdapter {
        var ingredients = java.util.ArrayList<Ingredient>()

        var contexto: Context? = null

        constructor(contexto: Context, ingredients:ArrayList<Ingredient>){
            this.ingredients = ingredients
            this.contexto = contexto
        }

        override fun getCount(): Int {
            return ingredients.size
        }

        override fun getItem(position: Int): Any {
            return ingredients[position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            var ing = ingredients[position]
            var inflador = LayoutInflater.from(contexto)
            var vista = inflador.inflate(R.layout.ingredientes_view, null)

            var nombre = vista.findViewById(R.id.ing_nombre) as TextView
            var qty = vista.findViewById(R.id.ing_cantidad) as TextView
            var unit = vista.findViewById(R.id.ing_medida) as TextView

            nombre.setText(ing.name)
            qty.setText(ing.qty)
            unit.setText(ing.unit)

            return vista
        }

    }
}