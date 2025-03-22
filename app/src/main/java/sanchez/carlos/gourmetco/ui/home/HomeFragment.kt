package sanchez.carlos.gourmetco.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import sanchez.carlos.gourmetco.databinding.FragmentHomeBinding
import sanchez.carlos.gourmetco.ui.home.tabs.ExploreFragment
import sanchez.carlos.gourmetco.ui.home.tabs.MyRecipesFragment

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // cargar datos del user
        loadUserData()

        // Configurar el ViewPager2 con el adapter
        val adapter = ViewPagerAdapter(requireActivity())
        binding.viewPager.adapter = adapter

        // Conectar TabLayout con ViewPager2
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Explore"
                1 -> "My Recipes"
                else -> ""
            }
        }.attach()
    }

    private fun loadUserData() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val uid = currentUser.uid
            db.collection("users").document(uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val userName = document.getString("name") ?: "User"
                        binding.tvGreeting.text = "Hi, $userName!"
                    } else {
                        binding.tvGreeting.text = "Hi there!"
                        Log.d("HomeFragment", "No se encontraron datos del usuario")
                    }
                }
                .addOnFailureListener { e ->
                    binding.tvGreeting.text = "Hi there!"
                    Log.e("HomeFragment", "Error al cargar datos del usuario", e)
                }
        } else {

            binding.tvGreeting.text = "Hi there!"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class ViewPagerAdapter(fragmentActivity: FragmentActivity) :
    FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> ExploreFragment()
            1 -> MyRecipesFragment()
            else -> throw IllegalStateException("Invalid position")
        }
    }
}
