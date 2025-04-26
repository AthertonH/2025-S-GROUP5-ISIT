package com.example.ingrediscan.model

/**
 * Represents a user's email address.
 *
 * @property value The email address value.
 *
 * @throws IllegalArgumentException if the email address is not in a valid format.
 */
class Email(private val value: String) {
    init {
        require(isValidEmail(value)) { "Invalid email address format." }
    }

    override fun toString(): String {
        return value
    }

    // if we are going to handle emails and passwords ourselves, then we'll need to
    // add more rigorous validation here.
    companion object {
        private fun isValidEmail(email: String?): Boolean {
            // Simple email validation
            if (email == null) {
                return false
            }
            val emailRegex = Regex("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}")
            return emailRegex.matches(email)
        }
    }
}