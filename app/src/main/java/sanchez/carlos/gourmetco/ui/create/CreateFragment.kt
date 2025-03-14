package sanchez.carlos.gourmetco.ui.create

import android.content.Intent
import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import sanchez.carlos.gourmetco.MainActivity
import sanchez.carlos.gourmetco.R

class CreateFragment : Fragment() {

    private lateinit var cancel : Button
    private lateinit var save : Button
    private lateinit var unitySpinner: Spinner

    companion object {
        fun newInstance() = CreateFragment()
    }

    private val viewModel: CreateViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_create, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cancel = view.findViewById(R.id.btnCancel)
        save = view.findViewById(R.id.btnSave)
        unitySpinner = view.findViewById(R.id.unity)

        unitySpinner.setSelection(0)
        setupUnitySpinner()

        cancel.setOnClickListener {
            val intent = Intent(requireContext(), MainActivity::class.java)
            startActivity(intent)
        }

        save.setOnClickListener {
            val intent = Intent(requireContext(), MainActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupUnitySpinner() {
        val units = arrayOf("Pz", "Sp", "Cup")
        val adapter = ArrayAdapter(requireContext(),
            android.R.layout.simple_spinner_item, units)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        unitySpinner.adapter = adapter
    }
}