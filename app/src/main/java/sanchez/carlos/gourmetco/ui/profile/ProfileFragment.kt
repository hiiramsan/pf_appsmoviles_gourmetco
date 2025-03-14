package sanchez.carlos.gourmetco.ui.profile

import android.content.Intent
import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import sanchez.carlos.gourmetco.R
import sanchez.carlos.gourmetco.ui.LoginActivity

import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import sanchez.carlos.gourmetco.MainActivity

class ProfileFragment : Fragment() {

    private lateinit var back : ImageButton
    private lateinit var btnLogOut : Button

    companion object {
        fun newInstance() = ProfileFragment()
    }

    private val viewModel: ProfileViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        back = view.findViewById(R.id.btn_back)
        btnLogOut = view.findViewById(R.id.btn_logOut)

        back.setOnClickListener {
            val intent = Intent(requireContext(), MainActivity::class.java)
            startActivity(intent)
        }

        btnLogOut.setOnClickListener({
            Firebase.auth.signOut()
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        })
    }
}