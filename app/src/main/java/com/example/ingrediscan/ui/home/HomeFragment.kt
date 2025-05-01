package com.example.ingrediscan.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.ingrediscan.databinding.FragmentHomeBinding
import androidx.navigation.fragment.findNavController
import com.example.ingrediscan.R
import android.util.Log
import com.example.ingrediscan.BackEnd.ApiCalls.FoodApiService
import com.example.ingrediscan.ui.home.HomeViewModel


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = homeViewModel
        val caloriesTextView: TextView = binding.caloriesTracked
        val labelTextView: TextView = binding.caloriesLabel
        val progressBar: ProgressBar = binding.progressBar
        val appNameTextView: TextView = binding.appName
        val taglineTextView: TextView = binding.tagline

        homeViewModel.caloriesTracked.observe(viewLifecycleOwner) {
            homeViewModel.loadTotalCaloriesFromDB()
            //progress bar will update after the loadTotalCaloriesFromDB() is called
            //ensures that the progress bar value is accurate & synchronized w/ the above function
            progressBar.progress = homeViewModel.calculateProgress()
        }

        homeViewModel.label.observe(viewLifecycleOwner) {
            labelTextView.text = it
        }

        // The food items in the latest foods container updates dynamically
        // Have them grouped into one via a list to improve performance time
        homeViewModel.foodItems.observe(viewLifecycleOwner) { foods ->
            binding.foodItem1.text = foods.getOrNull(0)?.let { "${it.name}\n${it.calories} Calories" } ?: ""
            binding.foodItem2.text = foods.getOrNull(1)?.let { "${it.name}\n${it.calories} Calories" } ?: ""
            binding.foodItem3.text = foods.getOrNull(2)?.let { "${it.name}\n${it.calories} Calories" } ?: ""
            binding.foodItem4.text = foods.getOrNull(3)?.let { "${it.name}\n${it.calories} Calories" } ?: ""
        }

        // Load latest foods when fragment is opened
        homeViewModel.loadLatestFoods()

        appNameTextView.text = "IngrediScan"
        taglineTextView.text = "Nutrition transparency at your fingertips"


        val breakfastAddButton: View = binding.BreakfastAddButton
        breakfastAddButton.setOnClickListener {

            //Used to pass the meal type info to the LogMealBySearch fragment
            val bundle = Bundle().apply {
                putString("mealType", "Breakfast")
            }

            // Navigate to LogMealBySearchFragment from Log Meal for Breakfast button
            findNavController().navigate(R.id.navigation_log_meal_by_search, bundle)
        }

        val lunchAddButton : View = binding.LunchAddButton
        lunchAddButton.setOnClickListener {

            //Used to pass the meal type info to the LogMealBySearch fragment
            val bundle = Bundle().apply {
                putString("mealType", "Lunch")
            }

            // Navigate to LogMealBySearchFragment from Log Meal for Lunch button
            findNavController().navigate(R.id.navigation_log_meal_by_search, bundle)
        }

        val dinnerAddButton : View = binding.DinnerAddButton
        dinnerAddButton.setOnClickListener {

            //Used to pass the meal type info to the LogMealBySearch fragment
            val bundle = Bundle().apply {
                putString("mealType", "Dinner")
            }

            // Navigate to LogMealBySearchFragment from Log Meal for Dinner button
            findNavController().navigate(R.id.navigation_log_meal_by_search, bundle)
        }

        val snackAddButton : View = binding.SnackAddButton
        snackAddButton.setOnClickListener {

            //Used to pass the meal type info to the LogMealBySearch fragment
            val bundle = Bundle().apply {
                putString("mealType", "Snack")
            }

            //Navigate to LogMealBySearchFragment from Log Meal for Snack Button
            findNavController().navigate(R.id.navigation_log_meal_by_search, bundle)
        }

        val viewAllButton: View = binding.viewAll
        viewAllButton.setOnClickListener {
            findNavController().navigate(R.id.navigation_previous_results)
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}
