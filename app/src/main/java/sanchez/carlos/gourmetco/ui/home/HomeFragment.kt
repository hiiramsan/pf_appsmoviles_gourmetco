package sanchez.carlos.gourmetco.ui.home

import android.os.Bundle
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

    // obtener usuario para poner saludo en Home
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

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

        // metodo para cargar info
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
        val user = auth.currentUser
        user?.let {
            val uid = it.uid
            db.collection("users").document(uid).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val fullName = document.getString("fullName") ?: "User"
                        binding.tvGreeting.text = "Hi, $fullName"
                    } else {
                        binding.tvGreeting.text = "Hi, User"
                    }
                }
                .addOnFailureListener {
                    binding.tvGreeting.text = "Hi, User"
                }
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
