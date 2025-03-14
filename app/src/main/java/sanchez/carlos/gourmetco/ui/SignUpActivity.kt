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

class SignUpActivity : AppCompatActivity() {

    // variables
    lateinit var loginTextView : TextView
    lateinit var continueButton : Button

    // auth
    private lateinit var name : EditText
    private lateinit var email : EditText
    private lateinit var password : EditText
    private lateinit var confirmPassword : EditText

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sign_up)

        // auth
        auth = Firebase.auth

        name = findViewById(R.id.et_fullName)
        email = findViewById(R.id.et_email)
        password = findViewById(R.id.et_password)
        confirmPassword = findViewById(R.id.et_confirmPassword)

        // variables
        loginTextView = findViewById(R.id.loginTextView)
        continueButton = findViewById(R.id.continueButton)

        continueButton.setOnClickListener{
            if(email.text.isEmpty() || password.text.isEmpty() || confirmPassword.text.isEmpty()){
               /*
                errorTv.text = "Todos los campos deben de ser llenados"
                errorTv.visibility = View.VISIBLE
                */
            } else if (!password.text.toString().equals(confirmPassword.text.toString())){
                /*
                errorTv.text = "Las contraseñas no coinciden"
                errorTv.visibility = View.VISIBLE
                 */
            } else {
                /*
                errorTv.visibility = View.INVISIBLE
                 */
                signIn(email.text.toString(), password.text.toString())
            }
        }

        // go to register screen
        loginTextView.setOnClickListener{
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }

    private fun signIn(email:String, password: String){
        Log.d("INFO", "email: ${email}, password: ${password}")

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if(task.isSuccessful){
                    Log.d("INFO", "signInWithEmail:success")
                    val user = auth.currentUser
                    val intent = Intent(this, MainActivity::class.java)
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
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
}