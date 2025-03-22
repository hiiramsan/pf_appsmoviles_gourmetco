package sanchez.carlos.gourmetco.ui.create

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import sanchez.carlos.gourmetco.MainActivity
import sanchez.carlos.gourmetco.R

class CreateFragment : Fragment() {

    private lateinit var cancel: Button
    private lateinit var save: Button
    private lateinit var btnAdd: TextView
    private lateinit var ingredientes: ArrayList<Ingredient>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ingredientes = ArrayList()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_create, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rvIngredients: RecyclerView = view.findViewById(R.id.rvIngredients)
        btnAdd = view.findViewById(R.id.btn_add)

        cargarIngredients()

        rvIngredients.layoutManager = LinearLayoutManager(requireContext())
        val adapter = IngredientAdapter(ingredientes) { position ->
            ingredientes.removeAt(position)
            rvIngredients.adapter?.notifyItemRemoved(position)
            rvIngredients.adapter?.notifyItemRangeChanged(position, ingredientes.size)
        }
        rvIngredients.adapter = adapter

        cancel = view.findViewById(R.id.btnCancel)
        save = view.findViewById(R.id.btnSave)

        btnAdd.setOnClickListener {
            ingredientes.add(Ingredient("", "", "Pz"))
            adapter.notifyItemInserted(ingredientes.size - 1)
            rvIngredients.smoothScrollToPosition(ingredientes.size - 1)
        }

        cancel.setOnClickListener {
            val intent = Intent(requireContext(), MainActivity::class.java)
            startActivity(intent)
        }

        save.setOnClickListener {
            val intent = Intent(requireContext(), MainActivity::class.java)
            startActivity(intent)
        }
    }

    private fun cargarIngredients() {
        ingredientes.add(Ingredient("", "", "Pz"))
    }

    class IngredientAdapter(
        private val ingredients: ArrayList<Ingredient>,
        private val onDeleteListener: (Int) -> Unit
    ) : RecyclerView.Adapter<IngredientAdapter.IngredientViewHolder>() {

        class IngredientViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val nameEditText: EditText = view.findViewById(R.id.etIngredientName)
            val qtyEditText: EditText = view.findViewById(R.id.etIngredientQty)
            val unitSpinner: Spinner = view.findViewById(R.id.spIngredientUnit)
            val deleteButton: ImageButton = view.findViewById(R.id.btnDeleteIngredient)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IngredientViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_ingredients, parent, false)
            return IngredientViewHolder(view)
        }

        override fun getItemCount(): Int = ingredients.size

        override fun onBindViewHolder(holder: IngredientViewHolder, position: Int) {
            val ingredient = ingredients[position]

            holder.nameEditText.setText(ingredient.name)
            holder.qtyEditText.setText(ingredient.qty.toString())

            val units = arrayOf("Pz", "Sp", "Cup")
            val spinnerAdapter = ArrayAdapter(
                holder.itemView.context,
                android.R.layout.simple_spinner_item, units
            )
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            holder.unitSpinner.adapter = spinnerAdapter

            val unitPosition = units.indexOf(ingredient.unit)
            if (unitPosition >= 0) {
                holder.unitSpinner.setSelection(unitPosition)
            }

            holder.deleteButton.setOnClickListener {
                onDeleteListener(position)
            }
        }
    }
}