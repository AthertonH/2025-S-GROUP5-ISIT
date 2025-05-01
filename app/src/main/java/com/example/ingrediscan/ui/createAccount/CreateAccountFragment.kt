package com.example.ingrediscan.ui.createAccount

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.ingrediscan.R
import com.example.ingrediscan.databinding.FragmentCreateAccountBinding
import com.example.ingrediscan.model.Password
import com.example.ingrediscan.model.Email
import androidx.lifecycle.Observer
import com.google.firebase.auth.FirebaseUser

class CreateAccountFragment : Fragment() {
    private var _binding: FragmentCreateAccountBinding? = null
    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!
    private lateinit var createAccountViewModel: CreateAccountViewModel
    private val TAG = "CreateAccountFragment"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val createAccountViewModel =
            ViewModelProvider(this).get(CreateAccountViewModel::class.java)

        _binding = FragmentCreateAccountBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // sign up button
        binding.buttonSignup.setOnClickListener {
            // TODO: Use "fullname" somewhere in the project"
            val fullname = binding.enterFullname.text.toString().trim()
            val email = binding.enterUsername.text.toString().trim() //email (to be more accurate)
            val password = binding.enterPassword.text.toString().trim()
            val confirmPassword = binding.confirmPassword.text.toString().trim()

            // Pop-up error message if fields are empty
            if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || fullname.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill in all fields.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener // Exit the listener if fields are empty
            }
            // Pop-up error message if passwords don't match
            if (password != confirmPassword) {
                Toast.makeText(requireContext(), "Passwords do not match.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener // Exit if passwords don't match
            }

            // Email validation using Email.kt
            try {
                Email(email)
            } catch (e: IllegalArgumentException) {
                Toast.makeText(requireContext(), e.message, Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            // Password validation using Password.kt
            try {
                Password.validate(password)
                // successful sign up if we get to here
                //findNavController().navigate(R.id.signup_to_home) // Navigate to home screen on success
                createAccountViewModel.createAccount(email, password)
            } catch (e: IllegalArgumentException) {
                // Password validation failed
                Toast.makeText(requireContext(), e.message, Toast.LENGTH_LONG).show()
            }

        }

        // go back to login page if already have an account
        binding.Linksignin.setOnClickListener {
            findNavController().navigate(R.id.signup_to_login)
        }

        // LiveData observation
        // .observe() allows us to "watch" the currentUser Livedata for any changes to its value,
        // When the value of currentUser changes, the code inside the observe() method will run.
        // Observe currentUser changes
        createAccountViewModel.currentUser.observe(viewLifecycleOwner, Observer { user ->
            updateUI(user)
        })

        // Observe error messages
        // viewLifeCycleOwner tells LiveData to only observe the currentUser data as long as the view is active.
        // Helps prevent memory leaks
        createAccountViewModel.errorMessage.observe(viewLifecycleOwner, Observer { message ->
            if (!message.isNullOrEmpty()) {
                Log.e(TAG, "Sign-up error: $message")
                Toast.makeText(context, "Sign-up failed: $message", Toast.LENGTH_LONG).show()
            }
        })

        return root
    }

    /**
     * Update the UI based on the current user's authentication state.
     * If the user is signed in, navigate to the home screen. Otherwise, display an error message.
     *
     * @param user The current user, or null if no user is signed in.
     */
    private fun updateUI(user: FirebaseUser?) {
        if (user != null){
            Log.d(TAG, "User signed in: ${user.email}")
            Toast.makeText(context, "Sign-up successful! User: ${user.email}", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.signup_to_home)
        } else {
            Log.d(TAG, "No user signed up (or sign-up failed, check error message)")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}