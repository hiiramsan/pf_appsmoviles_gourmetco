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
import sanchez.carlos.gourmetco.MainActivity
import sanchez.carlos.gourmetco.R

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore

class SignUpActivity : AppCompatActivity() {
  // variables
    lateinit var loginTextView: TextView
    lateinit var continueButton: Button

    // auth
    private lateinit var name: EditText
    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var confirmPassword: EditText

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sign_up)

        // auth
        auth = Firebase.auth
        db = FirebaseFirestore.getInstance()

        name = findViewById(R.id.et_fullName)
        email = findViewById(R.id.et_email)
        password = findViewById(R.id.et_password)
        confirmPassword = findViewById(R.id.et_confirmPassword)

        // variables
        loginTextView = findViewById(R.id.loginTextView)
        continueButton = findViewById(R.id.continueButton)

        continueButton.setOnClickListener{
            if(email.text.isEmpty() || password.text.isEmpty() || confirmPassword.text.isEmpty() || name.text.isEmpty()){
                Toast.makeText(this, "Todos los campos deben ser llenados", Toast.LENGTH_SHORT).show()
            } else if (!password.text.toString().equals(confirmPassword.text.toString())){
                Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
            } else {
                signUp(email.text.toString(), password.text.toString(), name.text.toString())
            }
        }

        // go to register screen
        loginTextView.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }

    private fun signUp(email: String, password: String, fullName: String) {
        Log.d("INFO", "email: ${email}, password: ${password}")

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d("INFO", "signInWithEmail:success")
                    val user = auth.currentUser
                    user?.let {
                        guardarDatosUser(it.uid, fullName, email)
                        goToMain(it.uid, email)
                    }
                } else {
                    Log.w("ERROR", "signInWithEmail:failure", task.exception)
                    Toast.makeText(
                        baseContext,
                        "El registro falló.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun guardarDatosUser(uid: String, fullName: Any, email: String) {
        val user = hashMapOf(
            "uid" to uid,
            "fullName" to fullName,
            "email" to email
        )
        db.collection("users").document(uid).set(user)
            .addOnSuccessListener {
                Log.d("INFO", "Usuario guardado en Firestore")
            }
            .addOnFailureListener { e ->
                Log.w("ERROR", "Error guardando usuario en Firestore", e)
            }
    }

    private fun goToMain(uid: String, email: String) {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("user", email)
        intent.putExtra("uid", uid)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }
}

    