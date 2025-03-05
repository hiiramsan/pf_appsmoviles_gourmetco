package sanchez.carlos.gourmetco.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import sanchez.carlos.gourmetco.MainActivity
import sanchez.carlos.gourmetco.R

class SignUpActivity : AppCompatActivity() {

    // variables
    lateinit var loginTextView : TextView
    lateinit var continueButton : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sign_up)

        loginTextView = findViewById(R.id.loginTextView)
        continueButton = findViewById(R.id.continueButton)

        // go to register screen
        loginTextView.setOnClickListener{
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        continueButton.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

    }
}