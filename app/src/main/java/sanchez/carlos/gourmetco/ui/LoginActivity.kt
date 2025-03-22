package sanchez.carlos.gourmetco.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import sanchez.carlos.gourmetco.MainActivity
import sanchez.carlos.gourmetco.R

class LoginActivity : AppCompatActivity() {

    private lateinit var signUpTextView: TextView
    private lateinit var loginButton: Button

    private lateinit var email: EditText
    private lateinit var password: EditText

    private lateinit var auth: FirebaseAuth
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        auth = Firebase.auth

        email = findViewById(R.id.et_email)
        password = findViewById(R.id.et_password)

        signUpTextView = findViewById(R.id.registerTextView)
        loginButton = findViewById(R.id.continueButton)

        loginButton.setOnClickListener {
            validateAndLogin()
        }

        signUpTextView.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }
    }

    private fun validateAndLogin() {
        val emailText = email.text.toString().trim()
        val passwordText = password.text.toString()

        if (emailText.isEmpty() || passwordText.isEmpty()) {
            Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show()
            return
        }

        login(emailText, passwordText)
    }

    private fun login(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.let {
                        val uid = it.uid
                        checkUserInFirestore(uid)
                    }
                } else {
                    Log.w("ERROR", "Error en el inicio de sesión", task.exception)
                    Toast.makeText(this, "Credenciales incorrectas", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun checkUserInFirestore(uid: String) {
        db.collection("gourmetco").document("users").get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val fullName = document.getString("fullName") ?: "Usuario"
                    val email = document.getString("email") ?: "Desconocido"

                    Log.d("INFO", "Usuario encontrado: $fullName, $email")

                    Toast.makeText(this, "Bienvenido $fullName", Toast.LENGTH_SHORT).show()
                    goToMain()
                } else {
                    Log.w("ERROR", "Usuario no encontrado en Firestore")
                    Toast.makeText(this, "Error: Usuario no registrado en Firestore", Toast.LENGTH_SHORT).show()
                    auth.signOut()
                }
            }
            .addOnFailureListener { e ->
                Log.w("ERROR", "Error al consultar Firestore", e)
                Toast.makeText(this, "Error al validar usuario", Toast.LENGTH_SHORT).show()
            }
    }

    private fun goToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
}
