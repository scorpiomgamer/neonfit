package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.ui.WorkoutViewModel
import com.example.ui.navigation.NavDestination
import com.example.ui.navigation.NeonBottomBar
import com.example.ui.screens.HubScreen
import com.example.ui.theme.DarkBg
import com.example.ui.theme.MyApplicationTheme

class HubActivity : ComponentActivity() {
    private val viewModel: WorkoutViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = DarkBg,
                    bottomBar = {
                        NeonBottomBar(
                            currentDestination = NavDestination.HUB,
                            onNavigate = { dest ->
                                when (dest) {
                                    NavDestination.WORKOUT -> {
                                        val intent = Intent(this, MainActivity::class.java).apply {
                                            putExtra(MainActivity.EXTRA_DESTINATION, NavDestination.WORKOUT.name)
                                            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                                        }
                                        startActivity(intent)
                                        finish()
                                    }
                                    NavDestination.PROGRESS -> {
                                        val intent = Intent(this, ProgressActivity::class.java).apply {
                                            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                                        }
                                        startActivity(intent)
                                        finish()
                                    }
                                    NavDestination.HUB -> {}
                                }
                            }
                        )
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.fillMaxSize()) {
                        HubScreen(viewModel = viewModel)
                    }
                }
            }
        }
    }
}
