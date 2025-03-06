package sanchez.carlos.gourmetco

import android.os.Bundle
import android.view.View
import android.widget.ScrollView
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import sanchez.carlos.gourmetco.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_activity_main)

        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_trending, R.id.nav_bookmark, R.id.nav_create, R.id.nav_profile
            )
        )

        navView.setupWithNavController(navController)

        // Escuchar cambios en los fragmentos
        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.nav_home) {
                binding.scrollView2.visibility = View.VISIBLE  // Mostrar en Home
            } else {
                binding.scrollView2.visibility = View.GONE  // Ocultar en otros fragmentos
            }
        }
    }
}
