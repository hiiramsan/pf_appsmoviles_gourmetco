package sanchez.carlos.gourmetco.ui.profile

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
import android.widget.TextView
import sanchez.carlos.gourmetco.R
import sanchez.carlos.gourmetco.ui.LoginActivity

import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import sanchez.carlos.gourmetco.MainActivity

class ProfileFragment : Fragment() {

    private lateinit var back : ImageButton
    private lateinit var btnLogOut : Button
    private lateinit var tvUsername : TextView
    private lateinit var tvMail : TextView
    private lateinit var db: FirebaseFirestore

    companion object {
        fun newInstance() = ProfileFragment()
    }

    private val viewModel: ProfileViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
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

        db = FirebaseFirestore.getInstance()
        loadUserData()

        back.setOnClickListener {
            val intent = Intent(requireContext(), MainActivity::class.java)
            startActivity(intent)
        }

        btnLogOut.setOnClickListener({
            Firebase.auth.signOut()
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        })
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
}