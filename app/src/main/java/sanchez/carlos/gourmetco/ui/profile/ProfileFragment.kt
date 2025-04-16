package sanchez.carlos.gourmetco.ui.profile

import android.app.AlertDialog
import android.content.Intent
import androidx.fragment.app.viewModels
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import sanchez.carlos.gourmetco.R
import sanchez.carlos.gourmetco.ui.LoginActivity

import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import sanchez.carlos.gourmetco.MainActivity
import sanchez.carlos.gourmetco.Recipe
import sanchez.carlos.gourmetco.ui.ProfileRecipeAdapter

class ProfileFragment : Fragment() {

    private lateinit var back: ImageButton
    private lateinit var btnLogOut: Button
    private lateinit var tvUsername: TextView
    private lateinit var tvMail: TextView
    private lateinit var lvRecipes: ListView
    private lateinit var db: FirebaseFirestore
    private var recipesList = mutableListOf<Recipe>()
    private var allRecipes = mutableListOf<Recipe>()
    private lateinit var profileAdapter: ProfileRecipeAdapter
    private var currentUserId: String? = null

    companion object {
        fun newInstance() = ProfileFragment()
    }

    private val viewModel: ProfileViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        back = view.findViewById(R.id.btn_back)
        btnLogOut = view.findViewById(R.id.btn_logOut)
        tvUsername = view.findViewById(R.id.tvUsername)
        tvMail = view.findViewById(R.id.tvMail)
        lvRecipes = view.findViewById(R.id.lvRecipes)

        db = FirebaseFirestore.getInstance()

        // Inicializar la lista de recetas y el adaptador
        recipesList = mutableListOf()
        val currentUserId = Firebase.auth.currentUser?.uid
        profileAdapter = ProfileRecipeAdapter(
            requireContext(),
            recipesList,
            currentUserId,
            onEditClick = { recipe -> editRecipe(recipe) },
            onDeleteClick = { recipe -> deleteRecipe(recipe) }
        )
        lvRecipes.adapter = profileAdapter

        // Cargar datos
        loadUserData()
        loadUserRecipes()

        back.setOnClickListener {
            val intent = Intent(requireContext(), MainActivity::class.java)
            startActivity(intent)
        }

        btnLogOut.setOnClickListener {
            Firebase.auth.signOut()
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    private fun loadUserData() {
        val currentUser = Firebase.auth.currentUser
        if (currentUser != null) {
            tvMail.text = currentUser.email
            val uid = currentUser.uid

            db.collection("users").document(uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val userName = document.getString("name") ?: "Usuario"
                        tvUsername.text = userName
                    } else {
                        tvUsername.text = "Usuario"
                        Log.d("ProfileFragment", "No se encontraron datos del usuario")
                    }
                }
                .addOnFailureListener { e ->
                    tvUsername.text = "Usuario"
                    Log.e("ProfileFragment", "Error al cargar datos del usuario", e)
                }
        } else {
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    private fun loadUserRecipes() {
        currentUserId = Firebase.auth.currentUser?.uid
        val uid = currentUserId
        if (uid.isNullOrEmpty()) return

        db.collection("recipes")
            .whereEqualTo("userId", uid)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                recipesList.clear()
                allRecipes.clear()
                for (document in documents) {
                    try {
                        val recipe = document.toObject(Recipe::class.java).apply {
                            id = document.id
                        }
                        recipesList.add(recipe)
                        allRecipes.add(recipe)
                    } catch (e: Exception) {
                        Log.e("ProfileFragment", "Error converting document: ${document.id}", e)
                    }
                }
                profileAdapter.notifyDataSetChanged()

                if (recipesList.isEmpty()) {
                    Toast.makeText(context, "No tienes recetas guardadas", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Log.w("ProfileFragment", "Error loading recipes", exception)
                Toast.makeText(context, "Error al cargar recetas: ${exception.localizedMessage}", Toast.LENGTH_LONG).show()
            }
    }


    private fun editRecipe(recipe: Recipe) {
        // Implementar la lógica para editar la receta
    }

    private fun deleteRecipe(recipe: Recipe) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Eliminar receta")
            .setMessage("¿Estás seguro que deseas eliminar esta receta?")
            .setPositiveButton("Eliminar") { _, _ ->
                db.collection("recipes").document(recipe.id)
                    .delete()
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "Receta eliminada", Toast.LENGTH_SHORT).show()
                        recipesList.remove(recipe)
                        profileAdapter.notifyDataSetChanged()
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(), "Error al eliminar", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}