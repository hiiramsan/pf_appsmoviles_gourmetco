package sanchez.carlos.gourmetco.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import sanchez.carlos.gourmetco.MainActivity
import sanchez.carlos.gourmetco.R

class SignUpActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var name: EditText
    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var confirmPassword: EditText
    private lateinit var continueButton: Button

    private lateinit var loginTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        // Inicializar Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        db.firestoreSettings =
            FirebaseFirestoreSettings.Builder().setPersistenceEnabled(true).build()

        // Inicializar componentes
        name = findViewById(R.id.et_fullName)
        email = findViewById(R.id.et_email)
        password = findViewById(R.id.et_password)
        confirmPassword = findViewById(R.id.et_confirmPassword)
        continueButton = findViewById(R.id.continueButtonSU)
        loginTextView = findViewById(R.id.loginTextView)

        continueButton.setOnClickListener {
            validateAndSignUp()
        }

        loginTextView.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }

    private fun validateAndSignUp() {
        val fullNameText = name.text.toString().trim()
        val emailText = email.text.toString().trim()
        val passwordText = password.text.toString().trim()
        val confirmPasswordText = confirmPassword.text.toString().trim()

        if (fullNameText.isEmpty() || emailText.isEmpty() || passwordText.isEmpty() || confirmPasswordText.isEmpty()) {
            Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show()
            return
        }

        if (passwordText != confirmPasswordText) {
            Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
            return
        }

        // Registrar usuario en Firebase Authentication
        auth.createUserWithEmailAndPassword(emailText, passwordText).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val user = auth.currentUser
                if (user != null) {
                    Log.d("AUTH_DEBUG", "Usuario creado en Auth - UID: ${user.uid}")
                    saveUserToFirestore(user.uid, fullNameText, emailText)
                } else {
                    Log.e("AUTH_ERROR", "Usuario Auth es null")
                }
            }
        }
    }

    private fun saveUserToFirestore(uid: String, fullName: String, email: String) {
        val user = hashMapOf(
            "id" to uid, "name" to fullName, "email" to email
        )

        Log.d("DEBUG", "Intentando guardar usuario en Firestore: $user")

        db.collection("users").document(uid).set(user).addOnSuccessListener {
            Log.d("SUCCESS", "Usuario guardado en Firestore con ID: $uid")
            Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show()
            goToMain()
        }.addOnFailureListener { e ->
            Log.e("ERROR", "Error al guardar usuario en Firestore", e)
            Toast.makeText(
                this, "Error al guardar en Firestore: ${e.message}", Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun goToMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
