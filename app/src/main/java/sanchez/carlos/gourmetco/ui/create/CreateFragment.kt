package sanchez.carlos.gourmetco.ui.create

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import sanchez.carlos.gourmetco.MainActivity
import sanchez.carlos.gourmetco.R

class CreateFragment : Fragment() {

    private lateinit var cancel: Button
    private lateinit var save: Button
    private lateinit var btnAdd: TextView
    private lateinit var ingredientes: ArrayList<Ingredient>

    // inicializar firestore things
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var ivPreview: ImageView


    // campos
    private lateinit var etTitle : EditText
    private lateinit var etDescription : EditText
    private lateinit var etInstructions : EditText
    private lateinit var etLink : EditText
    private lateinit var checkBoxShare : CheckBox
    // falta ingredientes


    // cloudinary setup
    val REQUEST_IMAGE_GET = 1
    val CLOUD_NAME = "dvznvnzam"
    val UPLOAD_PRESET = "recipes-preset"
    var imageUri: Uri? = null
    // bool pa checar si Cloudinary ha sido inicializado
    private var isCloudinaryInitialized = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ingredientes = ArrayList()

        // setear firestore things
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // inicializar cloudinary
        initCloudinary()

    }


    fun initCloudinary() {
        if (!isCloudinaryInitialized) {
            val config: MutableMap<String, String> = HashMap<String, String>()
            config["cloud_name"] = CLOUD_NAME
            MediaManager.init(requireContext(), config)
            isCloudinaryInitialized = true
        }
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

        // setear elementos layout
        cancel = view.findViewById(R.id.btnCancel)
        save = view.findViewById(R.id.btnSave)
        etTitle = view.findViewById(R.id.etTitle)
        etDescription= view.findViewById(R.id.etDescription)
        etInstructions= view.findViewById(R.id.etInstructions)
        etLink = view.findViewById(R.id.etLink)
        checkBoxShare = view.findViewById(R.id.checkBoxShare)

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
            saveRecipe()
            val intent = Intent(requireContext(), MainActivity::class.java)
            startActivity(intent)
        }

        // MANEJAR SUBIDA DE IMAGENES
        val upload : CardView = view.findViewById(R.id.card_upload)
        ivPreview = view.findViewById(R.id.ivPreview)
        upload.setOnClickListener {
            val intent: Intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            startActivityForResult(intent, REQUEST_IMAGE_GET)
        }


    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_GET && resultCode == Activity.RESULT_OK) {
            val fullPhotoUri: Uri? = data?.data

            if(fullPhotoUri != null) {
                imageUri = fullPhotoUri
                changeImage(fullPhotoUri)
            }
        }
    }

    fun saveRecipe() {
        if (imageUri != null) {
            MediaManager.get().upload(imageUri).unsigned(UPLOAD_PRESET).callback(object :
                UploadCallback {
                override fun onStart(requestId: String) {}

                override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}

                override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                    val url = resultData["secure_url"] as String? ?: ""
                    Log.d("Cloudinary", "Upload success: $url")

                    val title = etTitle.text.toString().trim()
                    val instructions = etInstructions.text.toString().trim()
                    val isShared = checkBoxShare.isChecked

                    // Validaciones básicas
                    if (title.isNotEmpty() && instructions.isNotEmpty()) {

                        val currentUser = Firebase.auth.currentUser
                        var userName : String = ""

                        if (currentUser != null) {
                            val uid = currentUser.uid
                            db.collection("users").document(uid)
                                .get()
                                .addOnSuccessListener { document ->
                                    if (document != null && document.exists()) {
                                        userName = document.getString("name") ?: "Usuario"
                                    } else {
                                        Log.d("CreateFragment", "No se encontraron datos del usuario")
                                    }
                                }
                            }

                        val recipe = mapOf(
                            "title" to title,
                            "image" to url,
                            "calories" to "0",
                            "time" to "0 min",
                            "author" to userName,
                            "isShared" to isShared,
                            "instructions" to instructions,
                            "ingredients" to emptyList<String>(), // No implementado aun, tala hazlo
                            "categories" to emptyList<String>(), // No implementado aun, tala hazlo
                            "userId" to (auth.currentUser?.uid ?: ""),
                            "createdAt" to System.currentTimeMillis()
                        )

                        db.collection("recipes").add(recipe)
                            .addOnSuccessListener {
                                Log.d("Firebase", "Recipe saved successfully")
                                Toast.makeText(requireContext(), "Receta guardada", Toast.LENGTH_SHORT).show()
                                requireActivity().finish()
                            }
                            .addOnFailureListener { e ->
                                Log.e("Firebase", "Error saving recipe", e)
                            }
                    } else {
                        Log.e("Validation", "Title or instructions are empty")
                        Toast.makeText(requireContext(), "Título e instrucciones son obligatorios", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onError(requestId: String, error: ErrorInfo) {
                    Log.d("Cloudinary", "Upload failed: ${error.description}")
                    Toast.makeText(requireContext(), "Error al subir imagen", Toast.LENGTH_SHORT).show()
                }

                override fun onReschedule(requestId: String, error: ErrorInfo) {}

            }).dispatch()
        } else {
            Log.e("Image", "No image selected")
            Toast.makeText(requireContext(), "Debes seleccionar una imagen", Toast.LENGTH_SHORT).show()
        }
    }


    fun changeImage(uri: Uri) {
        try {
            ivPreview.setImageURI(uri)
        } catch (e: Exception) {
            e.printStackTrace()
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