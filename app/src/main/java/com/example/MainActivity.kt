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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.ui.WorkoutViewModel
import com.example.ui.navigation.NavDestination
import com.example.ui.navigation.NeonBottomBar
import com.example.ui.screens.HubScreen
import com.example.ui.screens.ProgressScreen
import com.example.ui.screens.WorkoutScreen
import com.example.ui.theme.DarkBg
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    private val viewModel: WorkoutViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val initialTab = intent.getStringExtra(EXTRA_DESTINATION) ?: NavDestination.WORKOUT.name

        setContent {
            MyApplicationTheme {
                MainAppScreen(
                    viewModel = viewModel,
                    initialDestination = try {
                        NavDestination.valueOf(initialTab)
                    } catch (_: Exception) {
                        NavDestination.WORKOUT
                    },
                    onLaunchActivity = { destination ->
                        when (destination) {
                            NavDestination.PROGRESS -> {
                                val intent = Intent(this, ProgressActivity::class.java)
                                startActivity(intent)
                            }
                            NavDestination.HUB -> {
                                val intent = Intent(this, HubActivity::class.java)
                                startActivity(intent)
                            }
                            NavDestination.WORKOUT -> {
                                // Already in workout
                            }
                        }
                    }
                )
            }
        }
    }

    companion object {
        const val EXTRA_DESTINATION = "extra_destination"
    }
}

@Composable
fun MainAppScreen(
    viewModel: WorkoutViewModel,
    initialDestination: NavDestination = NavDestination.WORKOUT,
    onLaunchActivity: ((NavDestination) -> Unit)? = null
) {
    var currentDestination by remember { mutableStateOf(initialDestination) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = DarkBg,
        bottomBar = {
            NeonBottomBar(
                currentDestination = currentDestination,
                onNavigate = { dest ->
                    currentDestination = dest
                }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            when (currentDestination) {
                NavDestination.WORKOUT -> {
                    WorkoutScreen(viewModel = viewModel)
                }
                NavDestination.PROGRESS -> {
                    ProgressScreen(viewModel = viewModel)
                }
                NavDestination.HUB -> {
                    HubScreen(viewModel = viewModel)
                }
            }
        }
    }
}

