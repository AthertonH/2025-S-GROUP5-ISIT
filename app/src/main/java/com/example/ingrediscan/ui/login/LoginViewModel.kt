package com.example.ingrediscan.ui.login

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class LoginViewModel : ViewModel() {

    private val _username = MutableLiveData<String>()
    val username: LiveData<String> get() = _username

    private val _password = MutableLiveData<String>()
    val password: LiveData<String> get() = _password

    private val _currentUser = MutableLiveData<FirebaseUser?>()
    val currentUser: LiveData<FirebaseUser?> get() = _currentUser

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    companion object {
        private const val TAG = "LoginViewModel"
    }

    fun login(email: String, password: String) {
        _username.value = email
        _password.value = password

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Sign in success
                    Log.d(TAG, "signInWithEmail:success (used 'Login' button)")
                    val user = auth.currentUser
                    _currentUser.value = user
                    _errorMessage.value = null // Clear any previous error
                } else {
                    // If sign in fails
                    Log.w(TAG, "signInWithEmail:failure (used 'Login' button)", task.exception)
                    _errorMessage.value = task.exception?.message ?: "Authentication failed."
                    _currentUser.value = null
                }
            }
    }
}
