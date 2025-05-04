package com.example.ingrediscan.ui.profile

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.ingrediscan.R
import com.example.ingrediscan.databinding.FragmentProfileBinding
import com.example.ingrediscan.utils.*
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth

private lateinit var pickImageLauncher: ActivityResultLauncher<Intent>

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var profileViewModel: ProfileViewModel

    // Stores/saves a value
    private fun saveToPrefs(key: String, value: String) {
        val prefs = requireContext().getSharedPreferences("UserProfile", Context.MODE_PRIVATE)
        prefs.edit().putString(key, value).apply()
    }

    // Retrieves/loads a value
    private fun getFromPrefs(key: String): String? {
        val prefs = requireContext().getSharedPreferences("UserProfile", Context.MODE_PRIVATE)
        return prefs.getString(key, null)
    }

    // Sets up view for the ProfileFragment
    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val root: View = binding.root

        profileViewModel = ViewModelProvider(this)[ProfileViewModel::class.java]

        pickImageLauncher = setupImagePicker(binding.profileButtonImage)
        binding.profileBanner.visibility = View.VISIBLE

        val weightButton = binding.profileButtonWeight
        val heightButton = binding.profileButtonHeight
        val ageButton = binding.profileButtonAge
        val sexButton = binding.profileButtonSex
        val activityLevelButton = binding.profileButtonActivityLevel

        // Restore preferences (Otherwise preferences will reset when leaving the page)
        getFromPrefs("weight")?.toIntOrNull()?.let { profileViewModel.setWeight(it) }
        getFromPrefs("heightFeet")?.toIntOrNull()?.let { feet ->
            getFromPrefs("heightInches")?.toIntOrNull()?.let { inches ->
                profileViewModel.setHeight(feet, inches)
            }
        }
        getFromPrefs("age")?.toIntOrNull()?.let { profileViewModel.setAge(it) }
        getFromPrefs("sex")?.let { profileViewModel.setSex(it) }
        getFromPrefs("activityLevel")?.let { profileViewModel.setActivityLevel(it) }

        // This will automatically update the BMI and Caloric goals, even after leaving the page
        // or closing the application
        profileViewModel.updateBMI()
        profileViewModel.updateCalorieGoals()

        profileViewModel.weight.observe(viewLifecycleOwner) { newWeight ->
            weightButton.text = getString(R.string.weight_label, newWeight)
        }
        weightButton.setOnClickListener {
            showWeightPickerDialog { weight ->
                profileViewModel.setWeight(weight)
                saveToPrefs("weight", weight.toString())
                profileViewModel.updateBMI()
                profileViewModel.updateCalorieGoals()
            }
        }

        profileViewModel.heightFeet.observe(viewLifecycleOwner) { feet ->
            profileViewModel.heightInches.observe(viewLifecycleOwner) { inches ->
                heightButton.text = getString(R.string.height_label, feet, inches)
            }
        }
        heightButton.setOnClickListener {
            showHeightPickerDialog { feet, inches ->
                profileViewModel.setHeight(feet, inches)
                saveToPrefs("heightFeet", feet.toString())
                saveToPrefs("heightInches", inches.toString())
                profileViewModel.updateBMI()
                profileViewModel.updateCalorieGoals()
            }
        }

        profileViewModel.age.observe(viewLifecycleOwner) { newAge ->
            ageButton.text = getString(R.string.age_label, newAge)
        }
        ageButton.setOnClickListener {
            showNumberPickerDialog("Select Age", 18, 100) { age ->
                profileViewModel.setAge(age)
                saveToPrefs("age", age.toString())
                profileViewModel.updateCalorieGoals()
            }
        }

        profileViewModel.sex.observe(viewLifecycleOwner) { newSex ->
            sexButton.text = getString(R.string.sex_label, newSex)
        }
        sexButton.setOnClickListener {
            showChoiceDialog("Select Sex", listOf("Male", "Female")) { selectedSex ->
                profileViewModel.setSex(selectedSex)
                saveToPrefs("sex", selectedSex)
                profileViewModel.updateCalorieGoals()
            }
        }

        profileViewModel.activityLevel.observe(viewLifecycleOwner) { newActivityLevel ->
            activityLevelButton.text = getString(R.string.activity_level_label, newActivityLevel)
        }
        activityLevelButton.setOnClickListener {
            val levels =
                listOf("Sedentary", "Lightly Active", "Moderately Active", "Active", "Very Active")
            showChoiceDialog("Select Activity Level", levels) { selected ->
                profileViewModel.setActivityLevel(selected)
                saveToPrefs("activityLevel", selected)
                profileViewModel.updateCalorieGoals()
            }
        }

        binding.profileButtonPassword.setOnClickListener {
            val context = requireContext()
            val layout = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(50, 40, 50, 10)
            }

            val oldPasswordInput = EditText(context).apply {
                hint = "Current Password"
                inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }

            val newPasswordInput = EditText(context).apply {
                hint = "New Password"
                inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }

            val confirmPasswordInput = EditText(context).apply {
                hint = "Confirm New Password"
                inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }

            layout.addView(oldPasswordInput)
            layout.addView(newPasswordInput)
            layout.addView(confirmPasswordInput)

            AlertDialog.Builder(context).setTitle("Change Password").setView(layout)
                .setPositiveButton("Submit") { dialog, _ ->
                    val oldPass = oldPasswordInput.text.toString()
                    val newPass = newPasswordInput.text.toString()
                    val confirmPass = confirmPasswordInput.text.toString()

                    when {
                        oldPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty() -> {
                            Toast.makeText(context, "All fields are required", Toast.LENGTH_SHORT)
                                .show()
                        }

                        newPass != confirmPass -> {
                            Toast.makeText(
                                context, "New passwords do not match", Toast.LENGTH_SHORT
                            ).show()
                        }

                        oldPass == newPass -> {
                            Toast.makeText(
                                context, "New password must be different", Toast.LENGTH_SHORT
                            ).show()
                        }

                        else -> {
                            val user = FirebaseAuth.getInstance().currentUser
                            if (user != null && user.email != null) {
                                val credential =
                                    EmailAuthProvider.getCredential(user.email!!, oldPass)
                                user.reauthenticate(credential)
                                    .addOnCompleteListener { reauthTask ->
                                        if (reauthTask.isSuccessful) {
                                            user.updatePassword(newPass)
                                                .addOnCompleteListener { updateTask ->
                                                    if (updateTask.isSuccessful) {
                                                        Toast.makeText(
                                                            context,
                                                            "Password changed successfully",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    } else {
                                                        Toast.makeText(
                                                            context,
                                                            "Password update failed: ${updateTask.exception?.message}",
                                                            Toast.LENGTH_LONG
                                                        ).show()
                                                    }
                                                }
                                        } else {
                                            Toast.makeText(
                                                context,
                                                "Reauthentication failed: ${reauthTask.exception?.message}",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    }
                            } else {
                                Toast.makeText(context, "User not signed in", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                    }
                    dialog.dismiss()
                }.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }.show()
        }

        binding.profileButtonLogout.setOnClickListener{
            FirebaseAuth.getInstance().signOut()
            findNavController().navigate(R.id.navigation_login)
        }

        profileViewModel.bmi.observe(viewLifecycleOwner)
        {
                bmi -> binding.bmiText.text = getString(R.string.bmi_label, bmi)
        }

        profileViewModel.calorieGoals.observe(viewLifecycleOwner) { goals ->
            val weight = profileViewModel.weight.value
            val feet = profileViewModel.heightFeet.value
            val inches = profileViewModel.heightInches.value
            val age = profileViewModel.age.value
            val sex = profileViewModel.sex.value
            val activity = profileViewModel.activityLevel.value

            val missing = mutableListOf<String>()
            if (weight == null || weight == 0) missing.add("weight")
            if (feet == null || feet == 0 || inches == null) missing.add("height")
            if (age == null || age == 0) missing.add("age")
            if (sex == null || sex == "N/A") missing.add("sex")
            if (activity == null || activity == "N/A") missing.add("activity level")

            if (missing.isNotEmpty())
            {
                binding.caloricSuggestionLoss.text = "Please enter ${missing.joinToString(", ")}"
                binding.caloricSuggestionGain.text = ""
            }
            // Shows maintain, loss, or gain
            else
            {
                binding.caloricSuggestionLoss.text = """
                Maintain: ${goals.maintain} kcal
                Mild Loss: ${goals.mildLoss} kcal
                Loss: ${goals.loss} kcal
                Extreme Loss: ${goals.extremeLoss} kcal
                """.trimIndent()

                binding.caloricSuggestionGain.text = """
                Maintain: ${goals.maintain} kcal
                Mild Gain: ${goals.mildGain} kcal
                Gain: ${goals.gain} kcal
                Extreme Gain: ${goals.extremeGain} kcal
                """.trimIndent()
            }
        }

        return root
    }

    override fun onDestroyView()
    {
        super.onDestroyView()
        _binding = null
    }
}