package com.example.hanger.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.hanger.HangerActivity
import com.example.hanger.LogInActivity
import com.example.hanger.R
import com.example.hanger.databinding.FragmentHomeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    private lateinit var logoutButton: Button

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        logoutButton = view.findViewById(R.id.logout)
        logoutButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            var intent: Intent = Intent(requireContext(), LogInActivity::class.java)
            startActivity(intent)
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}