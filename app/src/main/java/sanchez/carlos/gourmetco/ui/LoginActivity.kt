package sanchez.carlos.gourmetco.ui

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import sanchez.carlos.gourmetco.R

class LoginActivity : AppCompatActivity() {

    // variables
    lateinit var registerTextView : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        registerTextView = findViewById(R.id.registerTextView)

        // go to register screen
        registerTextView.setOnClickListener{
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }


    }
}