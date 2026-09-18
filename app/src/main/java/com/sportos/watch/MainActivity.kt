package com.sportos.watch

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.google.android.horologist.compose.layout.AppScaffold
import com.sportos.watch.presentation.screens.ActiveWorkoutScreen
import com.sportos.watch.presentation.screens.MainMenuScreen
import com.sportos.watch.presentation.screens.WorkoutSummaryScreen
import com.sportos.watch.presentation.theme.SportOSTheme
import com.sportos.watch.service.WorkoutService
import com.sportos.watch.sports.SportEngineState

class MainActivity : ComponentActivity() {

    private var workoutServiceBinder by mutableStateOf<WorkoutService.LocalBinder?>(null)
    private var hasPermissions by mutableStateOf(false)

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasPermissions = permissions.values.all { it }
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            workoutServiceBinder = service as WorkoutService.LocalBinder
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            workoutServiceBinder = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        checkAndRequestPermissions()

        setContent {
            SportOSTheme {
                AppScaffold {
                    CalopeiaApp(
                        binder = workoutServiceBinder,
                        onStartWorkout = { sport, subMode, targetPace, isDemo ->
                            startWorkoutService(sport, subMode, targetPace, isDemo)
                        },
                        onStopWorkout = { stopWorkoutService() },
                        onPauseWorkout = { workoutServiceBinder?.pause() },
                        onResumeWorkout = { workoutServiceBinder?.resume() },
                        onLapTrigger = { workoutServiceBinder?.lap() }
                    )
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val intent = Intent(this, WorkoutService::class.java)
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    override fun onStop() {
        super.onStop()
        if (workoutServiceBinder != null) {
            unbindService(serviceConnection)
            workoutServiceBinder = null
        }
    }

    private fun checkAndRequestPermissions() {
        val required = mutableListOf(
            Manifest.permission.BODY_SENSORS,
            Manifest.permission.ACTIVITY_RECOGNITION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            required.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        val missing = required.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missing.isNotEmpty()) {
            permissionLauncher.launch(missing.toTypedArray())
        } else {
            hasPermissions = true
        }
    }

    private fun startWorkoutService(
        sportType: String,
        subMode: String,
        targetPace: Double,
        isDemo: Boolean
    ) {
        val intent = Intent(this, WorkoutService::class.java).apply {
            action = WorkoutService.ACTION_START_WORKOUT
            putExtra(WorkoutService.EXTRA_SPORT_TYPE, sportType)
            putExtra(WorkoutService.EXTRA_SPORT_MODE, subMode)
            putExtra(WorkoutService.EXTRA_TARGET_PACE, targetPace)
            putExtra(WorkoutService.EXTRA_DEMO_MODE, isDemo)
        }
        startForegroundService(intent)
    }

    private fun stopWorkoutService() {
        val intent = Intent(this, WorkoutService::class.java).apply {
            action = WorkoutService.ACTION_STOP_WORKOUT
        }
        startService(intent)
    }
}

@Composable
fun CalopeiaApp(
    binder: WorkoutService.LocalBinder?,
    onStartWorkout: (sport: String, subMode: String, targetPace: Double, isDemo: Boolean) -> Unit,
    onStopWorkout: () -> Unit,
    onPauseWorkout: () -> Unit,
    onResumeWorkout: () -> Unit,
    onLapTrigger: () -> Unit
) {
    val navController = rememberSwipeDismissableNavController()
    var currentSport by remember { mutableStateOf(binder?.currentSport ?: "Running") }
    var lastRecordedState by remember { mutableStateOf<SportEngineState?>(null) }

    // Seamless auto-reconnect: if WorkoutService is active in background, route directly to workout
    androidx.compose.runtime.LaunchedEffect(binder, binder?.isWorkoutActive) {
        if (binder != null && binder.isWorkoutActive) {
            currentSport = binder.currentSport
            if (navController.currentBackStackEntry?.destination?.route != "workout" &&
                navController.currentBackStackEntry?.destination?.route != "summary") {
                navController.navigate("workout") {
                    popUpTo("home") { inclusive = false }
                }
            }
        }
    }

    SwipeDismissableNavHost(
        navController = navController,
        startDestination = "home"
    ) {
        // 1. MAIN MENU
        composable("home") {
            MainMenuScreen(
                onSportSelected = { sport, subMode, pace, isDemo ->
                    currentSport = sport
                    onStartWorkout(sport, subMode, pace, isDemo)
                    navController.navigate("workout")
                },
                onNavigateToHistory = {
                    navController.navigate("history")
                }
            )
        }

        // 2. ACTIVE WORKOUT SCREEN
        composable("workout") {
            val liveState by binder?.engineState?.collectAsState(initial = null) ?: mutableStateOf(null)
            
            // Keep track of the latest state for the post-workout summary
            if (liveState != null) {
                lastRecordedState = liveState
            }

            ActiveWorkoutScreen(
                sportType = currentSport,
                engineState = liveState,
                activeEngine = binder?.getService()?.activeEngine,
                onPauseWorkout = onPauseWorkout,
                onResumeWorkout = onResumeWorkout,
                onLapTrigger = onLapTrigger,
                onFinishWorkout = {
                    // Capture guaranteed final snapshot before tearing down the foreground service
                    val finalSnapshot = binder?.latestStateSnapshot ?: liveState ?: lastRecordedState
                    lastRecordedState = finalSnapshot
                    onStopWorkout()
                    navController.navigate("summary") {
                        popUpTo("home") { inclusive = false }
                    }
                }
            )
        }

        // 3. POST-WORKOUT CELEBRATION & SUMMARY
        composable("summary") {
            WorkoutSummaryScreen(
                sportType = currentSport,
                finalState = lastRecordedState,
                onDismiss = {
                    lastRecordedState = null
                    navController.popBackStack("home", inclusive = false)
                }
            )
        }

        // 4. WORKOUT HISTORY & PERSONAL BESTS
        composable("history") {
            com.sportos.watch.presentation.screens.WorkoutHistoryScreen(
                onNavigateBack = {
                    navController.popBackStack("home", inclusive = false)
                }
            )
        }
    }
}
