package sanchez.carlos.gourmetco.ui.create

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.google.android.flexbox.FlexboxLayout
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import sanchez.carlos.gourmetco.MainActivity
import sanchez.carlos.gourmetco.R
import sanchez.carlos.gourmetco.Recipe
import kotlin.random.Random

class CreateFragment : Fragment() {

    private lateinit var cancel: Button
    private lateinit var save: Button
    private lateinit var btnAdd: TextView
    private lateinit var btnAddCategory: ImageButton
    private lateinit var etNewCategory: EditText
    private lateinit var flexboxSelectedCategories: FlexboxLayout
    private lateinit var flexboxAvailableCategories: FlexboxLayout
    private lateinit var ingredientes: ArrayList<Ingredient>
    private lateinit var categorias: ArrayList<String>
    private lateinit var categoriasSeleccionadas: ArrayList<String>

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var ivPreview: ImageView

    private lateinit var etTitle: EditText
    private lateinit var etDescription: EditText
    private lateinit var etInstructions: EditText
    private lateinit var etLink: EditText
    private lateinit var checkBoxShare: CheckBox

    val REQUEST_IMAGE_GET = 1
    val UPLOAD_PRESET = "recipes-preset"
    var imageUri: Uri? = null

    // for editing...
    private var isEditing = false
    private var currentRecipeId: String? = null
    private var currentPhoto: String? = null
    private lateinit var ingredientAdapter: IngredientAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ingredientes = ArrayList()
        categorias = arrayListOf("Breakfast", "Salty", "Vegan", "Lunch", "Dinner", "Medium")
        categoriasSeleccionadas = ArrayList()

        auth = Firebase.auth
        db = FirebaseFirestore.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_create, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViews(view)
        setupRecyclerView()
        setupButtons()
        setupCategories()

        arguments?.let { bundle ->
            isEditing = bundle.getBoolean("IS_EDIT", false)
            currentRecipeId = bundle.getString("RECIPE_ID")

            if (isEditing) loadRecipeData()
        }

