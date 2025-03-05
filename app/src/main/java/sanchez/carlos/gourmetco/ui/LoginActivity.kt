package sanchez.carlos.gourmetco.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import sanchez.carlos.gourmetco.MainActivity
import sanchez.carlos.gourmetco.R

class LoginActivity : AppCompatActivity() {

    // variables
    lateinit var registerTextView : TextView
    lateinit var continueButton : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        registerTextView = findViewById(R.id.registerTextView)
        continueButton = findViewById(R.id.continueButton)

        // go to register screen
        registerTextView.setOnClickListener{
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }

        // go to home apge
        continueButton.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }


    }
}