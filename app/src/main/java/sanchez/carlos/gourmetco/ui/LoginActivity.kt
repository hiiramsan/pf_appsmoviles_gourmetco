package sanchez.carlos.gourmetco.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import sanchez.carlos.gourmetco.MainActivity
import sanchez.carlos.gourmetco.R

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    // variables
    private lateinit var registerTextView : TextView
    private lateinit var continueButton : Button

    // auth
    private lateinit var email : EditText
    private lateinit var password : EditText

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        // auth
        auth = Firebase.auth
        onStart()

        email = findViewById(R.id.et_email)
        password = findViewById(R.id.et_password)
        continueButton = findViewById(R.id.continueButton)
        registerTextView = findViewById(R.id.registerTextView)

        // go to home apge
        continueButton.setOnClickListener {
            login(email.text.toString(), password.text.toString())
        }

        // go to register screen
        registerTextView.setOnClickListener{
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }
    }

    private fun login(email:String, password: String){
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this)
            { task ->
                if(task.isSuccessful){
                    val user = auth.currentUser
                    user?.let {
                        guardarDatosUser(it)
                        goToMain(it)
                    }
                   // showError(visible = false)
                    goToMain(user!!)
                } else {
                   // showError("Usuario y/o contraseña equivocados", true)
                }
            }
    }

    private fun guardarDatosUser(user: FirebaseUser) {
        val userData = hashMapOf(
            "email" to user.email,
            "uid" to user.uid
        )
        db.collection("users").document(user.uid).set(userData)
            .addOnSuccessListener {
                // Datos guardados correctamente
            }
            .addOnFailureListener {
                // Error al guardar datos
            }
    }

    /*
    private fun showError(text: String = "", visible:Boolean){
        val errorTv: TextView = findViewById(R.id.tvError)
        errorTv.text = text
        errorTv.visibility = if (visible) View.VISIBLE else View.GONE
    }
    */

    public override fun onStart(){
        super.onStart()
        val currentUser = auth.currentUser
        if(currentUser != null){
            goToMain(currentUser)
        }
    }

    private fun goToMain(user: FirebaseUser){
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("user", user.email)
        intent.putExtra("uid", user.uid)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }
}