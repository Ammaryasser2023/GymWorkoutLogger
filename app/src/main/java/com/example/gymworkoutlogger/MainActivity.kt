package com.example.gymworkoutlogger

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.gymworkoutlogger.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. Status Bar & Notch Insets Protection
        ViewCompat.setOnApplyWindowInsetsListener(binding.mainRoot) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(0, systemBars.top, 0, 0)
            insets
        }

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        binding.bottomNavigation.setupWithNavController(navController)

        // 2. Breadcrumbs & Top Bar Setup
        binding.btnBreadcrumbBack.setOnClickListener {
            navController.navigateUp()
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.dashboardFragment -> {
                    binding.btnBreadcrumbBack.visibility = View.GONE
                    binding.ivBreadcrumbIcon.setImageResource(R.drawable.ic_home)
                    binding.tvBreadcrumbPath.text = "Home"
                }
                R.id.exerciseLibraryFragment -> {
                    binding.btnBreadcrumbBack.visibility = View.GONE
                    binding.ivBreadcrumbIcon.setImageResource(R.drawable.ic_exercise)
                    binding.tvBreadcrumbPath.text = "Exercise Library"
                }
                R.id.workoutHistoryFragment -> {
                    binding.btnBreadcrumbBack.visibility = View.GONE
                    binding.ivBreadcrumbIcon.setImageResource(R.drawable.ic_history)
                    binding.tvBreadcrumbPath.text = "Workout History"
                }
                R.id.profileFragment -> {
                    binding.btnBreadcrumbBack.visibility = View.GONE
                    binding.ivBreadcrumbIcon.setImageResource(R.drawable.ic_person)
                    binding.tvBreadcrumbPath.text = "Profile"
                }
                R.id.workoutDetailFragment -> {
                    binding.btnBreadcrumbBack.visibility = View.VISIBLE
                    binding.ivBreadcrumbIcon.setImageResource(R.drawable.ic_history)
                    binding.tvBreadcrumbPath.text = "History  ›  Workout Detail"
                }
                R.id.activeWorkoutFragment -> {
                    binding.btnBreadcrumbBack.visibility = View.VISIBLE
                    binding.ivBreadcrumbIcon.setImageResource(R.drawable.ic_exercise)
                    binding.tvBreadcrumbPath.text = "Home  ›  Active Workout"
                }
                else -> {
                    binding.btnBreadcrumbBack.visibility = View.VISIBLE
                    binding.tvBreadcrumbPath.text = destination.label ?: "Gym Logger"
                }
            }
        }
    }
}
