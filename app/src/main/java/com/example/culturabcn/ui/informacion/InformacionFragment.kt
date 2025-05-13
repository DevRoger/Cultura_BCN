package com.example.culturabcn.ui.informacion

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.culturabcn.databinding.FragmentConfiguracionBinding
import com.example.culturabcn.databinding.FragmentInformacionBinding

class InformacionFragment : Fragment() {

    private var _binding: FragmentInformacionBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
                             ): View {
        _binding = FragmentInformacionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}