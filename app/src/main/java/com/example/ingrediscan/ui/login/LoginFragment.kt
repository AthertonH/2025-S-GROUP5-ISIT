package com.example.ingrediscan.ui.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.ingrediscan.databinding.FragmentLoginBinding
import androidx.navigation.fragment.findNavController
import com.example.ingrediscan.R
import android.util.Log
import androidx.lifecycle.Observer
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.FirebaseAuth


class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!
    private lateinit var loginViewModel: LoginViewModel
    private val TAG = "LoginFragment"
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
         loginViewModel =
            ViewModelProvider(this).get(LoginViewModel::class.java)

        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        val root: View = binding.root

        auth = FirebaseAuth.getInstance()

        // login button
        binding.buttonLogin.setOnClickListener {
            val username = binding.enterUsername.text.toString().trim()
            val password = binding.enterPassword.text.toString().trim()

            // Pop up an error message when inputs for username and password are empty
            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter username and password.", Toast.LENGTH_SHORT).show()
            } else { // successful login
                // TO DO: back end
                //findNavController().navigate(R.id.login_to_home)
                loginViewModel.login(username, password)
            }
        }

        // sign up button
        binding.buttonSignup.setOnClickListener {
            findNavController().navigate(R.id.navigation_create_account)
        }

        loginViewModel.currentUser.observe(viewLifecycleOwner, Observer { user ->
            updateUI(user)
        })

        loginViewModel.errorMessage.observe(viewLifecycleOwner, Observer { message ->
            if (!message.isNullOrEmpty()) {
                Log.e(TAG, "Login error: $message")
                Toast.makeText(context, "Login failed: $message", Toast.LENGTH_LONG).show()
                Log.d(TAG, "No user signed in (or login failed, check error message)")
            }
        })

        return root
    }

    override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        if(currentUser != null) {
            Log.d(TAG, "User being signed-in from cached credentials/tokens")
            updateUI(currentUser)
        }
    }

    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            Log.d(TAG, "User signed in: ${user.email}")
            Toast.makeText(context, "Login successful! User: ${user.email}", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.login_to_home)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