        ingredientAdapter = setupRecyclerView()

    }

    // load recipe data on edit mode
    private fun loadRecipeData() {
        currentRecipeId ?: return

        FirebaseFirestore.getInstance().collection("recipes")
            .document(currentRecipeId!!)
            .get()
            .addOnSuccessListener { document ->
                val recipe = document.toObject(Recipe::class.java) ?: return@addOnSuccessListener

                // set title, desc and isntructrions
                etTitle.setText(recipe.title)
                etDescription.setText(recipe.description)
                etInstructions.setText(recipe.instructions)
                etLink.setText(recipe.link)

                // set infredients
                ingredientes.clear()
                recipe.ingredients.forEach { ingredient ->
                    ingredientes.add(Ingredient(ingredient.name, ingredient.quantity, ingredient.unit))
                }
                ingredientAdapter.notifyDataSetChanged()
                requireView().findViewById<RecyclerView>(R.id.rvIngredients).scrollToPosition(0)

                // set categories
                flexboxSelectedCategories.removeAllViews()
                recipe.categories?.let { categories ->
                    categoriasSeleccionadas.clear()
                    categoriasSeleccionadas.addAll(categories)
                    updateSelectedCategories()
                    updateAvailableCategories()
                }

                // set isshared
                if (recipe.isShared) checkBoxShare.isChecked = true

                // set phto
                recipe.image?.let { uri ->
                    currentPhoto = uri
                    Glide.with(requireContext()).load(uri).into(ivPreview)
                }

            }
            .addOnFailureListener {
                Toast.makeText(context, "Error cargando recetitia del orto", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupViews(view: View) {
        cancel = view.findViewById(R.id.btnCancel)
        save = view.findViewById(R.id.btnSave)
        btnAdd = view.findViewById(R.id.btn_add)
        btnAddCategory = view.findViewById(R.id.btn_add_category)
        etNewCategory = view.findViewById(R.id.et_new_category)
        flexboxSelectedCategories = view.findViewById(R.id.flexbox_selected_categories)
        flexboxAvailableCategories = view.findViewById(R.id.flexbox_available_categories)
        ivPreview = view.findViewById(R.id.ivPreview)

        etTitle = view.findViewById(R.id.etTitle)
        etDescription = view.findViewById(R.id.etDescription)
        etInstructions = view.findViewById(R.id.etInstructions)
        etLink = view.findViewById(R.id.etLink)
        checkBoxShare = view.findViewById(R.id.checkBoxShare)
    }

    private fun setupRecyclerView(): IngredientAdapter {
        val rvIngredients: RecyclerView = requireView().findViewById(R.id.rvIngredients)
        cargarIngredients()

        rvIngredients.layoutManager = LinearLayoutManager(requireContext())
        val adapter = IngredientAdapter(ingredientes) { position ->
            ingredientes.removeAt(position)
            rvIngredients.adapter?.notifyItemRemoved(position)
            rvIngredients.adapter?.notifyItemRangeChanged(position, ingredientes.size)
        }
        rvIngredients.adapter = adapter

        btnAdd.setOnClickListener {
            ingredientes.add(Ingredient("", "", "Pz"))
            adapter.notifyItemInserted(ingredientes.size - 1)
            rvIngredients.smoothScrollToPosition(ingredientes.size - 1)
        }

        return adapter
    }

    private fun setupButtons() {
        cancel.setOnClickListener {
            val intent = Intent(requireActivity(), MainActivity::class.java)
            startActivity(intent)
        }

        save.setOnClickListener {
            saveRecipe()
        }

        val upload: CardView = requireView().findViewById(R.id.card_upload)
        upload.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "image/*"
            }
            startActivityForResult(intent, REQUEST_IMAGE_GET)
        }
    }

    private fun setupCategories() {
        btnAddCategory.setOnClickListener {
            val newCategory = etNewCategory.text.toString().trim()
            if (newCategory.isNotEmpty() && !categorias.contains(newCategory)) {
                categorias.add(newCategory)
                etNewCategory.text.clear()
                updateAvailableCategories()
            }
        }
        updateAvailableCategories()
        updateSelectedCategories()
    }

    // funcion para mostrar las categorias seleccionadas
    private fun updateAvailableCategories() {
        flexboxAvailableCategories.removeAllViews()

        categorias.filter { !categoriasSeleccionadas.contains(it) }.forEach { category ->
            val view = layoutInflater.inflate(R.layout.item_not_selected_category, null).apply {
                findViewById<TextView>(R.id.tv_category).text = category

                layoutParams = FlexboxLayout.LayoutParams(
                    FlexboxLayout.LayoutParams.WRAP_CONTENT,
                    resources.getDimensionPixelSize(R.dimen.category_button_height)
                ).apply {
                    setMargins(5, 0, 5, 10)
                    flexGrow = 0f
                }

                setOnClickListener {
                    categoriasSeleccionadas.add(category)
                    updateAvailableCategories()
                    updateSelectedCategories()
                }
            }

            view.backgroundTintList = resources.getColorStateList(R.color.light_gray)
            flexboxAvailableCategories.addView(view)
        }
    }

    private fun updateSelectedCategories() {
        flexboxSelectedCategories.removeAllViews()

        categoriasSeleccionadas.forEach { category ->
            val view = layoutInflater.inflate(R.layout.item_selected_category, null).apply {
                findViewById<TextView>(R.id.tv_category).text = category

                setOnClickListener {
                    categoriasSeleccionadas.remove(category)
                    updateAvailableCategories()
                    updateSelectedCategories()
                }

                layoutParams = FlexboxLayout.LayoutParams(
                    FlexboxLayout.LayoutParams.WRAP_CONTENT,
                    resources.getDimensionPixelSize(R.dimen.category_button_height)
                ).apply {
                    setMargins(5, 0, 5, 10)
                }
            }
            flexboxSelectedCategories.addView(view)
        }
        Log.d("CreateFragment", "Categorías seleccionadas: $categoriasSeleccionadas")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_GET && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                imageUri = uri
                ivPreview.setImageURI(uri)
            }
        }
    }

    private fun saveRecipe() {
        // For new recipes, validate image. For editing, image is optional
        if (imageUri == null && !isEditing) {
            Toast.makeText(requireContext(), "Selecciona una imagen", Toast.LENGTH_SHORT).show()
            return
        }

        val title = etTitle.text.toString().trim()
        val description = etDescription.text.toString().trim()
        val instructions = etInstructions.text.toString().trim()
        val link = etLink.text.toString().trim()
        val isShared = checkBoxShare.isChecked

        if (title.isEmpty() || instructions.isEmpty()) {
            Toast.makeText(
                requireContext(), "Título e instrucciones son obligatorios", Toast.LENGTH_SHORT
            ).show()
            return
        }

        // If editing and no new image was selected, save with existing image URL
        if (isEditing && imageUri == null) {
            // Assume originalImageUrl is stored when loading the recipe for editing
            saveRecipeToFirestore(
                title, description, instructions, link, currentPhoto!!, isShared
            )
            return
        }

        // Upload new image (for new recipes or when editing with a new image)
        com.cloudinary.android.MediaManager.get().upload(imageUri).unsigned(UPLOAD_PRESET)
            .callback(object : UploadCallback {
                override fun onStart(requestId: String) {}

                override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}

                override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                    val imageUrl = resultData["secure_url"] as String? ?: ""
                    saveRecipeToFirestore(
                        title, description, instructions, link, imageUrl, isShared
                    )
                }

                override fun onError(requestId: String, error: ErrorInfo) {
                    Log.e("Cloudinary", "Upload failed: ${error.description}")
                    Toast.makeText(requireContext(), "Error al subir imagen", Toast.LENGTH_SHORT)
                        .show()
                }

                override fun onReschedule(requestId: String, error: ErrorInfo) {}
            }).dispatch()
    }

    private fun saveRecipeToFirestore(
        title: String,
        description: String,
        instructions: String,
        link: String,
        imageUrl: String,
        isShared: Boolean
    ) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(requireContext(), "No hay usuario autenticado", Toast.LENGTH_SHORT)
                .show()
            return
        }

        db.collection("users").document(currentUser.uid).get().addOnSuccessListener { document ->
            val userName = document?.getString("name") ?: "Usuario"
            val userEmail = document?.getString("email") ?: currentUser.email ?: ""

            val recipe = hashMapOf(
                "title" to title,
                "description" to description,
                "image" to imageUrl,
                "instructions" to instructions,
                "link" to link,
                "isShared" to isShared,
                "author" to userName,
                "authorEmail" to userEmail,
                "userId" to currentUser.uid,
                "categories" to categoriasSeleccionadas,
                "ingredients" to ingredientes.map { it.toMap() },
                "updatedAt" to System.currentTimeMillis()
            )

            // If we're editing, don't overwrite creation data
            if (!isEditing) {
                recipe["createdAt"] = System.currentTimeMillis()
                recipe["calories"] = getRandomCalories()
                recipe["time"] = getRandomMinutes()
            }

            val successMessage = if (isEditing) "Receta actualizada" else "Receta guardada"

            if (isEditing) {
                db.collection("recipes").document(currentRecipeId!!)
                    .update(recipe as Map<String, Any>)
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), successMessage, Toast.LENGTH_SHORT).show()
                        navigateBackToMain()
                    }
                    .addOnFailureListener { e ->
                        handleFirebaseError(e, "actualizar")
                    }
            } else {
                // Create new recipe
                db.collection("recipes").add(recipe)
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), successMessage, Toast.LENGTH_SHORT).show()
                        navigateBackToMain()
                    }
                    .addOnFailureListener { e ->
                        handleFirebaseError(e, "guardar")
                    }
            }
        }.addOnFailureListener { e ->
            Log.e("Firebase", "Error getting user data", e)
            Toast.makeText(
                requireContext(), "Error al obtener datos del usuario", Toast.LENGTH_SHORT
            ).show()
        }
    }

    // Helper method to navigate back to the main activity
    private fun navigateBackToMain() {
        val intent = Intent(requireContext(), MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(intent)
    }

    // Helper method to handle Firebase errors
    private fun handleFirebaseError(e: Exception, action: String) {
        Log.e("Firebase", "Error $action recipe", e)
        Toast.makeText(
            requireContext(), "Error al $action receta", Toast.LENGTH_SHORT
        ).show()
    }

    fun getRandomCalories(): Long {
        return Random.nextLong(100, 800)
    }

    fun getRandomMinutes(): Long {
        return Random.nextLong(5, 120)
    }

    private fun cargarIngredients() {
        if (ingredientes.isEmpty()) {
            ingredientes.add(Ingredient("", "", "Pz"))
        }
    }

    data class Ingredient(
        val name: String, val qty: String, val unit: String
    ) {
        fun toMap(): Map<String, String> {
            return mapOf(
                "name" to name, "quantity" to qty, "unit" to unit
            )
        }
    }

    class IngredientAdapter(
        private val ingredients: ArrayList<Ingredient>, private val onDeleteListener: (Int) -> Unit
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

        override fun onBindViewHolder(
            holder: IngredientViewHolder, @SuppressLint("RecyclerView") position: Int
        ) {
            val ingredient =
                ingredients.getOrNull(position) ?: return  // Protección contra posiciones inválidas

            holder.nameEditText.setText(ingredient.name)
            holder.qtyEditText.setText(ingredient.qty)

            // Configurar el spinner
            ArrayAdapter.createFromResource(
                holder.itemView.context,
                R.array.ingredient_units,
                android.R.layout.simple_spinner_item
            ).also { adapter ->
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                holder.unitSpinner.adapter = adapter
            }

            // Seleccionar la unidad correcta
            val unitPosition =
                (holder.unitSpinner.adapter as? ArrayAdapter<String>)?.getPosition(ingredient.unit)
                    ?: 0
            holder.unitSpinner.setSelection(unitPosition)

            // Listeners seguros
            holder.nameEditText.doAfterTextChanged { text ->
                if (position in 0 until ingredients.size) {
                    ingredients[position] = ingredients[position].copy(name = text.toString())
                }
            }

            holder.qtyEditText.doAfterTextChanged { text ->
                if (position in 0 until ingredients.size) {
                    ingredients[position] = ingredients[position].copy(qty = text.toString())
                }
            }

            holder.unitSpinner.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>?, view: View?, pos: Int, id: Long
                    ) {
                        if (position in 0 until ingredients.size) {
                            val selectedUnit = parent?.getItemAtPosition(pos).toString()
                            ingredients[position] = ingredients[position].copy(unit = selectedUnit)
                        }
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                }

            holder.deleteButton.setOnClickListener {
                if (position in 0 until ingredients.size) {
                    onDeleteListener(position)
                }
            }
        }
    }
}