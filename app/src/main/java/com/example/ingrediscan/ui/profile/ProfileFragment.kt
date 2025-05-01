package com.example.ingrediscan.ui.profile
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
import com.google.firebase.auth.FirebaseAuth

private lateinit var pickImageLauncher: ActivityResultLauncher<Intent>

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var profileViewModel: ProfileViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val root: View = binding.root

        profileViewModel = ViewModelProvider(this)[ProfileViewModel::class.java]

        // Use image picker utility (allow user to set profile picture)
        pickImageLauncher = setupImagePicker(binding.profileButtonImage)

        // Show profile banner
        binding.profileBanner.visibility = View.VISIBLE

        // Buttons
        val weightButton = binding.profileButtonWeight
        val heightButton = binding.profileButtonHeight
        val ageButton = binding.profileButtonAge
        val sexButton = binding.profileButtonSex
        val activityLevelButton = binding.profileButtonActivityLevel

        // *** Weight Button ***
        profileViewModel.weight.observe(viewLifecycleOwner) { newWeight ->
            weightButton.text = getString(R.string.weight_label, newWeight)
        }
        weightButton.setOnClickListener {
            showNumberPickerDialog("Enter Weight", 100, 500) { weight ->
                profileViewModel.setWeight(weight)
                profileViewModel.updateBMI()
                profileViewModel.updateCalorieGoals()
            }
        }

        // *** Height Button ***
        profileViewModel.heightFeet.observe(viewLifecycleOwner) { feet ->
            profileViewModel.heightInches.observe(viewLifecycleOwner) { inches ->
                heightButton.text = getString(R.string.height_label, feet, inches)
            }
        }
        heightButton.setOnClickListener {
            showHeightPickerDialog { feet, inches ->
                profileViewModel.setHeight(feet, inches)
                profileViewModel.updateBMI()
                profileViewModel.updateCalorieGoals()
            }
        }

        // *** Age Button ***
        profileViewModel.age.observe(viewLifecycleOwner) { newAge ->
            ageButton.text = getString(R.string.age_label, newAge)
        }
        ageButton.setOnClickListener {
            showNumberPickerDialog("Select Age", 18, 100) { age ->
                profileViewModel.setAge(age)
                profileViewModel.updateCalorieGoals()
            }
        }

        // *** Sex Button ***
        profileViewModel.sex.observe(viewLifecycleOwner) { newSex ->
            sexButton.text = getString(R.string.sex_label, newSex)
        }
        sexButton.setOnClickListener {
            showChoiceDialog("Select Sex", listOf("Male", "Female")) { selectedSex ->
                profileViewModel.setSex(selectedSex)
                profileViewModel.updateCalorieGoals()
            }
        }

        // *** Activity Level Button ***
        profileViewModel.activityLevel.observe(viewLifecycleOwner) { newActivityLevel ->
            activityLevelButton.text = getString(R.string.activity_level_label, newActivityLevel)
        }
        activityLevelButton.setOnClickListener {
            val levels = listOf("Sedentary", "Lightly Active", "Moderately Active", "Active", "Very Active")
            showChoiceDialog("Select Activity Level", levels) { selected ->
                profileViewModel.setActivityLevel(selected)
                profileViewModel.updateCalorieGoals()
            }
        }

        // *** Change Password Button ***
        // REQUIRES BACK END FOR FULL FUNCTIONALITY
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

            AlertDialog.Builder(context)
                .setTitle("Change Password")
                .setView(layout)
                .setPositiveButton("Submit") { dialog, _ ->
                    val oldPass = oldPasswordInput.text.toString()
                    val newPass = newPasswordInput.text.toString()
                    val confirmPass = confirmPasswordInput.text.toString()

                    when {
                        oldPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty() -> {
                            Toast.makeText(context, "All fields are required", Toast.LENGTH_SHORT).show()
                        }
                        newPass != confirmPass -> {
                            Toast.makeText(context, "New passwords do not match", Toast.LENGTH_SHORT).show()
                        }
                        oldPass == newPass -> {
                            Toast.makeText(context, "New password must be different", Toast.LENGTH_SHORT).show()
                        }
                        else -> {
                            // TODO: Validate oldPass and save newPass
                            Toast.makeText(context, "Password changed successfully", Toast.LENGTH_SHORT).show()
                        }
                    }

                    dialog.dismiss()
                }
                .setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
                .show()
        }

        // Logout button
        binding.profileButtonLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            findNavController().navigate(R.id.navigation_login)
        }

        // *** BMI Text ***
        profileViewModel.bmi.observe(viewLifecycleOwner) { bmi ->
            binding.bmiText.text = getString(R.string.bmi_label, bmi)
        }

        // *** Calorie Suggestions ***
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

            binding.caloricSuggestionTextView.text = if (missing.isNotEmpty()) {
                "Please enter ${missing.joinToString(", ")}"
            } else {
                """
                    Maintain: ${goals.maintain} kcal
                    Mild Loss: ${goals.mildLoss} kcal
                    Loss: ${goals.loss} kcal
                    Extreme Loss: ${goals.extremeLoss} kcal
                """.trimIndent()
            }
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
