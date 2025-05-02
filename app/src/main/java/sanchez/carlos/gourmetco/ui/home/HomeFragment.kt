package sanchez.carlos.gourmetco.ui.home

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ListView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import sanchez.carlos.gourmetco.R
import sanchez.carlos.gourmetco.databinding.FragmentHomeBinding
import sanchez.carlos.gourmetco.ui.RecipeAdapter
import sanchez.carlos.gourmetco.ui.home.tabs.ExploreFragment
import sanchez.carlos.gourmetco.ui.home.tabs.MyRecipesFragment

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var viewPagerAdapter: ViewPagerAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupCategoryToggleListeners()

        // toggle visibility de las categorias con el btnFilter
        binding.categoriesContainer.visibility = View.GONE

        binding.btnFilter.setOnClickListener {
            if (binding.categoriesContainer.visibility == View.VISIBLE) {
                binding.categoriesContainer.visibility = View.GONE
            } else {
                binding.categoriesContainer.visibility = View.VISIBLE
            }
        }

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // cargar datos del user
        loadUserData()

        // Configurar el ViewPager2 con el adapter
        viewPagerAdapter = ViewPagerAdapter(requireActivity())
        binding.viewPager.adapter = viewPagerAdapter

        // Conectar TabLayout con ViewPager2
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Explore"
                1 -> "My Recipes"
                else -> ""
            }
        }.attach()

        // Configurar el botón de búsqueda
        setupSearchButton()
        setupSearch()
    }

    private fun searchInExploreFragment(fragment: ExploreFragment, query: String) {
        fragment.applySearchFilter(query)
    }

    private fun searchInMyRecipesFragment(fragment: MyRecipesFragment, query: String) {
        fragment.applySearchFilter(query)
    }

    private fun setupSearch() {
        binding.ivSearch.setOnClickListener {
            performSearch(binding.etSearch.text.toString().trim())
        }
        binding.etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch(binding.etSearch.text.toString().trim())
                true
            } else false
        }
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { performSearch(s.toString().trim()) }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun setupSearchButton() {
        binding.ivSearch.setOnClickListener {
            val query = binding.etSearch.text.toString().trim()
            performSearch(query)

            val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(binding.etSearch.windowToken, 0)
        }

        binding.etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
                val query = binding.etSearch.text.toString().trim()
                performSearch(query)

                val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding.etSearch.windowToken, 0)
                return@setOnEditorActionListener true
            }
            false
        }
    }

    private fun performSearch(query: String) {
        val currentPosition = binding.viewPager.currentItem
        val currentFragment = viewPagerAdapter.getFragment(currentPosition)

        when (currentFragment) {
            is ExploreFragment -> searchInExploreFragment(currentFragment, query)
            is MyRecipesFragment -> searchInMyRecipesFragment(currentFragment, query)
        }
    }

    private fun searchInExploreFragmentOLD(fragment: ExploreFragment, query: String) {
        val listView = fragment.view?.findViewById<ListView>(R.id.lvRecipes)
        val adapter = listView?.adapter as? RecipeAdapter

        if (adapter != null) {
            val allRecipes = fragment.getAllRecipes() ?: return

            if (query.isEmpty()) {
                adapter.clear()
                adapter.addAll(allRecipes)
            } else {
                val filteredRecipes = allRecipes.filter { recipe ->
                    recipe.title.contains(query, ignoreCase = true)
                }
                adapter.clear()
                adapter.addAll(filteredRecipes)
            }
            adapter.notifyDataSetChanged()
        }
    }

    private fun searchInMyRecipesFragmentOLD(fragment: MyRecipesFragment, query: String) {
        val listView = fragment.view?.findViewById<ListView>(R.id.lvRecipes)
        val adapter = listView?.adapter as? RecipeAdapter

        if (adapter != null) {
            val allRecipes = fragment.getAllRecipes() ?: return

            if (query.isEmpty()) {
                adapter.clear()
                adapter.addAll(allRecipes)
            } else {
                val filteredRecipes = allRecipes.filter { recipe ->
                    recipe.title.contains(query, ignoreCase = true)
                }
                adapter.clear()
                adapter.addAll(filteredRecipes)
            }
            adapter.notifyDataSetChanged()
        }
    }

    private fun loadUserData() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val uid = currentUser.uid
            db.collection("users").document(uid).get().addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val userName = document.getString("name") ?: "User"
                    binding.tvGreeting.text = "Hi, $userName!"
                } else {
                    binding.tvGreeting.text = "Hi there!"
                    Log.d("HomeFragment", "No se encontraron datos del usuario")
                }
            }.addOnFailureListener { e ->
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

    private fun setupCategoryToggleListeners() {
        val toggles = listOf(
            binding.toggleBreakfast,
            binding.toggleLunch,
            binding.toggleDinner,
            binding.toggleSnack,
            binding.toggleEasy,
            binding.toggleMedium,
            binding.toggleVegan,
            binding.toggleSpicy
        )

        for (toggle in toggles) {
            toggle.setOnCheckedChangeListener { _, _ ->
                applyCategoryFilters()
            }
        }
    }

    private fun applyCategoryFilters() {
        val selectedCategories = mutableListOf<String>()

        if (binding.toggleBreakfast.isChecked) selectedCategories.add("Breakfast")
        if (binding.toggleLunch.isChecked) selectedCategories.add("Lunch")
        if (binding.toggleDinner.isChecked) selectedCategories.add("Dinner")
        if (binding.toggleSnack.isChecked) selectedCategories.add("Snack")
        if (binding.toggleEasy.isChecked) selectedCategories.add("Easy")
        if (binding.toggleMedium.isChecked) selectedCategories.add("Medium")
        if (binding.toggleVegan.isChecked) selectedCategories.add("Vegan")
        if (binding.toggleSpicy.isChecked) selectedCategories.add("Spicy")

        val currentFragment = viewPagerAdapter.getFragment(binding.viewPager.currentItem)

        when (currentFragment) {
            is ExploreFragment -> filterRecipesInFragment(currentFragment, selectedCategories)
            is MyRecipesFragment -> filterRecipesInFragment(currentFragment, selectedCategories)
        }
    }

    private fun filterRecipesInFragment(fragment: Fragment, selectedCategories: List<String>) {
        val listView = fragment.view?.findViewById<ListView>(R.id.lvRecipes)
        val adapter = listView?.adapter as? RecipeAdapter

        val allRecipes = when (fragment) {
            is ExploreFragment -> fragment.getAllRecipes()
            is MyRecipesFragment -> fragment.getAllRecipes()
            else -> null
        } ?: return

        val filtered = if (selectedCategories.isEmpty()) {
            allRecipes
        } else {
            allRecipes.filter { recipe ->
                recipe.categories.any { it in selectedCategories }
            }
        }

        adapter?.clear()
        adapter?.addAll(filtered)
        adapter?.notifyDataSetChanged()
    }


}

// Adaptador modificado para mantener referencias a los fragmentos
class ViewPagerAdapter(fragmentActivity: FragmentActivity) :
    FragmentStateAdapter(fragmentActivity) {

    private val fragments = SparseArray<Fragment>()

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        val fragment = when (position) {
            0 -> ExploreFragment()
            1 -> MyRecipesFragment()
            else -> throw IllegalStateException("Invalid position")
        }
        fragments.put(position, fragment)
        return fragment
    }

    fun getFragment(position: Int): Fragment? {
        return fragments[position]
    }
}